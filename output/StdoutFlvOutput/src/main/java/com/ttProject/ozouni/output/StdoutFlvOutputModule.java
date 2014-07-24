package com.ttProject.ozouni.output;

import org.apache.log4j.Logger;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IOutputModule;

/**
 * 標準出力としてflvデータを出力するモジュール
 * @author taktod
 */
public class StdoutFlvOutputModule implements IOutputModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(StdoutFlvOutputModule.class);
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		logger.info(frame);
	}
}
