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

// 処理バッファサイズ
var bufsize = 1024;
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

// adpcmのデコードに必要なデータ
var imaIndexTable = [-1,-1,-1,-1,2,4,6,8,-1,-1,-1,-1,2,4,6,8];
var imaStepTable = [
    7,     8,     9,    10,    11,    12,    13,    14,    16,    17,
   19,    21,    23,    25,    28,    31,    34,    37,    41,    45,
   50,    55,    60,    66,    73,    80,    88,    97,   107,   118,
  130,   143,   157,   173,   190,   209,   230,   253,   279,   307,
  337,   371,   408,   449,   494,   544,   598,   658,   724,   796,
  876,   963,  1060,  1166,  1282,  1411,  1552,  1707,  1878,  2066,
 2272,  2499,  2749,  3024,  3327,  3660,  4026,  4428,  4871,  5358,
 5894,  6484,  7132,  7845,  8630,  9493, 10442, 11487, 12635, 13899,
15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767];
var nextIndex = function(index, nibble) {
	var newIndex = index + imaIndexTable[nibble];
	if(newIndex < 0) {
		return 0;
	}
	else if(newIndex > 88) {
		return 88;
	}
	else {
		return newIndex;
	}
};
var nextPredictor = function(index, nibble, predictor, step) {
	var sign = (nibble & 0x08) == 0x08;
	var delta = nibble & 0x07;
	var diff = step >> 1;
	if((delta & 0x04) == 4) {
		diff += (step << 2);
	}
	if((delta & 0x02) == 2) {
		diff += (step << 1);
	}
	if((delta & 0x01) == 1) {
		diff += step;
	}
	diff >>= 2;
	if(sign) {
		predictor -= diff;
	}
	else {
		predictor += diff;
	}
	// predictorがoverflowする場合は、音声が割れたりする模様
	if(predictor > 32767) {
		return 32767;
	}
	else if(predictor < -32768) {
		return -32768;
	}
	else {
		return predictor;
	}
};

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

/**
 * websocket経由で取得した、audioBufferからfltのpcmをつくってfloat32Arrayとして応答する
 * @param audioBuffer
 */
function getFloat32PcmArray(audioBuffer) {
	// この部分で処理しておく。
	var size = (audioBuffer.length - 4) * 2 + 1; // 先頭の2byteがpredictor 1byteがfirstIndex 次の1byteは0x00(この部分に関しては圧縮によって変動する予定)
	var result = new Float32Array(size);
	var pos = 0; // 設置するデータの位置
	var predictor = (audioBuffer[1] << 8) | audioBuffer[0];
	if(predictor > 32767) {
		predictor = predictor - 0x010000;
	}
	result[pos] = predictor / 65536;
	pos ++;
	var index = audioBuffer[2];
	var step = imaStepTable[index];
	var nibble = 0; // 偏差フラグ値
	for(var bPos = 4;bPos < audioBuffer.length;bPos ++) {
		// high 4bitやっておく
		nibble = ((audioBuffer[bPos] >> 4) & 0x0F);
		index = nextIndex(index, nibble);
		predictor = nextPredictor(index, nibble, predictor, step);
		step = imaStepTable[index];
		result[pos] = predictor / 65536;
		pos ++;
		// low 4bitやっておく
		nibble = (audioBuffer[bPos]) & 0x0F;
		index = nextIndex(index, nibble);
		predictor = nextPredictor(index, nibble, predictor, step);
		step = imaStepTable[index];
		result[pos] = predictor / 65536;
		pos ++;
	}
	return result;
}

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
			audioBuffers.push({ts:timestamp, buf:getFloat32PcmArray(currentAdpcm)});
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
var currentBuffer = null;
/** 現在処理中のbufferの処理データ位置 */
var aPos = 0;
function Process(ev) {
	var buf0 = ev.outputBuffer.getChannelData(0);
	var buf1 = ev.outputBuffer.getChannelData(1);
	// buf0とbuf1にデータを設定すれば、それが再生される
	for(var i = 0;i < bufsize;i ++) {
		if(currentBuffer == null) {
			// データがない
			var data = audioBuffers.shift();
			if(data == null) {
				// データをnullでうめておきたい。
				currentBuffer = null;
			}
			else {
				ats = data.ts;
				currentBuffer = data.buf;
				aPos = 0; // 位置を0から開始にしておく。
			}
		}
		// ここでコピーを実行しておく
		if(currentBuffer == null) {
			buf0[i] = buf1[i] = 0;
		}
		else {
			buf0[i] = buf1[i] = currentBuffer[aPos];
			aPos ++; // 位置を１つずらしておく
			if(currentBuffer.length == aPos) {
				currentBuffer = null; // いままでのバッファがなくなったので、破棄しておく。
			}
		}
	}
}

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
	}
	document.querySelector("div").innerHTML = "audio:" + audioBuffers.length + " / video:" + imageBuffers.length + " ats:" + ats + " / vts:" + ts;
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
	audioBuffers = [];
	imageBuffers = [];
	if(typeof(audioctx.createOscillator) !== "undefined") {
		osc = audioctx.createOscillator();
		osc.connect(scrproc);
		osc.start(0);
		// 開始するときに、websocketと接続する形にしておけばいいかな
		ws = new WebSocket("ws://" + location.host + "/wts/123");
		ws.onmessage = onMessage;
	}
}