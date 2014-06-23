package com.ttProject.ozouni.dataHandler;

import com.ttProject.ozouni.dataHandler.server.DataClient;

/**
 * server動作経由でデータを受け取るhandler
 * @author taktod
 */
public class ServerReceiveDataHandler implements IReceiveDataHandler {
	private final String server;
	private final int port;
	private final DataClient client;
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
	public void setKey(String key) {
		
	}
}
