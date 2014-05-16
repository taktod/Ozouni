package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;

/**
 * dataHandlerのインターフェイス
 * @author taktod
 */
public interface IDataHandler {
	/**
	 * データをシステムに送り出す
	 * @param buffer
	 */
	public void pushData(ByteBuffer buffer);
	/**
	 * データ受け取りリスナーを設定する
	 * @param listener
	 */
	public void registerDataListener(IDataListener listener);
}
