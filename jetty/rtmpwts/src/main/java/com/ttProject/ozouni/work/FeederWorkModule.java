package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * jettyサーバーの内部でデータを送信するworker
 * @author taktod
 */
public class FeederWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FeederWorkModule.class);
	/**
	 * {@inheritDoc}
	 * 使いません
	 */
	@Deprecated
	@Override
	public void setWorkModule(IWorkModule workModule) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(int num) throws Exception {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		// frameデータを受け取ります。
		logger.info(frame);
	}
}
