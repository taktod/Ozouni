// sampleRateの変更を実施したいので、fft(高速フーリエ変換)を実施したいと思う。
// websocket経由でデータをうけとったら、内容を確認して、wavの形にすぐにデコードする必要があるだろう。
// 周波数は、そのときに必要な形になおしておく。でいいと思う。

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
var bufsize = 1024; // 実際のデータとしては、1024ごとに送信挿入する感じ。
// 普通にやると、adpcmをデコードすると、2041samplesになるので、1024だとちと端数入りますね。
// 再生中かどうかフラグ
var play = 0;

// audioContextを初期化します
if(typeof(webkitAudioContext)!=="undefined")
	var audioctx = new webkitAudioContext();
else if(typeof(AudioContext)!=="undefined")
	var audioctx = new AudioContext();

var scrproc = audioctx["createScriptProcessor"](bufsize);
scrproc["onaudioprocess"] = Process;
scrproc["connect"](audioctx.destination);
var osc = null;

// adpcmのデコードに必要なデータ
var imaIndexTable = [
	-1, -1, -1, -1, 2, 4, 6, 8,
	-1, -1, -1, -1, 2, 4, 6, 8
];
var imaStepTable = [
	    7,     8,     9,    10,    11,    12,    13,    14,    16,    17,
	   19,    21,    23,    25,    28,    31,    34,    37,    41,    45,
	   50,    55,    60,    66,    73,    80,    88,    97,   107,   118,
	  130,   143,   157,   173,   190,   209,   230,   253,   279,   307,
	  337,   371,   408,   449,   494,   544,   598,   658,   724,   796,
	  876,   963,  1060,  1166,  1282,  1411,  1552,  1707,  1878,  2066,
	 2272,  2499,  2749,  3024,  3327,  3660,  4026,  4428,  4871,  5358,
	 5894,  6484,  7132,  7845,  8630,  9493, 10442, 11487, 12635, 13899,
	15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
];
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

// バッファに一旦データをためて、処理に回します
// 音声バッファ
var audioBuffers = []; // 処理用の音声バッファ
// 画像バッファ
var imageBuffers = []; // 処理用の映像バッファ

var bufferPos = 0; // 次に読み込むべきbufferの位置データ
// adpcmのデコードに必要なデータ
var predictor = 0; // 音声データの振幅
var index = 0; // 音声データの処理index値
var step = 0;

var flag = true;
var ats = 0;

// 画像を表示するcanvas
var canvas = document.getElementById("target");
var ctx = canvas.getContext("2d");
// websocket経由で対象サーバーに接続する。
var ws = new WebSocket("ws://localhost:8080/123");
//var ws = new WebSocket("ws://192.168.20.155:8080/wts/123");
ws.onopen = function() {
	// 接続したとき(まぁどうでもいい)
	console.log("opened");
};
ws.onclose = function() {
	// 閉じたとき(こっちもどうでもいい)
	console.log("closed");
	end = true;
};

/**
 * websocket経由で取得した、audioBufferからfltのpcmをつくってfloat32Arrayとして応答する
 * @param audioBuffer
 */
function getFloat32PcmArray(audioBuffer) {
	// この部分で処理しておく。
	var size = (audioBuffer.length - 4) * 2 + 1; // 先頭の2byteがpredictor 1byteがfirstIndex 次の1byteは0x00(この部分に関しては圧縮によって変動する予定)
	var result = new Float32Array(size);
	var pos = 0; // 設置するデータの位置
	// はじめのpredictorを取得する。
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
	// ここまできたら処理おわり。
	return result;
}

// データ受け取り処理
ws.onmessage = function(evt) {
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
		case 1: // 1のデータは音声データ
			if(play == 0) {
				return;
			}
//			console.log("snd:" + timestamp);
			var currentAdpcm = uint8ArrayView.subarray(5);
//			audioBuffers.push({ts:timestamp, buf:currentAdpcm});
			// ここでadpcmをpcmに変換してfloatのArrayにしておきたいと思います。
			audioBuffers.push({ts:timestamp, buf:getFloat32PcmArray(currentAdpcm)});
			// 取得した瞬間に、Buffersのデータに変換してやる必要あり。
			break;
		case 4: // 映像データ(適当に再生時に表示しないとだめ)
/*			if(play == 0) {
				return;
			}*/
//			console.log("mov:" + timestamp);
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
var aPos = 0;
var currentBuffer = null; // 現在処理中のbuffer
// ev.outputBufferにwavデータをいれることで音声の再生につながる形になっています。
// ここでデータを急激にいれるためにおかしくなる。(正確にはたまってはすててを繰り返すことでおかしくなってしまう)
function Process(ev) {
	var buf0 = ev.outputBuffer.getChannelData(0);
	var buf1 = ev.outputBuffer.getChannelData(1);
	// このタイミングでaudioBuffersからデータを取り出して、必要なデータをコピーしていけばよい 
	// データが空になったらshiftすればいいのか？
	for(var i = 0;i < bufsize;i ++) {
		if(currentBuffer == null) {
			// データがない
			var data = audioBuffers.shift();
			ats = data.ts;
			currentBuffer = data.buf;
			aPos = 0; // 位置を0から開始にしておく。
		}
		// ここでコピーを実行しておく
		buf0[i] = buf1[i] = currentBuffer[aPos];
		aPos ++; // 位置を１つずらしておく
		if(currentBuffer.length == aPos) {
			currentBuffer = null; // いままでのバッファがなくなったので、破棄しておく。
		}
	}
}

var imageUpdate = function() {
	var img = null;
	while(imageBuffers.length > 0) {
		if(imageBuffers[0].ts > ats) {
			break;
		}
		img = imageBuffers.shift().img;
	}
	if(img != null) {
		ctx.drawImage(img, 0, 0);
	}
	// 次のフレーム時にも処理しておきたい。
	requestAnimationFrame(imageUpdate);
};
imageUpdate();

function Play() {
    if(play) {
        if(osc)
            osc.stop(0);
        play = 0;
    }
    else {
    	console.log("playstart");
//        audioBuffers = [];
        if(typeof(audioctx.createOscillator)!=="undefined") {
            osc = audioctx.createOscillator();
            osc.connect(scrproc);
            osc.start(0);
            console.log("osc started.");
        }
        play = 1;
    }
}
