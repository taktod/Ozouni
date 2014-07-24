package com.ttProject.ozouni.dataHandler.server.test;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;

import com.ttProject.ozouni.dataHandler.server.DataClient;
import com.ttProject.ozouni.dataHandler.server.DataServer;

public class ServerTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ServerTest.class);
	private ExecutorService executor = Executors.newCachedThreadPool();
	/**
	 * 動作テスト
	 */
//	@Test
	public void test() throws Exception {
		logger.info("test開始");
		DataServer ds = new DataServer(12345);
		Thread.sleep(10000);
		logger.info("send開始");
		ds.sendData(ChannelBuffers.copiedBuffer("test".getBytes()));
		Thread.sleep(1000);
		logger.info("send開始");
		executor.submit(new Runnable() {
			@Override
			public void run() {
				// クライアントとしてつなぐ(blockするので、別スレッドにしとく。)
				new DataClient("localhost", 12345);
			}
		});
		ds.sendData(ChannelBuffers.copiedBuffer(ByteBuffer.wrap("test".getBytes())));
		Thread.sleep(1000);
		logger.info("send開始");
		ds.sendData(ChannelBuffers.copiedBuffer(ByteBuffer.wrap("test".getBytes())));
		Thread.sleep(1000);
		logger.info("send開始");
		ds.sendData(ChannelBuffers.copiedBuffer(ByteBuffer.wrap("test".getBytes())));
		Thread.sleep(1000);
		ds.close();
		Thread.sleep(1000);
		logger.info("testおわり");
	}
}
