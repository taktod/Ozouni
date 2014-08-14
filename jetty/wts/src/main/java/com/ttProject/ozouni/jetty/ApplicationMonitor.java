package com.ttProject.ozouni.jetty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * アプリケーションの動作を監視する内部モニター
 * @author taktod
 */
public class ApplicationMonitor {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(ApplicationMonitor.class);
	/** 動作アプリケーション保持 */
	private Map<String, IApplication> applications = new ConcurrentHashMap<String, IApplication>();
	/**
	 * コンストラクタ
	 */
	public ApplicationMonitor() {
		// 静的初期化でtime動作をつくっておく。
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread t = new Thread(runnable);
				t.setName("ApplicationMonitorThread");
				t.setDaemon(true);
				return t;
			}
		});
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// ここで30秒に一回監視していく感じにしておけばいい感じになると思われ
				for(IApplication app : applications.values()) {
					Application application = (Application)app;
					if(application.isExpired()) {
						removeApplication(app);
					}
				}
			}
		}, 0, 30, TimeUnit.SECONDS);
	}
	/**
	 * 特定のパスに対応するアプリケーションを参照する(なかったら作る)
	 * @param path
	 * @return
	 */
	public IApplication getApplication(String path) {
		IApplication app = applications.get(path);
		if(app == null) {
			app = new Application(path);
			applications.put(path, app);
			// アプリケーション作成時イベント
		}
		return app;
	}
	/**
	 * アプリケーションを撤去する。
	 * @param app
	 */
	protected void removeApplication(IApplication app) {
		applications.remove(app.getPath());
	}
	/**
	 * アプリケーションリストを参照する
	 * @return
	 */
	public List<IApplication> getApplications() {
		List<IApplication> result = new ArrayList<IApplication>();
		for(Entry<String, IApplication> entry : applications.entrySet()) {
			result.add(entry.getValue());
		}
		return result;
	}
}
