package com.ttProject.ozouni.dataHandler;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.ozouni.base.analyzer.IServerNameAnalyzer;
import com.ttProject.ozouni.dataHandler.server.DataServer;

/**
 * サーバーとして他のプロセスにデータを提供する動作
 * @author taktod
 */
public class ServerSendDataHandler implements ISendDataHandler {
	/** ロガー */
	private Logger logger = Logger.getLogger(ServerSendDataHandler.class);
	private final DataServer server;
	private final int port;
	private static final String pid;
	/** server名解決動作 */
	@Autowired
	private IServerNameAnalyzer serverNameAnalyzer;
	static {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		pid = bean.getName().split("@")[0]; // プロセス番号保持
	}
	/**
	 * コンストラクタ
	 */
	public ServerSendDataHandler() throws Exception {
		DataServer server = null;
		// ポート番号候補として、pidから適当につくることにする
		int portNumber = Integer.parseInt(pid) % 1000;
		System.out.println("portNumber:" + portNumber);
		portNumber += 1000;
		for(;portNumber < 65535;portNumber += 1000) {
			System.out.println("portNumber:" + portNumber);
			try {
				server = new DataServer(portNumber);
				break; // break入れないとサーバーが決定しない。
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(portNumber > 65535) {
			logger.fatal("プロセスIDからサーバーポート番号が割り出せませんでした。");
			throw new RuntimeException("サーバーのポート番号が決定しませんでした。");
		}
		this.server = server;
		this.port = portNumber;
	}
	/**
	 * コンストラクタ
	 * @param port
	 */
	public ServerSendDataHandler(int port) {
		server = new DataServer(port);
		this.port = port;
	}
	/**
	 * ポート番号を応答する
	 * @return
	 */
	public int getPort() {
		return port;
	}
	/**
	 * データを送信する
	 */
	@Override
	public void pushData(ByteBuffer buffer) {
		server.sendData(ChannelBuffers.copiedBuffer(buffer));
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		// ここではmethod名を応答するのではなく、keyを応答しておきたいところ。
		// →key応答は別のメソッドにしました。
		return "server";
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		// ここでは、一意に判定するための文字列を応答します。
		// とりあえずserver:[server]:[port]とでもしておこうか
		StringBuilder key = new StringBuilder("server");
		key.append(":").append(serverNameAnalyzer.getServerName()).append(":").append(port);
		return key.toString();
	}
}
