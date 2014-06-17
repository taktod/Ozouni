package com.ttProject.ozouni.dataHandler;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;

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
		portNumber += 1000;
		for(;portNumber < 65535;portNumber += 1000) {
			try {
				server = new DataServer(portNumber);
			}
			catch(Exception e) {
				;
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
	@Override
	public String getMethod() {
		return "server";
	}
}
