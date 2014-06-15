package com.ttProject.ozouni.base.test;

import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.ozouni.base.worker.SignalWorker;

/**
 * signalWorkerの動作について確認するテスト
 * @author taktod
 */
public class SignalWorkerTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(SignalWorkerTest.class);
	@Test
	public void test() throws Exception {
		logger.info("テスト開始");
		SignalWorker worker = new SignalWorker();
		worker.setSignalExecutor(Executors.newSingleThreadScheduledExecutor());
		Thread.sleep(10000);
		worker.setInterval(2000);
		Thread.sleep(10000);
		logger.info("テスト終わり");
	}
}
