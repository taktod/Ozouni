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
	@SuppressWarnings("rawtypes")
	@Test
	public void test() throws Exception {
		logger.info("テスト開始");
		Configuration cacheManagerConfig = new Configuration();
		cacheManagerConfig.setDynamicConfig(false);
		cacheManagerConfig.setMonitoring("off");
		cacheManagerConfig.updateCheck(false);
		cacheManagerConfig.name("testManager");

		FactoryConfiguration<?> peerProviderFactoryConfig = new FactoryConfiguration();
		peerProviderFactoryConfig.setClass("net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory");
		peerProviderFactoryConfig.setProperties("peerDiscovery=automatic, multicastGroupAddress=230.0.0.1,multicastGroupPort=4446");
		cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderFactoryConfig);

		FactoryConfiguration<?> peerListenerFactoryConfig = new FactoryConfiguration();
		peerListenerFactoryConfig.setClass("net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory");
		// ここの値はあとで変更する必要あり。
		cacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerFactoryConfig);
		
		CacheConfiguration cacheConfig = new CacheConfiguration();
		cacheConfig.name("test");
		cacheConfig.maxBytesLocalHeap(1, MemoryUnit.MEGABYTES);

		CacheEventListenerFactoryConfiguration eventListenerFactoryConfig = new CacheEventListenerFactoryConfiguration();
		eventListenerFactoryConfig.setClass("net.sf.ehcache.distribution.RMICacheReplicatorFactory");
		cacheConfig.addCacheEventListenerFactory(eventListenerFactoryConfig);

		cacheManagerConfig.addCache(cacheConfig);
		CacheManager manager = new CacheManager(cacheManagerConfig);
		Cache cache = manager.getCache("test");
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
