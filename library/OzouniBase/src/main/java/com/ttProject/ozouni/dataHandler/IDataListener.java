package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;

/**
 * frameを受け取るlistener
 * @author taktod
 */
public interface IDataListener {
	/** データをうけとったときの動作 */
	public void receiveData(ByteBuffer buffer);
}
