package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;

/**
 * データを送信するhandler
 * @author taktod
 */
public interface ISendDataHandler {
	/**
	 * データをシステムに送り出す
	 * @param buffer
	 */
	public void pushData(ByteBuffer buffer);
}
