package com.ttProject.ozouni.input;

import org.apache.log4j.Logger;

import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IOutputModule;

/**
 * 共有frameを他のサーバーから受け取るinputModule
 * @author taktod
 */
public class FrameInputModule implements IInputModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FrameInputModule.class);
	/**
	 * 出力モジュールを設定
	 */
	@Override
	public void setOutputModule(IOutputModule outputModule) {
		
	}
	/**
	 * 開始処理
	 */
	@Override
	public void start() throws Exception {
		logger.info("開始します。");
		// このタイミングでserverClientHandlerを起動してデータを取得するようにしないとだめ
	}
}
