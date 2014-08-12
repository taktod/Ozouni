package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	private static Map<String, IApplication> applications = new ConcurrentHashMap<String, IApplication>();
	private Set<IClient> clients = new CopyOnWriteArraySet<IClient>();
	private final String path;
	private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
	private long expire = 10000L; // 10秒たったらexpireになったものとして扱う(最終ユーザーがアクセスしてから)
	private long lastClientRemoveTime = -1; // クライアントが最後にアクセスしていた時刻保持
	public static IApplication getInstance(String path) {
		IApplication app = applications.get(path);
		if(app == null) {
			app = new Application(path);
			applications.put(path, app);
			// アプリケーション作成時イベント
		}
		return app;
	}
	public static synchronized void removeApplication(IApplication app) {
		applications.remove(app.getPath());
	}
	public static synchronized List<IApplication> getApplications() {
		List<IApplication> result = new ArrayList<IApplication>();
		for(Entry<String, IApplication> entry : applications.entrySet()) {
			result.add(entry.getValue());
		}
		return result;
	}
	/**
	 * コンストラクタ
	 * @param path
	 */
	private Application(String path) {
		this.path = path;
	}
	@Override
	public void close() {
		properties.clear();
	}
	@Override
	public Set<IClient> getClientSet() {
		return new HashSet<IClient>(clients);
	}
	@Override
	public String getPath() {
		return path;
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
		// 接続しているclientすべてにメッセージ
	}
	@Override
	public void sendMessage(String data) {
		// 接続しているclientすべてにメッセージ
	}
	public void addClient(IClient client) {
		clients.add(client);
	}
	public void removeClient(IClient client) {
		clients.remove(client);
		if(clients.size() == 0) {
			lastClientRemoveTime = System.currentTimeMillis();
		}
	}
}
