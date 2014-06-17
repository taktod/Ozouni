package com.ttProject.ozouni.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ttProject.frame.IFrame;

/**
 * やりとりするフレームのcodecID
 * @author taktod
 */
public enum CodecType {
	AAC(0, true),
	ADPCM_IMA_WAV(1, true),
	ADPCM_SWF(2, true),
	MP3(6, true),
	NELLYMOSER(7, true),
	SPEEX(8, true),
	VORBIS(10, true),
	OPUS(15, true),
	FLV1(3, false),
	H264(4, false),
	MJPEG(5, false),
	THEORA(9, false),
	VP6(11, false),
	VP8(12, false),
	VP9(13, false),
	H265(14, false);
	private final int value;
	private final boolean audioFlg;
	/**
	 * コンストラクタ
	 * @param value
	 * @param audioFlg
	 */
	private CodecType(int value, boolean audioFlg) {
		this.value = value;
		this.audioFlg = audioFlg;
	};
	public boolean isAudio() {
		return audioFlg;
	}
	public int getValue() {
		return value;
	}
	/**
	 * 番号からコーデック値を応答する
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public static CodecType getCodecType(int num) throws Exception {
		for(CodecType type : values()) {
			if(type.getValue() == num) {
				return type;
			}
		}
		throw new Exception("未対応のID番号です");
	}
	public static CodecType getCodecTypeFromFrame(IFrame frame) throws Exception {
		Pattern pattern = Pattern.compile(".*frame\\.([^\\.]+)\\..*");
		Matcher m = pattern.matcher(frame.getClass().getName());
		if(m.matches() && m.groupCount() == 1) {
			String typeName = m.group(1).toUpperCase();
			for(CodecType type : values()) {
				switch(type) {
				case ADPCM_IMA_WAV:
					if(typeName.equals("ADPCMIMAWAV")) {
						return type;
					}
					break;
				case ADPCM_SWF:
					if(typeName.equals("ADPCMSWF")) {
						return type;
					}
					break;
				default:
					if(typeName.equals(type.toString())) {
						return type;
					}
					break;
				}
			}
			System.out.println(typeName);
		}
		// TODO frameのクラスから割り出す必要があるか？
		throw new Exception("未対応のframeです");
	}
}
