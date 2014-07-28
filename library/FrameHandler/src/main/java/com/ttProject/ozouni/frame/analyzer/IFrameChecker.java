package com.ttProject.ozouni.frame.analyzer;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.CodecType;

/**
 * frameを処理するためのanalyzer
 * @author taktod
 */
public interface IFrameChecker {
	/**
	 * frameからcodecTypeを確認するためのanalyzer
	 * @param frame
	 * @return
	 */
	public CodecType checkCodecType(IFrame frame);
}
