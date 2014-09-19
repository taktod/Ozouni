package com.ttProject.ozouni.base;

import com.ttProject.ozouni.dataHandler.ISendDataHandler;

/**
 * 出力モジュールのインターフェイス
 * @author taktod
 */
public interface IOutputModule extends IWorkModule {
	/**
	 * データを出力するhandlerを設定する。
	 * @param sendDataHandler
	 */
	public void setSendDataHandler(ISendDataHandler sendDataHandler);
}
