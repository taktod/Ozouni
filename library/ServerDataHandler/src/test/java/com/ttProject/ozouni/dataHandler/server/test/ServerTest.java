package com.ttProject.ozouni.dataHandler.server.test;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.ozouni.dataHandler.IDataListener;
import com.ttProject.ozouni.dataHandler.server.DataClient;
import com.ttProject.ozouni.dataHandler.server.DataServer;

public class ServerTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ServerTest.class);
	/**
	 * 動作テスト
	 */
	@Test
	public void test() throws Exception {
		logger.info("test開始");
		DataServer ds = new DataServer(12345);
		Thread.sleep(1000);
		logger.info("send開始");
		ds.sendData(ByteBuffer.wrap("test".getBytes()));
		Thread.sleep(1000);
		logger.info("send開始");
/*
		executor.submit(new Runnable() {
			@Override
			public void run() {
				// クライアントとしてつなぐ(blockするので、別スレッドにしとく。)
//				new DataClient("localhost", 12345);
			}
		});
*/
		// 非同期接続やったので、exeutorを作る必要がなくなったw
		DataClient dataClient = new DataClient();
		dataClient.connect("localhost", 12345);
		dataClient.addEventListener(new IDataListener() {
			@Override
			public void receiveData(ByteBuffer buffer) {
				logger.info(new String(buffer.array()).intern());
			}
		});
		ds.sendData(ByteBuffer.wrap("test".getBytes()));
		Thread.sleep(1000);
		logger.info("send開始");
		ds.sendData(ByteBuffer.wrap("test".getBytes()));
		Thread.sleep(1000);
		logger.info("send開始");
		ds.sendData(ByteBuffer.wrap("test".getBytes()));
		Thread.sleep(1000);
		dataClient.close();
		ds.close();
		Thread.sleep(1000);
		logger.info("testおわり");
	}
}
