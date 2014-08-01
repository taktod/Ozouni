package com.ttProject.ozouni.base;

import com.ttProject.frame.IFrame;

/**
 * 中間処理用のモジュールのインターフェイス
 * @author taktod
 */
public interface IWorkModule {
	/**
	 * inputModuleからworkModuleにframeを送る
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	public void pushFrame(IFrame frame, int id) throws Exception;
}
