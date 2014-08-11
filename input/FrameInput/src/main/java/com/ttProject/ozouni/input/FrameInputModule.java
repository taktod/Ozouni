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
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.dataHandler.IDataListener;
import com.ttProject.ozouni.dataHandler.IReceiveDataHandler;
import com.ttProject.ozouni.frame.AnalyzerChecker;
import com.ttProject.ozouni.frame.ShareFrameData;
import com.ttProject.ozouni.reportHandler.IReportHandler;

/**
 * 共有frameを他のサーバーから受け取るinputModule
 * @author taktod
 */
public class FrameInputModule implements IInputModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FrameInputModule.class);
	/** 作業モジュール */
	private IWorkModule workModule = null;
	/** データ取得用handler設定 */
	private IReceiveDataHandler receiveDataHandler = null;
	/** 報告データ動作 */
	@Autowired
	private IReportHandler reportHandler;
	/** Analyzerのmap */
	private Map<Integer, IAnalyzer> analyzerMap = new HashMap<Integer, IAnalyzer>();
	/** analyzerがどうなっているか調べる動作 */
	private AnalyzerChecker analyzerChecker = new AnalyzerChecker();
	/** 接続先targetId */
	private String targetId = null;
	/**
	 * コンストラクタ
	 */
	public FrameInputModule() {
		targetId = System.getProperty("targetId");
	}
	/**
	 * 出力モジュールを設定
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		// このoutputModuleにデータを送りつける必要あり。
		this.workModule = workModule;
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
	public void setAnalyzerChecker(AnalyzerChecker analyzerChecker) {
		this.analyzerChecker = analyzerChecker;
	}
	/**
	 * targetIdを変更する(startする前に変更する必要あり)
	 * @param targetId
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	/**
	 * 開始処理
	 */
	@Override
	public void start() throws Exception {
		logger.info("開始します。");
		// このタイミングでserverClientHandlerを起動してデータを取得するようにしないとだめ
		ReportData reportData = reportHandler.getReportData(targetId);
		if(reportData == null) {
			throw new RuntimeException("接続先が見つかりませんでした。");
		}
		receiveDataHandler.registerListener(new IDataListener() {
			@Override
			public void receiveData(ByteBuffer buffer) {
				// ここのところで、bufferからSharedFrameDataを作り直さないとだめ
				// TODO コーデックがかわった場合は、analyzerも書き直す必要がある・・・
				// 一応可能性としては、ないとはいえない。
				// frameの内容がかわったら、自分のデータを削除して、別のプロセスを作り直しておきたいところではある。
				// いままでのデータと違う形になったら、なんとかしないとだめ。
				try {
					ShareFrameData shareFrameData = new ShareFrameData(buffer);
					if(shareFrameData.getCodecType().isAudio()) {
						audioReceiveData(shareFrameData);
					}
					else if(shareFrameData.getCodecType().isVideo()) {
						videoReceiveData(shareFrameData);
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
	/**
	 * 音声データの処理をつづける
	 * @return
	 */
	private void audioReceiveData(ShareFrameData shareFrameData) throws Exception {
		IAnalyzer analyzer = analyzerMap.get(shareFrameData.getTrackId());
		// analyzerがない場合、もしくは、codecTypeが一致しない場合は作り直す必要がある
		if(analyzer == null || analyzer.getCodecType() != shareFrameData.getCodecType()) {
			analyzer = analyzerChecker.checkAnalyzer(shareFrameData.getCodecType());
			// selectorをセットアップしておく
			shareFrameData.setupFrameSelector(((AudioAnalyzer)analyzer).getSelector());
			analyzerMap.put(shareFrameData.getTrackId(), analyzer);
		}
		IFrame frame = null;
		IReadChannel channel = new ByteReadChannel(shareFrameData.getFrameData());
		while((frame = analyzer.analyze(channel)) != null) {
			pushData(frame, shareFrameData);
		}
		frame = analyzer.getRemainFrame();
		if(frame != null) {
			pushData(frame, shareFrameData);
		}
	}
	/**
	 * 映像データの処理をつづける
	 * @param shareFrameData
	 * @return
	 */
	private void videoReceiveData(ShareFrameData shareFrameData) throws Exception {
		IAnalyzer analyzer = analyzerMap.get(shareFrameData.getTrackId());
		if(analyzer == null || analyzer.getCodecType() != shareFrameData.getCodecType()) {
			analyzer = analyzerChecker.checkAnalyzer(shareFrameData.getCodecType());
			shareFrameData.setupFrameSelector(((VideoAnalyzer)analyzer).getSelector());
			analyzerMap.put(shareFrameData.getTrackId(), analyzer);
		}
		IFrame frame = null;
		IReadChannel channel = new ByteReadChannel(shareFrameData.getFrameData());
		while((frame = analyzer.analyze(channel)) != null) {
			pushData(frame, shareFrameData);
		}
		frame = analyzer.getRemainFrame();
		if(frame != null) {
			pushData(frame, shareFrameData);
		}
	}
	/**
	 * 実際の送信処理
	 * @param frame
	 * @param shareFrameData
	 * @throws Exception
	 */
	private void pushData(IFrame frame, ShareFrameData shareFrameData) throws Exception {
		if(frame instanceof NullFrame) {
			// h264とかでnullFrameになることもある、nullFrameの場合はデータを捨てておきます。
			return;
		}
		Frame f = (Frame)frame;
		f.setTimebase(shareFrameData.getTimebase());
		f.setPts(shareFrameData.getPts());
		// 出力モジュールにデータを明け渡します。
		workModule.pushFrame(frame, shareFrameData.getTrackId());
	}
}
