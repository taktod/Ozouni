package com.ttProject.ozouni.input;

import org.apache.log4j.Logger;

import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.input.rtmp.IReceiveWriter;
import com.ttProject.ozouni.input.rtmp.RtmpClient;

/**
 * 
 * @author taktod
 */
public class RtmpInputModule implements IInputModule {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(RtmpInputModule.class);
	/** rtmpClient(固定にしておく) */
	private RtmpClient client = new RtmpClient();
	/** データ処理のwriter */
	private IReceiveWriter writer = null;
	/** 出力モジュール */
	private IWorkModule workModule = null;
	/** objectEncodingの値 */
	private int objectEncoding = -1;
	/**
	 * 出力モジュールを設定する。(bean用)
	 */
	@Override
	public void setOutputModule(IWorkModule workModule) {
		// この出力モジュールをreceiveWriterに紐づけないとだめ・・・面倒だな
		this.workModule = workModule;
	}
	/**
	 * objectEncodingを設定する
	 */
	public void setObjectEncoding(int encoding) {
		this.objectEncoding = encoding;
	}
	/**
	 * frameの書き込みクラスを設定する
	 * @param writer
	 */
	public void setWriterToSave(IReceiveWriter writer) {
		this.writer = writer;
	}
	/**
	 * クライアントoptionを参照する
	 * @return
	 */
	public ClientOptions getClientOptions() {
		return client.getClientOptions();
	}
	/**
	 * 処理を開始する
	 */
	@Override
	public void start() throws Exception {
		// writerと出力モジュールを紐づけておく
		writer.setOutputModule(workModule);
		// optionsの調整
		ClientOptions options = getClientOptions();
		if(options.getLoad() != 1 || options.getClientOptionsList() != null) {
			throw new Exception("マルチアクセスは禁止されています");
		}
		options.setSaveAs("test.flv"); // 入力のダミー
		// objectEncodingが追加されている場合はそっちを優先しておく。
		switch(objectEncoding) {
		case 0:
			options.putParam("objectEncoding", 0.0);
			break;
		case 3:
			options.putParam("objectEncoding", 3.0);
			break;
		default:
			break;
		}
		options.setWriterToSave(writer);
		// 動作開始
		client.connect();
	}
}
