package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.WebSocket;

/**
 * クライアントの公開インターフェイス
 * @author taktod
 */
public interface IClient extends WebSocket.OnTextMessage, WebSocket.OnBinaryMessage{
	/**
	 * 所属applicationを参照する
	 * @return
	 */
	public IApplication getApplication();
	/**
	 * クライアントIDを参照する
	 * @return
	 */
	public int getClentId();
	/**
	 * propertyを設定する
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, Object value);
	/**
	 * propertyを参照する
	 * @param key
	 * @return
	 */
	public Object getProperty(String key);
	/**
	 * binaryデータを転送する
	 * @param buffer
	 * @throws Exception
	 */
	public void sendMessage(ByteBuffer buffer) throws Exception;
	/**
	 * テキストデータを転送する
	 * @param data
	 * @throws Exception
	 */
	public void sendMessage(String data) throws Exception;
	/**
	 * 強制切断する
	 */
	public void close();
}
