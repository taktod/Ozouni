package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 接続にデータを送信するworkModule
 * @author taktod
 */
public class FeederWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FeederWorkModule.class);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		logger.info(frame);
	}
	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void setWorkModule(IWorkModule workModule) {
		
	}
}
