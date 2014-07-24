package com.ttProject.ozouni.entry;

/**
 * 動作開始時のentryインターフェイス
 * @author taktod
 */
public interface IEntry {
	/**
	 * 動作開始時に呼ばれます。
	 * @param args
	 */
	public void start(String[] args) throws Exception;
}