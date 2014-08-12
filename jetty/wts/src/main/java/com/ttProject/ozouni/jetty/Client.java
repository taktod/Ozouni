package com.ttProject.ozouni.jetty;

import org.eclipse.jetty.websocket.WebSocket;

/**
 * websocketのアクセスクライアント
 * @author taktod
 */
public class Client implements WebSocket.OnTextMessage, WebSocket.OnBinaryMessage {
	/**
	 * 切断時動作
	 */
	@Override
	public void onClose(int closeCode, String message) {
		System.out.println("切断したよ");
	}
	/**
	 * 接続時動作
	 */
	@Override
	public void onOpen(Connection connection) {
		System.out.println("接続したよ");
	}
	/**
	 * binaryメッセージをうけとったときの動作(クライアント側から取得することはないはず)
	 */
	@Override
	public void onMessage(byte[] data, int offset, int length) {
		System.out.println("binaryMessageうけとった");
	}
	/**
	 * textメッセージをうけとったときの動作
	 */
	@Override
	public void onMessage(String data) {
		System.out.println("textMessageうけとった:" + data);
	}
}
