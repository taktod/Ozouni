package com.ttProject.ozouni.server.test;

import java.net.InetAddress;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * ehcacheの動作テスト
 */
public class EhcacheTest3 {
	/** ロガー */
	private Logger logger = Logger.getLogger(EhcacheTest3.class);
	@Test
	public void test() throws Exception {
		try {
			logger.info("test開始");
			logger.info(InetAddress.getLocalHost().getHostAddress());
			Configuration cacheManagerConfig = new Configuration();
			CacheConfiguration cacheConfig = new CacheConfiguration();
			cacheConfig.name("test");
			cacheConfig.maxBytesLocalHeap(16, MemoryUnit.MEGABYTES);
			cacheConfig.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
			cacheManagerConfig.addCache(cacheConfig);
			CacheManager cacheManager = new CacheManager(cacheManagerConfig);
			Ehcache cache = cacheManager.getEhcache("test");
			cache.put(new Element("test", "hoge"));
			Element e = cache.get("test");
			logger.info(e.getObjectValue());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
