package com.ttProject.ozouni.server.test;

import java.net.BindException;

import org.junit.Test;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.FactoryConfiguration;

public class EhcacheTest2 {
	public static void main(String[] args) throws Exception {
		CacheManager manager = new CacheManager("src/test/resources/ehcache.xml");
		Cache cache = manager.getCache("cache");
		cache.put(new Element("test", "aiueo"));
		while(true) {
			Element e = cache.get("test");
			System.out.println(e.getObjectValue());
			Thread.sleep(1000);
		}
	}
	@SuppressWarnings("rawtypes")
	@Test
	public void test() throws Exception {
		try {
		    CacheConfiguration fee = new CacheConfiguration("cache", 10) 	       
	 	       .overflowToDisk(false)
	 	       .eternal(true)	        	      
	 	       .diskPersistent(false)
	 	       .memoryStoreEvictionPolicy("LFU");
		    
		    Configuration config = new Configuration(); 
	 		    config.setDynamicConfig(false);
	 		    config.setMonitoring("off");
	 		    config.updateCheck(false);
	 		     FactoryConfiguration<?> factoryConfigforpeerprovider = new FactoryConfiguration();
	 		     factoryConfigforpeerprovider.setClass("net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory");
	 		     factoryConfigforpeerprovider.setProperties("peerDiscovery=automatic, multicastGroupAddress=230.0.0.1,multicastGroupPort=4446");
	 		     
	 		     config.addCacheManagerPeerProviderFactory(factoryConfigforpeerprovider); // Configuration for PeerProvider
	 		    
	 		     FactoryConfiguration<?> factoryConfigforpeerlistener = new FactoryConfiguration();
	 		     factoryConfigforpeerlistener.setClass("net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory");
	 		     factoryConfigforpeerlistener.setProperties("hostName=192.168.11.7,port=53424,socketTimeoutMillis=12000");
	 		     
	 		     config.addCacheManagerPeerListenerFactory(factoryConfigforpeerlistener);
	 		     
	 		    CacheEventListenerFactoryConfiguration factoryConfig = new CacheEventListenerFactoryConfiguration();	
	 		     factoryConfig.setClass("net.sf.ehcache.distribution.RMICacheReplicatorFactory"); 
	 		   fee.addCacheEventListenerFactory(factoryConfig);
	 		     
	 		   CacheManager manager = new CacheManager(config);
	 		   Cache testCache = new Cache(fee);
	 		    
	 		   try {
	 		     manager.addCache(testCache);
	 		     // cacheをつくってaddしようとしたときにわかるので、ここでcacheデータを作り直せばよさそう
	 		     
	 		   }
	 		   catch(Exception e) {
	 			   System.out.println("例外取得:" + e.getMessage());
	 		   }
	 		   finally {
	 			   System.out.println("ここきた");
	 		   }
	 		testCache =     manager.getCache("cache");
//	 		     testCache.put(new Element("test", "aiueo135"));
	 		     int i = 0;
			while(true) {
	 		     testCache.put(new Element("test", "aiueo135" + i));
	 		     i++;
				Element e = testCache.get("test");
				System.out.println(e.getObjectValue());
				Thread.sleep(1000);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
//	@Test
	public void xmltest() throws Exception {
		CacheManager manager = new CacheManager("src/test/resources/ehcache.xml");
		Cache cache = manager.getCache("cache");
		cache.put(new Element("test", "aiueo135"));
		int i = 0;
		while(true) {
			// 接続できてからも設定した方が都合がいいみたいです。
			cache.put(new Element("test", "aiueo135" + i));
			i ++;
			Element e = cache.get("test");
			System.out.println(e.getObjectValue());
			Thread.sleep(1000);
		}
	}
}
