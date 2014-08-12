package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.WebSocket;

/**
 * クライアントの公開インターフェイス
 * @author taktod
 */
public interface IClient extends WebSocket.OnTextMessage, WebSocket.OnBinaryMessage{
	public IApplication getApplication();
	public int getClentId();
	public void setProperty(String key, Object value);
	public Object getProperty(String key);
	public void sendMessage(ByteBuffer buffer);
	public void sendMessage(String data);
	public void close();
}
