package com.ttProject.ozouni.work.test;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ttProject.ozouni.input.FrameInputModule;

/**
 * jettyの内部で利用する、小さなspringの設定動作テスト
 * signalWorkerとかが多重起動することになるので、ここではある程度調整しておきたい。
 * @author taktod
 */
public class JettyMinTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(JettyMinTest.class);
	/**
	 * 動作テスト
	 * @throws Exception
	 */
//	@Test
	public void test() throws Exception {
		logger.info("動作開始");
		ConfigurableApplicationContext context = null;
//		context = new ClassPathXmlApplicationContext("ozouni.xml");
		context = new AnnotationConfigApplicationContext(AppConfig.class);
//		IServerNameAnalyzer analyzer = context.getBean(IServerNameAnalyzer.class);
//		IReportHandler reportHandler = context.getBean(IReportHandler.class);
//		logger.info(reportHandler);
//		ReportData reportData = reportHandler.getReportData("456");
//		logger.info(reportData);
		FrameInputModule inputModule = context.getBean(FrameInputModule.class);
		inputModule.setTargetId("123");
		inputModule.start();
		Thread.sleep(1000);
		ConfigurableApplicationContext context2 = null;
//		context2 = new ClassPathXmlApplicationContext("ozouni.xml");
		// オブジェクトを共有していることがわかったので、AppConfigは有効っぽいですね。
		context2 = new AnnotationConfigApplicationContext(AppConfig.class);
		Thread.sleep(600000);
		logger.info("動作おわり");
	}
}
