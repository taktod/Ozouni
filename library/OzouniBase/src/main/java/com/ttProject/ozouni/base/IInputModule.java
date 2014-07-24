package com.ttProject.ozouni.base;

/**
 * 入力モジュールのインターフェイス
 * @author taktod
 */
public interface IInputModule {
	/**
	 * 出力モジュールの設定
	 * @param outputModule
	 */
	public void setOutputModule(IOutputModule outputModule);
	/**
	 * 開始動作
	 */
	public void start() throws Exception;
}
