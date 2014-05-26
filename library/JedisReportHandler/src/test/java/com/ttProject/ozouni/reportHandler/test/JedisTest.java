package com.ttProject.ozouni.reportHandler.test;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

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
			StringRedisTemplate template = (StringRedisTemplate) context.getBean("stringRedisTemplate");
			template.opsForHash().put("a", "b", "c");
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
