package com.ttProject.ozouni.webm.test;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ttProject.ozouni.input.FrameInputModule;

/**
 * Ozouniの動作でwebm用のframe共有動作を実施するプログラム
 * @author taktod
 */
public class ShareWorkTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ShareWorkTest.class);
	/**
	 * 動作テスト
	 */
//	@Test
	public void shareTest() {
		logger.info("共有開始");
		ConfigurableApplicationContext context = null;
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		FrameInputModule inputModule = context.getBean(FrameInputModule.class);
		inputModule.setTargetId("456");
		try {
			inputModule.start();
		}
		catch(Exception e) {
			logger.error("例外が発生しました。", e);;
		}
		context.close();
	}
}
