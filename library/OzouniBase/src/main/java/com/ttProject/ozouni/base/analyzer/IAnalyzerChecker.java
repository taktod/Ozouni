package com.ttProject.ozouni.base.analyzer;

import com.ttProject.frame.IAnalyzer;
import com.ttProject.ozouni.base.CodecType;

/**
 * SharedFrameDataから対象となるCheckerを決定します
 * @author taktod
 */
public interface IAnalyzerChecker {
	/**
	 * frameに戻す時に利用するanalyzerを設定
	 * @param codecType
	 * @return
	 */
	public IAnalyzer checkAnalyzer(CodecType codecType);
}
