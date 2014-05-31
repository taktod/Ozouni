package com.ttProject.ozouni.reportHandler.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.config.MemoryUnit;

/**
 * ehcacheのデフォルトアクセスをつくるfactory
 * @author taktod
 */
public class DefaultEhcacheFactory {
	/**
	 * ehcacheの設定を簡単につくれるようにするためのfactoryメソッド
	 * @param multicastGroupAddress 接続先共有で利用するUDPのaddress
	 * @param mulcastGroupPort 接続先共有で利用するUDPのport
	 * @param managerName 作成するmanagerName
	 * @param cacheName 作成するcacheName
	 */
	@SuppressWarnings("rawtypes")
	public static void createDefaultEhcache(String multicastGroupAddress, int mulcastGroupPort,
			String managerName, String cacheName) {
		Configuration cacheManagerConfig = new Configuration();
		cacheManagerConfig.setDynamicConfig(false);
		cacheManagerConfig.setMonitoring("off");
		cacheManagerConfig.updateCheck(false);
		cacheManagerConfig.name(managerName);

		FactoryConfiguration<?> peerProviderFactoryConfig = new FactoryConfiguration();
		peerProviderFactoryConfig.setClass("net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory");
		peerProviderFactoryConfig.setProperties("peerDiscovery=automatic, multicastGroupAddress=" + multicastGroupAddress + ",multicastGroupPort=" + mulcastGroupPort);
		cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderFactoryConfig);

		FactoryConfiguration<?> peerListenerFactoryConfig = new FactoryConfiguration();
		peerListenerFactoryConfig.setClass("net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory");
		// ここの値はあとで変更する必要あり。
		cacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerFactoryConfig);
		
		CacheConfiguration cacheConfig = new CacheConfiguration();
		cacheConfig.name(cacheName);
		cacheConfig.maxBytesLocalHeap(1, MemoryUnit.MEGABYTES);

		CacheEventListenerFactoryConfiguration eventListenerFactoryConfig = new CacheEventListenerFactoryConfiguration();
		eventListenerFactoryConfig.setClass("net.sf.ehcache.distribution.RMICacheReplicatorFactory");
		cacheConfig.addCacheEventListenerFactory(eventListenerFactoryConfig);

		cacheManagerConfig.addCache(cacheConfig);
		new CacheManager(cacheManagerConfig); // cacheManagerをつくっておく。
	}
}
