package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;

/**
 * サーバーとして他のプロセスにデータを提供する動作
 * @author taktod
 */
public class ServerSendDataHandler implements ISendDataHandler {
	/**
	 * コンストラクタ
	 */
	public ServerSendDataHandler() {
		// なにもないときは適当なサーバーをたてて動作させる。
	}
	/**
	 * コンストラクタ
	 * @param port
	 */
	public ServerSendDataHandler(int port) {
		// 指定ポート番号でサーバーをたてておく。
	}
	@Override
	public void pushData(ByteBuffer buffer) {

	}
}
