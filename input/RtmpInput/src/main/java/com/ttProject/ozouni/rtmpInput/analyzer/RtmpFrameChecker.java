package com.ttProject.ozouni.rtmpInput.analyzer;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.CodecType;
import com.ttProject.ozouni.base.analyzer.IFrameChecker;

/**
 * rtmpの転送データのframeを解析する。
 * @author taktod
 *
 */
public class RtmpFrameChecker implements IFrameChecker {
	@Override
	public CodecType checkCodecType(IFrame frame) {
		return null;
	}
}
