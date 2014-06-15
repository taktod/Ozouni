package com.ttProject.ozouni.base.worker;

import java.nio.ByteBuffer;

import com.ttProject.ozouni.dataHandler.IDataListener;

/**
 * receiveDataでうけとったデータをframeとして解釈するリスナー
 * @author taktod
 */
public abstract class FrameListener implements IDataListener {
	@Override
	public void receiveData(ByteBuffer buffer) {

	}
}
