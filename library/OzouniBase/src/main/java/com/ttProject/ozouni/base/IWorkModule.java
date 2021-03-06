package com.ttProject.ozouni.base;

import com.ttProject.frame.IFrame;

/**
 * 中間処理用のモジュールのインターフェイス
 * @author taktod
 */
public interface IWorkModule {
	/**
	 * 中間動作モジュールを設定(出力モジュールをいれるのが普通だが、workModuleを複数つなげることができるようにもしておきたい。)
	 * @param workModule
	 */
	public void setWorkModule(IWorkModule workModule);
	/**
	 * inputModuleからworkModuleにframeを送る
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	public void pushFrame(IFrame frame, int id) throws Exception;
}
