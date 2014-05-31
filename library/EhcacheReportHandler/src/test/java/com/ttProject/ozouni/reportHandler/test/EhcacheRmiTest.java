package com.ttProject.ozouni.reportHandler.test;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.ozouni.reportHandler.ehcache.DefaultEhcacheFactory;

/**
 * ehcacheのrmiによる共有動作のテスト
 * @author taktod
 *
 */
public class EhcacheRmiTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(EhcacheRmiTest.class);
	/**
	 * UDPのbroadcastとRMIでehcacheを共有する動作テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("テスト開始");
		// デフォルトのcacheManagerをつくってみる。
		DefaultEhcacheFactory.createDefaultEhcache("230.0.0.1", 4446, "manager", "test");
		Cache cache = CacheManager.getCacheManager("manager").getCache("test");
		cache.put(new Element("test", "hoge"));
		Element e = cache.get("test");
		logger.info("データ参照テスト:" + e.getObjectValue());
		// 1つのプロセスだけ、iをincrementしてデータの更新、その他のプロセスはデータ参照のままにして、cacheが共有されているか確認すればよし。
		int i = 0;
		while(true) {
			cache.put(new Element("test", "hoge" + (i ++)));
			e = cache.get("test");
			logger.info("データ参照テスト:" + e.getObjectValue());
			Thread.sleep(1000);
		}
//		logger.info("テスト終わり");
	}
}
