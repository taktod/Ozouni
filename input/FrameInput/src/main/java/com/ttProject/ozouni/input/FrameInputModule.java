package com.ttProject.ozouni.input;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.frame.AudioAnalyzer;
import com.ttProject.frame.Frame;
import com.ttProject.frame.IAnalyzer;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.NullFrame;
import com.ttProject.frame.VideoAnalyzer;
import com.ttProject.nio.channels.ByteReadChannel;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IOutputModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.dataHandler.IDataListener;
import com.ttProject.ozouni.dataHandler.IReceiveDataHandler;
import com.ttProject.ozouni.frame.ShareFrameData;
import com.ttProject.ozouni.frame.analyzer.AnalyzerChecker;
import com.ttProject.ozouni.frame.analyzer.IAnalyzerChecker;
import com.ttProject.ozouni.reportHandler.IReportHandler;

/**
 * 共有frameを他のサーバーから受け取るinputModule
 * @author taktod
 */
public class FrameInputModule implements IInputModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FrameInputModule.class);
	/** 出力モジュール */
	private IOutputModule outputModule = null;
	/** データ取得用handler設定 */
	private IReceiveDataHandler receiveDataHandler = null;
	/** 報告データ動作 */
	@Autowired
	private IReportHandler reportHandler;
	/** Analyzerのmap */
	private Map<Integer, IAnalyzer> analyzerMap = new HashMap<Integer, IAnalyzer>();
	/** analyzerがどうなっているか調べる動作 */
	private IAnalyzerChecker analyzerChecker = new AnalyzerChecker();
	/**
	 * 出力モジュールを設定
	 */
	@Override
	public void setOutputModule(IOutputModule outputModule) {
		// このoutputModuleにデータを送りつける必要あり。
		this.outputModule = outputModule;
	}
	/**
	 * データ受信動作を設定
	 * @param receiveDataHandler
	 */
	public void setReceiveDataHandler(IReceiveDataHandler receiveDataHandler) {
		this.receiveDataHandler = receiveDataHandler;
	}
	/**
	 * analyzerCheckerを外部から設定できるようにしておきます。
	 * @param analyzerChecker
	 */
	public void setAnalyzerChecker(IAnalyzerChecker analyzerChecker) {
		this.analyzerChecker = analyzerChecker;
	}
	/**
	 * 開始処理
	 */
	@Override
	public void start() throws Exception {
		logger.info("開始します。");
		// このタイミングでserverClientHandlerを起動してデータを取得するようにしないとだめ
		// サーバーの受けての方にreportDataのkeyをいれてアクセス先をつくらないとだめ、
		// ここから明日やる。
		// とりあえず、アクセスキーをつくっておきたい。
		logger.info(System.getProperty("targetId"));
		ReportData reportData = reportHandler.getReportData(System.getProperty("targetId"));
		if(reportData == null) {
			throw new RuntimeException("接続先が見つかりませんでした。");
		}
		logger.info(reportData);
		receiveDataHandler.registerListener(new IDataListener() {
			@Override
			public void receiveData(ByteBuffer buffer) {
				// ここのところで、bufferからSharedFrameDataを作り直さないとだめ
				// TODO コーデックがかわった場合は、analyzerも書き直す必要がある・・・
				// 一応可能性としては、ないとはいえない。
				// frameの内容がかわったら、自分のデータを削除して、別のプロセスを作り直しておきたいところではある。
				try {
					ShareFrameData shareFrameData = new ShareFrameData(buffer);
					// analyzerをいれます。
					IAnalyzer analyzer = analyzerMap.get(shareFrameData.getTrackId());
					// frameに戻す
					if(analyzer == null) {
						// このframeに対応するanalyzerを取得する必要あり。
						analyzer = analyzerChecker.checkAnalyzer(shareFrameData.getCodecType());
						if(analyzer instanceof AudioAnalyzer) {
							shareFrameData.setupFrameSelector(((AudioAnalyzer) analyzer).getSelector());
						}
						else if(analyzer instanceof VideoAnalyzer){
							shareFrameData.setupFrameSelector(((VideoAnalyzer) analyzer).getSelector());
						}
						else {
							throw new Exception("Analyzerが不明でした。");
						}
						analyzerMap.put(shareFrameData.getTrackId(), analyzer);
					}
					// この部分でframeの値をとれるだけとらないとだめ。
					IFrame frame = null;
					IReadChannel channel = new ByteReadChannel(shareFrameData.getFrameData());
					while((frame = analyzer.analyze(channel)) != null) {
						pushData(frame, shareFrameData);
					}
					frame = analyzer.getRemainFrame();
					if(frame != null && !(frame instanceof NullFrame)) {
						pushData(frame, shareFrameData);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		receiveDataHandler.setKey(reportData.getKey());
		receiveDataHandler.start(); // 起動します。
	}
	private void pushData(IFrame frame, ShareFrameData shareFrameData) throws Exception {
		Frame f = (Frame)frame;
		f.setTimebase(shareFrameData.getTimebase());
		f.setPts(shareFrameData.getPts());
		if(frame instanceof NullFrame) {
			// h264とかでnullFrameになることもある、nullFrameの場合はデータを捨てておきます。
			return;
		}
		// 出力モジュールにデータを明け渡します。
		outputModule.pushFrame(frame, shareFrameData.getTrackId());
	}
}
