package com.ttProject.ozouni.frame.worker;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAnalyzer;
import com.ttProject.frame.IFrame;
import com.ttProject.frameutil.AnalyzerChecker;
import com.ttProject.nio.channels.ByteReadChannel;
import com.ttProject.ozouni.dataHandler.IDataListener;
import com.ttProject.ozouni.dataHandler.IReceiveDataHandler;
import com.ttProject.ozouni.frame.ShareFrameData;

/**
 * フレームの受信を実施するworker
 * 特に必要ないといえばないけど、共通化できそうなものは共通モジュールにいれといた方がいいだろう
 * こちらもframeを扱うものとする
 * @author taktod
 */
public class ReceiveFrameWorker {
	/** ロガー */
	private Logger logger = Logger.getLogger(ReceiveFrameWorker.class);
	private IReceiveDataHandler receiveDataHandler = null;
	private IDataListener listener = new DataListener();
	private IFrameListener frameListener = null;
	private AnalyzerChecker analyzerChecker = null;
	/** trackId -> analyzer */
	private Map<Integer, IAnalyzer> analyzers = new HashMap<Integer, IAnalyzer>();
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
	public class DataListener implements IDataListener {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void receiveData(ByteBuffer buffer) {
			try {
				// dataからframeをつくって応答しておきたい。
				ShareFrameData frameData = new ShareFrameData(buffer);
				// frameデータがすでに取得したデータと一致するか確認して、しないならAnalyzerを作る必要あり。(でないとframe化できない。ただしサイズ変更とかは加味する必要なし(frame化するだけなので))
				IAnalyzer analyzer = analyzers.get(frameData.getTrackId());
				if(analyzer == null) {
					// analyzerが決定していないので、調整する必要あり。
					analyzer = analyzerChecker.checkAnalyzer(frameData.getCodecType());
					analyzers.put(frameData.getTrackId(), analyzer);
				}
				// あとはframeを取り出してlistenerに渡せばOK
				IFrame frame = analyzer.analyze(new ByteReadChannel(frameData.getFrameData()));
				// frameを渡して完了(とりあえずは・・・)
				frameListener.receiveFrame(frame);
			}
			catch(Exception e) {
				logger.error("データ取得時に例外が発生しました。", e);
			}
		}
	}
}
