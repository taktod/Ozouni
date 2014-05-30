package com.ttProject.ozouni.server.test;

import org.junit.Test;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

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
	@Test
	public void test() throws Exception {
		CacheManager manager = new CacheManager("src/test/resources/ehcache.xml");
		Cache cache = manager.getCache("cache");
		cache.put(new Element("test", "aiueo135"));
		while(true) {
			Element e = cache.get("test");
			System.out.println(e.getObjectValue());
			Thread.sleep(1000);
		}
	}
}
