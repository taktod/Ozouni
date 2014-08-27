package com.ttProject.ozouni.frame;

import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.ozouni.frame.worker.IFrameListener;

/**
 * フレームを読み込むreaderのinterface
 * @author taktod
 */
public interface IFrameReader {
	/**
	 * データの読み込みを開始します。
	 * @param source
	 */
	public void start(IReadChannel source) throws Exception;
	/**
	 * frameListenerを追加します。
	 * @param listener
	 */
	public void setFrameListener(IFrameListener listener);
}
