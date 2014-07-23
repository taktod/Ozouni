package com.ttProject.ozouni.rtmpInput.analyzer;

import org.apache.log4j.Logger;

import com.ttProject.frame.IFrame;
import com.ttProject.frame.aac.AacFrame;
import com.ttProject.frame.adpcmswf.AdpcmswfFrame;
import com.ttProject.frame.flv1.Flv1Frame;
import com.ttProject.frame.h264.H264Frame;
import com.ttProject.frame.mp3.Mp3Frame;
import com.ttProject.frame.nellymoser.NellymoserFrame;
import com.ttProject.frame.speex.SpeexFrame;
import com.ttProject.frame.vp6.Vp6Frame;
import com.ttProject.ozouni.base.CodecType;
import com.ttProject.ozouni.base.analyzer.IFrameChecker;

/**
 * rtmpの転送データのframeを解析する。
 * @author taktod
 *
 */
public class RtmpFrameChecker implements IFrameChecker {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(RtmpFrameChecker.class);
	@Override
	public CodecType checkCodecType(IFrame frame) {
		if(frame instanceof AacFrame) {
			return CodecType.AAC;
		}
		else if(frame instanceof Mp3Frame) {
			return CodecType.MP3;
		}
		else if(frame instanceof NellymoserFrame) {
			return CodecType.NELLYMOSER;
		}
		else if(frame instanceof SpeexFrame) {
			return CodecType.SPEEX;
		}
		else if(frame instanceof AdpcmswfFrame) {
			return CodecType.ADPCM_SWF;
		}
		else if(frame instanceof Flv1Frame) {
			return CodecType.FLV1;
		}
		else if(frame instanceof H264Frame) {
			return CodecType.H264;
		}
		else if(frame instanceof Vp6Frame) {
			return CodecType.VP6;
		}
		return null;
	}
}
