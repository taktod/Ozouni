package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;

/**
 * dataを受け取るlistener
 * @author taktod
 */
public interface IDataListener {
	/**
	 * データを受け取った時の動作
	 * @param buffer
	 */
	public void receiveData(ByteBuffer buffer);
}
