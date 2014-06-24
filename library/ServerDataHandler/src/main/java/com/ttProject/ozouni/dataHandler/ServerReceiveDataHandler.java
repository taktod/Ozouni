package com.ttProject.ozouni.dataHandler;

import com.ttProject.ozouni.dataHandler.server.DataClient;

/**
 * server動作経由でデータを受け取るhandler
 * @author taktod
 */
public class ServerReceiveDataHandler implements IReceiveDataHandler {
	private String server;
	private int port;
	private DataClient client;
	/**
	 * コンストラクタ
	 * @param server 接続しにいくサーバー
	 * @param port 接続するポート
	 */
	public ServerReceiveDataHandler(String server, int port) {
		this.server = server;
		this.port = port;
		this.client = new DataClient(server, port);
	}
	/**
	 * コンストラクタ
	 */
	public ServerReceiveDataHandler() {
	}
	public String getServer() {
		return server;
	}
	public int getPort() {
		return port;
	}
	@Override
	public void registerListener(IDataListener listener) {
		client.addEventListener(listener);
	}
	@Override
	public boolean unregisterListener(IDataListener listener) {
		return client.removeEventListener(listener);
	}
	@Override
	public void setKey(String key) throws Exception {
		// keyはserver:[server]:[port]となっているので、分割して利用する。
		String[] data = key.split(":");
		if("server".equals(data[0])) {
			throw new Exception("keyが不正です");
		}
		server = data[1];
		port = Integer.parseInt(data[2]);
		this.client = new DataClient(server, port);
	}
}
