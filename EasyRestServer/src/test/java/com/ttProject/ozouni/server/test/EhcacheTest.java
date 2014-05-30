/*
 * EasyRestServer(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.server.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * ehcacheの動作テスト
 * @author taktod
 */
public class EhcacheTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(EhcacheTest.class);
	/**
	 * cache参照
	 * @return
	 * @throws Exception
	 */
	public static Cache getEhcacheInstance() throws Exception {
		CacheManager manager = CacheManager.getInstance();
		if(manager.cacheExists("test")) {
			return manager.getCache("test");
		}
		Cache cache = new Cache("test",
				1,
				true,
				false,
				5,
				0);
		manager.addCache(cache);
		return cache;
	}
	@Test
	public void ehcacheTest() throws Exception {
		logger.info("テスト開始");
		Cache cache = getEhcacheInstance();
		logger.info("セットテスト");
		cache.put(new Element("test", "aiueo"));
		logger.info("ゲットテスト");
		Element e = cache.get("test");
		logger.info("aiueoが応答されるはず:" + e.getObjectValue());
		logger.info("expire付きテスト");
		cache.put(new Element("test2", "aiueo2", 2, 5)); // 2:timetoidle(２秒以上アクセスがなければ蒸発する) 5:timetolive(５秒経ったら蒸発する)
		e = cache.get("test2");
		logger.info(e.getObjectValue());
		Thread.sleep(1200);
		e = cache.get("test2");
		logger.info(e.getObjectValue());
		Thread.sleep(1200);
		e = cache.get("test2");
		logger.info(e.getObjectValue());
		Thread.sleep(1200);
		e = cache.get("test2");
		logger.info(e.getObjectValue());
		Thread.sleep(1200);
		e = cache.get("test2");
		logger.info(e.getObjectValue());
		Thread.sleep(1200);
		e = cache.get("test2");
		logger.info(e.getObjectValue());
	}
}
