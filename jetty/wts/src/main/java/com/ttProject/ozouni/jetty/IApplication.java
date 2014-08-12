package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * アプリケーションの公開インターフェイス
 * @author taktod
 */
public interface IApplication {
	/**
	 * 接続クライアントを参照する。(所属クライアントではないので、注意(接続前クライアントの場合は所属しているけど、ここにでてこないことがありえる))
	 * @return
	 */
	public Set<IClient> getClientSet();
	/**
	 * pathを参照する
	 * @return
	 */
	public String getPath();
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
	 * 強制終了する
	 */
	public void close();
}
