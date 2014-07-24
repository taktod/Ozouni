package com.ttProject.ozouni.base;

import com.ttProject.frame.IFrame;

/**
 * 出力モジュールのインターフェイス
 * @author taktod
 */
public interface IOutputModule {
	/**
	 * inputModuleからoutputModuleにframeを送る
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	public void pushFrame(IFrame frame, int id) throws Exception;
}
