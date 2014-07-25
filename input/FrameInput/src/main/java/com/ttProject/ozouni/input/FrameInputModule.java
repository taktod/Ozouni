package com.ttProject.ozouni.input;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IOutputModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.base.ShareFrameData;
import com.ttProject.ozouni.dataHandler.IDataListener;
import com.ttProject.ozouni.dataHandler.IReceiveDataHandler;
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
				try {
					ShareFrameData shareFrameData = new ShareFrameData(buffer);
					logger.info(shareFrameData.getCodecType());
					logger.info(shareFrameData.getTimebase());
					logger.info(shareFrameData.getPts());
				}
				catch(Exception e) {
					; // とりあえず例外は捨てておく
				}
			}
		});
		receiveDataHandler.setKey(reportData.getKey());
		receiveDataHandler.start(); // 起動します。
	}
}
