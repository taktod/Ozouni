package com.ttProject.ozouni.base.worker;

import java.nio.ByteBuffer;

import com.ttProject.frame.IAnalyzer;
import com.ttProject.ozouni.base.analyzer.IAnalyzerChecker;
import com.ttProject.ozouni.dataHandler.IDataListener;
import com.ttProject.ozouni.dataHandler.IReceiveDataHandler;

/**
 * フレームの受信を実施するworker
 * 特に必要ないといえばないけど、共通化できそうなものは共通モジュールにいれといた方がいいだろう
 * こちらもframeを扱うものとする
 * @author taktod
 */
public class ReceiveFrameWorker {
	private IReceiveDataHandler receiveDataHandler = null;
	private IDataListener listener = new DataListener();
	private IFrameListener frameListener = null;
	private IAnalyzerChecker analyzerChecker = null;
	private IAnalyzer frameAnalyzer = null;
	/**
	 * 監視を開始する
	 */
	public void start() {
		receiveDataHandler.registerListener(listener);
	}
	/**
	 * 
	 * @author taktod
	 */
	public static class DataListener implements IDataListener {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void receiveData(ByteBuffer buffer) {
			// dataからframeをつくって応答しておきたい。
		}
	}
}
