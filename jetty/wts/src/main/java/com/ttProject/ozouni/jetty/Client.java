package com.ttProject.ozouni.jetty;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * websocketのアクセスクライアント
 * @author taktod
 */
public class Client implements IClient {
	/** ロガー */
	private Logger logger = Logger.getLogger(Client.class);
	/** 動作コネクション */
	private WeakReference<Connection> connection = null;
	/** 関連するアプリケーション */
	private final IApplication app;
	/** プロパティー */
	private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
	/**
	 * コンストラクタ
	 * @param app
	 */
	public Client(IApplication app) {
		this.app = app;
	}
	/**
	 * 切断時動作
	 */
	@Override
	public void onClose(int closeCode, String message) {
		System.out.println("切断したよ");
		// 切断時イベント発行
		((Application)app).removeClient(this);
		properties.clear();
	}
	/**
	 * 接続時動作
	 */
	@Override
	public void onOpen(Connection connection) {
		this.connection = new WeakReference<Connection>(connection);
		System.out.println("接続したよ");
		((Application)app).addClient(this);
		close();
		// 接続時イベント発行
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
	@Override
	public IApplication getApplication() {
		return app;
	}
	@Override
	public int getClentId() {
		return hashCode();
	}
	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}
	@Override
	public void sendMessage(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void sendMessage(String data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void close() {
		if(connection != null) {
			Connection conn = connection.get();
			if(conn != null) {
				conn.close(); // 強制的に切断しておく。
			}
		}
	}
}
