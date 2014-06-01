package com.ttProject.ozouni.reportHandler.test;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.NotificationScope;

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
		Cache cache = new DefaultEhcacheFactory().createDefaultEhcache();
		cache.getCacheEventNotificationService().registerListener(new TestListener(), NotificationScope.REMOTE);
		
		cache.put(new Element("test", "hoge"));
		Element e = cache.get("test");
		logger.info("データ参照テスト:" + e.getObjectValue());
		// 1つのプロセスだけ、iをincrementしてデータの更新、その他のプロセスはデータ参照のままにして、cacheが共有されているか確認すればよし。
		int i = 0;
		while(true) {
			i ++;
///			if(i == 15) {
//				cache.put(new Element("test", "hoge"));
//			}
			cache.put(new Element("test", "hoge" + i));
			e = cache.get("test");
			logger.info("データ参照テスト:" + e.getObjectValue());
			Thread.sleep(1000);
		}
//		logger.info("テスト終わり");
	}
	public static class TestListener implements CacheEventListener {
		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
		@Override
		public void notifyElementRemoved(Ehcache cache, Element element)
				throws CacheException {
			// TODO Auto-generated method stub
		}

		@Override
		public void notifyElementPut(Ehcache cache, Element element)
				throws CacheException {
			// TODO Auto-generated method stub
			System.out.println("put");
			
		}

		@Override
		public void notifyElementUpdated(Ehcache cache, Element element)
				throws CacheException {
			// TODO Auto-generated method stub
			System.out.println("update"); // これがくるっぽいですね
		}

		@Override
		public void notifyElementExpired(Ehcache cache, Element element) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyElementEvicted(Ehcache cache, Element element) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyRemoveAll(Ehcache cache) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
	};
}
