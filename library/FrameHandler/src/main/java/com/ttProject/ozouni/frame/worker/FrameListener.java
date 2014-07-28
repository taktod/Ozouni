package com.ttProject.ozouni.frame.worker;

import java.nio.ByteBuffer;

import com.ttProject.ozouni.dataHandler.IDataListener;

/**
 * receiveDataでうけとったデータをframeとして解釈するリスナー
 * @author taktod
 */
public class FrameListener implements IDataListener {
	@Override
	public void receiveData(ByteBuffer buffer) {

	}
}
