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

/**
 * websocket経由で取得した、audioBufferからfltのpcmをつくってfloat32Arrayとして応答する
 * @param audioBuffer
 */
function getFloat32PcmArray(audioBuffer) {
	// この部分で処理しておく。
	var size = (audioBuffer.length - 4) * 2; // 先頭の2byteがpredictor 1byteがfirstIndex 次の1byteは0x00(この部分に関しては圧縮によって変動する予定)
	var result = new Float32Array(size);
	var pos = 0; // 設置するデータの位置
	var predictor = (audioBuffer[1] << 8) | audioBuffer[0];
	if(predictor > 32767) {
		predictor = predictor - 0x010000;
	}
//	result[pos] = predictor / 100000;
//	pos ++;
	var index = audioBuffer[2];
	var step = imaStepTable[index];
	var nibble = 0; // 偏差フラグ値
	for(var bPos = 4;bPos < audioBuffer.length;bPos ++) {
		// high 4bitやっておく
		nibble = ((audioBuffer[bPos] >> 4) & 0x0F);
		index = nextIndex(index, nibble);
		predictor = nextPredictor(index, nibble, predictor, step);
		step = imaStepTable[index];
		result[pos] = predictor / 100000;
		pos ++;
		// low 4bitやっておく
		nibble = (audioBuffer[bPos]) & 0x0F;
		index = nextIndex(index, nibble);
		predictor = nextPredictor(index, nibble, predictor, step);
		step = imaStepTable[index];
		result[pos] = predictor / 100000;
		pos ++;
	}
//	prodec = size;
//	prodec = pos;
	return result;
}

self.addEventListener("message", function(e){
	var data = e.data;
	// ここでadpcmのデコードを実施して、
	self.postMessage({ts:data.ts,buf:getFloat32PcmArray(e.data.buf),decoded:true}); // 応答しておく。
}, false);
