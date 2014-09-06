package com.ttProject.ozouni.work;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 接続にデータを送信するworkModule
 * @author taktod
 */
public class FeederWorkModule implements IWorkModule {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
	}
	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void setWorkModule(IWorkModule workModule) {
		
	}
}
