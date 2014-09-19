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
		logger.info("クライアントつくりました。:" + app);
		this.app = app;
	}
	/**
	 * 切断時動作
	 */
	@Override
	public void onClose(int closeCode, String message) {
		// 切断時イベント発行
		((Application)app).removeClient(this);
		properties.clear();
	}
	/**
	 * 接続時動作
	 */
	@Override
	public void onOpen(Connection connection) {
		logger.info("接続してるよん");
		if(((Application)app).isClosed()) { // なんらかの原因でapplicationがすでに終了済みだったら
			logger.info("アプリケーションがとじてました。なんで？");
			connection.close(); // 強制切断しておわらせる
			return;
		}
		logger.info("ここまでこれました。");
		this.connection = new WeakReference<Connection>(connection);
		((Application)app).addClient(this);
		// 接続時イベント発行
	}
	/**
	 * binaryメッセージをうけとったときの動作(クライアント側から取得することはないはず)
	 */
	@Override
	public void onMessage(byte[] data, int offset, int length) {
		logger.info("binaryメッセージを取得");
	}
	/**
	 * textメッセージをうけとったときの動作
	 */
	@Override
	public void onMessage(String data) {
		logger.info("textメッセージを取得:" + data);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IApplication getApplication() {
		return app;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getClentId() {
		return hashCode();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(ByteBuffer buffer) throws Exception {
		Connection conn = getConnection();
		if(conn != null) {
			int length = buffer.remaining();
			byte[] data = buffer.array();
			conn.sendMessage(data, 0, length);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(String data) throws Exception {
		Connection conn = getConnection();
		if(conn != null) {
			conn.sendMessage(data);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		Connection conn = getConnection();
		if(conn != null) {
			conn.close(); // 強制的に切断しておく。
		}
	}
	/**
	 * 接続オブジェクトを参照する
	 */
	private Connection getConnection() {
		if(connection != null) {
			Connection conn = connection.get();
			return conn;
		}
		return null;
	}
}
