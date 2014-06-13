package com.ttProject.ozouni.reportHandler.test;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.reportHandler.RedisReportHandler;

/**
 * jedisの動作テスト
 * @author taktod
 */
public class JedisTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(JedisTest.class);
	/**
	 * 動作テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("動作スタート");
		ConfigurableApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("test.xml");
			RedisReportHandler handler = (RedisReportHandler) context.getBean("handler");
			handler.reportData("test", new ReportData());
			System.out.println(handler.getData("test"));
		}
		catch(Exception e) {
			logger.error("例外が発生しました。", e);
		}
		finally {
			if(context != null) {
				context.close();
				context = null;
			}
		}
		logger.info("テストおわり");
	}
}
