package com.ttProject.ozouni.base.worker;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.ttProject.frame.IAnalyzer;
import com.ttProject.ozouni.base.CodecType;
import com.ttProject.ozouni.base.ShareFrameData;
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
	private Map<CodecType, IAnalyzer> analyzers = new HashMap<CodecType, IAnalyzer>();
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
			try {
				// dataからframeをつくって応答しておきたい。
				ShareFrameData frameData = new ShareFrameData(buffer);
				// frameデータがすでに取得したデータと一致するか確認して、しないならAnalyzerを作る必要あり。(でないとframe化できない。ただしサイズ変更とかは加味する必要なし(frame化するだけなので))
				// この方式だと、h264のトラックが複数あるとかいうときに動作できませんね。
			}
			catch(Exception e) {
				
			}
		}
	}
}
