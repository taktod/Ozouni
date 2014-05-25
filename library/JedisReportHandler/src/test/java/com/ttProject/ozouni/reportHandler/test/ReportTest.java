package com.ttProject.ozouni.reportHandler.test;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis経由でサーバーの状態をレポートする動作テスト
 * @author taktod
 */
public class ReportTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ReportTest.class);
	/**
	 * 動作テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("テスト実行");
		ConfigurableApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("test.xml");
			JedisPool pool = (JedisPool)context.getBean("jedisPool"); // beanのidを指定してpoolを引き出した。
			logger.info("pool取得に成功しました。");
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.set("test", "1234");
				pool.returnResource(jedis);
			}
			catch(Exception e) {
				pool.returnBrokenResource(jedis);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(context != null) {
				context.close();
			}
		}
/*		ReportData reportData = new ReportData();
		reportData.setFramePts(-1);
		reportData.setHostName("localhost");
		reportData.setLastUpdateTime(System.currentTimeMillis());
		reportData.setMethod(DataShareMethod.ServerDataHandler);
		reportData.setProcessId(13);
		IReportHandler handler = new RedisReportFactory().getHandler();
		handler.reportData(123, reportData);*/
	}
}
