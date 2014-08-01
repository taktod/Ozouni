package com.ttProject.ozouni.base;

/**
 * 入力モジュールのインターフェイス
 * @author taktod
 */
public interface IInputModule {
	/**
	 * 中間動作モジュールを設定(直接出力モジュールをいれてもOK)
	 * @param workModule
	 */
	public void setOutputModule(IWorkModule workModule);
	/**
	 * 開始動作
	 */
	public void start() throws Exception;
}
