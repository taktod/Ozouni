package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

/**
 * アプリケーション
 * @author taktod
 */
public class Application implements IApplication {
	/** 動作ロガー */
	private static Logger logger = Logger.getLogger(Application.class);
	private static ApplicationMonitor appMonitor = new ApplicationMonitor();
	public static IApplication getInstance(String path) {
		return appMonitor.getApplication(path);
	}
	public static synchronized List<IApplication> getApplications() {
		return appMonitor.getApplications();
	}

	private final String path;
	private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
	private Set<IClient> clients = new CopyOnWriteArraySet<IClient>();
	private long expire = 10000L; // 10秒たったらexpireになったものとして扱う(最終ユーザーがアクセスしてから)
	private long lastClientRemoveTime = -1; // クライアントが最後にアクセスしていた時刻保持
	private boolean closed = false;
	/**
	 * コンストラクタ
	 * @param path
	 */
	protected Application(String path) {
		this.path = path;
		// このタイミングでIInputModuleをつかって、データを問い合わせる必要あり。
		String[] paths = path.split("/"); // とりあえず、/123というパスであることを期待したい。
		if(paths.length >= 2) {
//			logger.info("データを取得しなければいけない相手は・・・" + paths[1]);
//			IInputModule frameInputModule = new FrameInputModule();
//			frameInputModule.setWorkModule(null); // workModuleとして、jettyにデータを送るworkModuleをかかないとだめ
			// ここでframeInputModuleをつくって、このアプリ用のInputModuleをひもづけておきたい。
			// で、そのデータを接続しているクライアントに送りつける動作をさせておきたいところ。
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		appMonitor.removeApplication(this);
		closed = true;
		for(IClient client : clients) {
			client.close();
		}
		clients.clear();
		properties.clear();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<IClient> getClientSet() {
		return new HashSet<IClient>(clients);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return path;
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
		// 接続しているclientすべてにメッセージ
		for(IClient client : clients) {
			client.sendMessage(buffer);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(String data) throws Exception {
		// 接続しているclientすべてにメッセージ
		for(IClient client : clients) {
			client.sendMessage(data);
		}
	}
	/**
	 * クライアントを追加
	 * @param client
	 */
	protected void addClient(IClient client) {
		clients.add(client);
	}
	/**
	 * クライアントを削除
	 * @param client
	 */
	protected void removeClient(IClient client) {
		clients.remove(client);
		if(clients.size() == 0) {
			lastClientRemoveTime = System.currentTimeMillis();
		}
	}
	/**
	 * アプリケーションが閉じられているか確認
	 * @return
	 */
	protected boolean isClosed() {
		return closed;
	}
	/**
	 * アプリケーションがexpireしているか確認
	 * @return
	 */
	protected boolean isExpired() {
		if(isClosed()) {
			return true;
		}
		if(lastClientRemoveTime == -1 || System.currentTimeMillis() - lastClientRemoveTime < expire) {
			return false;
		}
		return true;
	}
}
