package com.ttProject.ozouni.base;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.dataHandler.ISendDataHandler;

/**
 * 出力モジュールのインターフェイス
 * @author taktod
 */
public interface IOutputModule extends IWorkModule {
	/**
	 * inputModuleからoutputModuleにframeを送る
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception;
	/**
	 * データを出力するhandlerを設定する。
	 * @param sendDataHandler
	 */
	public void setSendDataHandler(ISendDataHandler sendDataHandler);
}
