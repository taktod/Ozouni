/*
 * wts - https://github.com/taktod/Ozouni/jetty/wts
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under The MIT license.
 * @author taktod
 */
// 画像の更新はrequestAnimationFrameをベースにして動作させます。
// requestAnimationFrameをつかって効率的に処理していきたい。
window.requestAnimationFrame = (function(){
	return window.requestAnimationFrame		||
		window.webkitRequestAnimationFrame	||
		window.mozRequestAnimationFrame		||
		window.oRequestAnimationFrame		||
		window.msRequestAnimationFrame		||
		function(callback, element){
			window.setTimeout(callback, 1000 / 60);
		};
})();

var worker = new Worker("adpcmDecodeWorker.js");

// 処理バッファサイズ
var bufsize = 1024;
var zeroBuffer = new Float32Array(10240);
// 再生中かどうかフラグ
var play = 0;
// audioContextを初期化します
if(typeof(webkitAudioContext)!=="undefined")
	var audioctx = new webkitAudioContext();
else if(typeof(AudioContext)!=="undefined")
	var audioctx = new AudioContext();
var scrproc = audioctx["createScriptProcessor"](bufsize); // 強制的に44100Hzになるっぽいです。(違うデータがくる可能性はあるかもしれませんけど・・・)
scrproc["onaudioprocess"] = Process;
scrproc["connect"](audioctx.destination);
var osc = null;

/** 音声バッファ */
var audioBuffers = [];
/** 映像バッファ */
var imageBuffers = [];
/** 現在の処理位置 */
var ats = 0;
/** 映像の動作context */
var ctx = document.getElementById("target").getContext("2d");
/** データの提供元のwebsocket */
var ws = null;

//var overflow = false;
worker.addEventListener("message", function(e) {
	audioBuffers.push(e.data);
}, false);

// データ受け取り処理
var onMessage = function(evt) {
	// メッセージ受け取り時
	if(!(evt.data instanceof Blob)) {
		return;
	}
	var blob = evt.data;
	var fr = new FileReader();
	fr.onloadend = function(event) {
		var buffer = event.target.result;
		var dataView = new DataView(buffer);
		var uint8ArrayView = new Uint8Array(buffer);
		var timestamp = dataView.getUint32(0);
		var type = dataView.getUint8(4);
		switch(type) {
		case 0: // 44100Hz
			var currentAdpcm = uint8ArrayView.subarray(5);
			// ここでadpcmをpcmに変換してfloatのArrayにしておきたいと思います。
			// 現状では、メモリーがパンクするので、ここでデコードしていない。
			worker.postMessage({ts:timestamp, buf:currentAdpcm});
//			audioBuffers.push({ts:timestamp, buf:getFloat32PcmArray(currentAdpcm), decoded:true});
			// 取得した瞬間に、Buffersのデータに変換してやる必要あり。
			break;
		case 1: // 22050Hz
			break;
		case 2: // 11025Hz
			break;
		case 3: // 5512Hz
			break;
		case 4: // 映像データ(適当に再生時に表示しないとだめ)
			// とりあえず画像にして、そのまま表示しておく。
			var imageArray = uint8ArrayView.subarray(5);
			var blob = new Blob([imageArray], {type:"image/jpeg"});
			var img = new Image();
			// イメージのロードは別のところで実施するようにしようか・・・
			img.onload = function() {
				imageBuffers.push({ts:timestamp, img: img});
			};
			img.src = window.URL.createObjectURL(blob);
			break;
		default:
			break;
		}
	};
	fr.readAsArrayBuffer(blob);
};
/** 現在処理中のbuffer */
var current = null; // currentBuffer object(buf pos left)
var prodec = false;
function Process(ev) {
	// もともとforループでコピーしていましたが、やめます。
	// arrayBufferのset関数つかった方が高速に動作できそうです。
	var buf0 = ev.outputBuffer.getChannelData(0);
	var buf1 = ev.outputBuffer.getChannelData(1);
	var target = {buf0:buf0, buf1:buf1, pos:0, left:buf0.length};
	do {
		if(current == null) {
			var data = audioBuffers.shift();
			if(data == null) {
				current = null;
			}
			else {
				ats = data.ts;
				if(!data.decoded) {
					current = {buf: getFloat32PcmArray(data.buf), pos: 0, left:data.length};
				}
				else {
					current = {buf: data.buf, pos: 0, left:data.buf.length};
				}
			}
		}
		if(current == null) {
			// このタイミングでnullBufferで埋めてしまう。
			
			target.buf0.set(zeroBuffer.subarray(0, target.left), target.pos);
			target.buf1.set(zeroBuffer.subarray(0, target.left), target.pos);
			prodec = true;
			break;
		}
		else {
			var copyLength = current.left;
			if(current.left > target.left) {
				// 残りデータの方が必要データより多い場合は・・・
				copyLength = target.left;
			}
			var copyBuf = current.buf.subarray(current.pos, copyLength + current.pos);
			current.pos += copyLength;
			current.left -= copyLength;
			if(current.left == 0) {
				current = null;
			}
			target.buf0.set(copyBuf, target.pos);
			target.buf1.set(copyBuf, target.pos);
			target.pos += copyLength;
			target.left -= copyLength;
		}
	} while(target.left > 0);
}

/** 最終更新 */
var lastUpdate = new Date().getTime();
/**
 * 画像の更新
 */
var imageUpdate = function() {
	var img = null;
	var ts = -1;
	while(imageBuffers.length > 0) {
		if(imageBuffers[0].ts > ats) {
			ts = imageBuffers[0].ts;
			break;
		}
		img = imageBuffers.shift().img;
	}
	if(img != null) {
		ctx.drawImage(img, 0, 0);
		document.querySelector("div").innerHTML = "prodec" + prodec + " audio:" + audioBuffers.length + " / video:" + imageBuffers.length + " ats:" + ats + " / vts:" + ts;
		prodec = false;
	}
//	overflow = false;
	if(audioBuffers.length != 0) {
		var length = audioBuffers.length;
		if(length > 3) {
			length = 3;
		}
		for(var i = 0;i < length;i ++) {
			if(!audioBuffers[i].decoded) {
				var data = audioBuffers[i];
				data.decoded = true;
				data.buf = getFloat32PcmArray(data.buf);
				audioBuffers[i] = data;
			}
		}
	}
	// このタイミングでwebsocketにheartBeatのデータをおくっておく必要あり。
	// heartBeatをここで送る必要があるが・・・
	var currentTime = new Date().getTime();
	if(currentTime - lastUpdate > 1000) {
		// 1秒以上たっている場合はなにかの処理をしないとだめ。
		if(!ws) {
		}
		else {
			if(ws.readyState == 1) {
//				ws.send("heartBeat");
			}
		}
		lastUpdate = new Date().getTime();
	}
	// 次のフレーム時にも処理しておきたい。
	requestAnimationFrame(imageUpdate);
};
imageUpdate();

/**
 * 再生開始処理
 */
function Play() {
	if(ws != null) {
		ws.close(); // 前の接続は殺しておく。
	}
	if(osc != null) {
		osc.disconnect(scrproc);
	}
	audioBuffers = [];
	ats = 0; // 初期化しておく。
	imageBuffers = [];
	if(typeof(audioctx.createOscillator) !== "undefined") {
		osc = audioctx.createOscillator();
		osc.frequency.value=440;
		osc.connect(scrproc);
		osc.start(0);
		// 開始するときにrtmpサーバーの接続先を指定して動作させておく。
		var server = document.getElementById("server").value;
		var port   = document.getElementById("port").value;
		var app    = document.getElementById("app").value;
		var stream = document.getElementById("stream").value;
		ws = new WebSocket("ws://" + location.host + "/wts/?host=" + server + "&port=" + port + "&app=" + app + "&stream=" + stream);
		ws.onmessage = onMessage;
	}
}
