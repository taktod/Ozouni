package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 変換動作モジュール
 * @author taktod
 *
 */
public class ConvertWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(ConvertWorkModule.class);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		logger.info(frame);
		// とりあえずここまでデータをもってくることはできた。
	}
}
