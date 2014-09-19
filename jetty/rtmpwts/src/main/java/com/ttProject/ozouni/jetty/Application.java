package com.ttProject.ozouni.jetty;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.input.RtmpInputModule;
import com.ttProject.ozouni.work.FeederWorkModule;
import com.ttProject.ozouni.work.XuggleAudioWorkModule;
import com.ttProject.ozouni.work.XuggleVideoWorkModule;

/**
 * アプリケーション
 * @author taktod
 * アプリケーションには、timer処理をいれておいて、reportHandlerでデータが取得できなくなったときに、FrameInputModuleによるデータの入手を破棄して、あたらしい接続がでてくるまで待って、でてきたら接続しなおすという動作が必要になりそうです。
 */
public class Application implements IApplication {
	/** 動作ロガー */
	private static Logger logger = Logger.getLogger(Application.class);
	private static ApplicationMonitor appMonitor = new ApplicationMonitor();
	/**
	 * シングルトン取得動作
	 * @param path
	 * @return
	 */
	public static IApplication getInstance(String host, String port, String app, String stream) throws Exception {
		// 今回この部分はpathでわけてありましたが、rtmpのurlで分けた方がよさそうですね。
		return appMonitor.getApplication(host, port, app, stream);
//		return null;
	}
	/**
	 * アプリケーションリスト参照動作
	 * @return
	 */
	public static synchronized List<IApplication> getApplications() {
		return appMonitor.getApplications();
	}

	private final String host;
	private final String port;
	private final String app;
	private final String stream;
	private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
	private Set<IClient> clients = new CopyOnWriteArraySet<IClient>();
	private long expire = 10000L; // 10秒たったらexpireになったものとして扱う(最終ユーザーがアクセスしてから)
	private long lastClientRemoveTime = -1; // クライアントが最後にアクセスしていた時刻保持
	private boolean closed = false;
	private ConfigurableApplicationContext context = null;
	/**
	 * コンストラクタ
	 * @param path
	 */
	protected Application(String host, String port, String app, String stream) throws Exception {
		this.host = host;
		this.port = port;
		this.app = app;
		this.stream = stream;
		// このタイミングでspringのcontextを読み込んでおきたい。
		context = new ClassPathXmlApplicationContext("rtmpdownload.xml");
		logger.info("ここまできたので、rtmpのダウンロード処理を開始したりします。");
		// このタイミングでxmlのデータをロードして、動作しなければいけない感じか？
		logger.info(host + port + app + stream);
		context = new ClassPathXmlApplicationContext("rtmpdownload.xml");
		String[] options = new String[7];
		options[0] = "-host";
		options[1] = host;
		options[2] = "-port";
		options[3] = port;
		options[4] = "-app";
		options[5] = app;
		options[6] = stream;
		RtmpInputModule rtmpInputModule = context.getBean(RtmpInputModule.class);
		ClientOptions clientOptions = rtmpInputModule.getClientOptions();
		if(!clientOptions.parseCli(options)) {
			throw new Exception("アクセスアドレスデータをパースすることができませんでした。");
		}
		IWorkModule feederWorkModule = new FeederWorkModule();
		XuggleAudioWorkModule audioModule = context.getBean(XuggleAudioWorkModule.class);
		audioModule.setWorkModule(feederWorkModule);
		XuggleVideoWorkModule videoModule = context.getBean(XuggleVideoWorkModule.class);
		videoModule.setWorkModule(feederWorkModule);
		rtmpInputModule.start(); // 開始するけど、このままだと、rtmpの転送がおわるまでずっとうごきっぱになってるはず。
		context.close();
			// uniqueIdは、決定できません。
/*			logger.info("ターゲットpath:" + paths[paths.length - 1]);
			context = new AnnotationConfigApplicationContext(AppConfig.class);
			FeederWorkModule workModule = context.getBean(FeederWorkModule.class);
			workModule.setApplication(this);
			FrameInputModule inputModule = context.getBean(FrameInputModule.class);
			inputModule.setTargetId(paths[paths.length - 1]);
			try {
				inputModule.start();
				// TODO 切断された場合の通知がほしいところ
			}
			catch(Exception e) {
			}*/
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
		return ApplicationMonitor.makePath(host, port, app, stream);
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
