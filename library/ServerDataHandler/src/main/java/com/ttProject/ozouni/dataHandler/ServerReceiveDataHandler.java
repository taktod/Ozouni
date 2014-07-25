package com.ttProject.ozouni.dataHandler;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ttProject.ozouni.dataHandler.server.DataClient;

/**
 * server動作経由でデータを受け取るhandler
 * @author taktod
 */
public class ServerReceiveDataHandler implements IReceiveDataHandler {
	/** ロガー */
	private Logger logger = Logger.getLogger(ServerReceiveDataHandler.class);
	private String server;
	private int port;
	private DataClient client;
	// ここでlistenerを設定しておいて、開始時にclientに紐づける形にしておく。
	private Set<IDataListener> listeners = new HashSet<IDataListener>();
	/**
	 * コンストラクタ
	 * @param server 接続しにいくサーバー
	 * @param port 接続するポート
	 */
	public ServerReceiveDataHandler(String server, int port) {
		this.server = server;
		this.port = port;
	}
	public ServerReceiveDataHandler(String key) {
		// keyからサーバーデータを取り出す必要がある。
	}
	/**
	 * コンストラクタ
	 */
	public ServerReceiveDataHandler() {
		// なにもないのをいれてから、keyをいれて動作開始しないとだめ。
	}
	public String getServer() {
		return server;
	}
	public int getPort() {
		return port;
	}
	@Override
	public void registerListener(IDataListener listener) {
		listeners.add(listener);
	}
	@Override
	public boolean unregisterListener(IDataListener listener) {
		return listeners.remove(listener);
	}
	@Override
	public void setKey(String key) throws Exception {
		// keyはserver:[server]:[port]となっているので、分割して利用する。
		String[] data = key.split(":");
		logger.info(data[0]);
		if(!"server".equals(data[0])) {
			throw new Exception("keyが不正です");
		}
		server = data[1];
		port = Integer.parseInt(data[2]);
	}
	/**
	 * 開始トリガー
	 * @throws Exception
	 */
	@Override
	public void start() throws Exception {
		logger.info("動作を開始します。");
		client = new DataClient();
		for(IDataListener listener : listeners) {
			client.addEventListener(listener);
		}
		client.connect(server, port);
		logger.info("終わるまで待機します。");
		client.waitForClose();
	}
}
