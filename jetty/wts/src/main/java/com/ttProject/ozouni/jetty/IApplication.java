package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * アプリケーションの公開インターフェイス
 * @author taktod
 */
public interface IApplication {
	public Set<IClient> getClientSet();
	public String getPath();
	public void setProperty(String key, Object value);
	public Object getProperty(String key);
	public void sendMessage(ByteBuffer buffer);
	public void sendMessage(String data);
	public void close();
}
