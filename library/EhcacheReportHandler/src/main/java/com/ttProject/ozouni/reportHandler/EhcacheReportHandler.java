package com.ttProject.ozouni.reportHandler;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.reportHandler.ehcache.DefaultEhcacheFactory;

/**
 * ehcache経由でレポートデータをやり取りするプログラム
 * @author taktod
 */
public class EhcacheReportHandler implements IReportHandler {
	/** 利用するehcacheのmanager名 */
	private String managerName = null;
	/** 利用するcache設定名 */
	private String cacheName = null;
	/** 利用するcache */
	private Cache cache = null;
	/**
	 * cacheの動作状態を確認する。
	 */
	private void checkCache() {
		if(cache != null) {
			return;
		}
		if(managerName != null && cacheName != null) {
			CacheManager manager = CacheManager.getCacheManager(managerName);
			if(manager != null) {
				cache = manager.getCache(cacheName);
				if(cache != null) {
					return;
				}
			}
		}
		setupDefaultCache();
	}
	private void setupDefaultCache() {
		DefaultEhcacheFactory factory = new DefaultEhcacheFactory();
		// managerが存在していない
		cache = factory.createDefaultEhcache();
		managerName = factory.getManagerName();
		cacheName = factory.getCacheName();
	}
	@Override
	public void reportData(int uid, ReportData data) {
		// 接続したときのみイベントを取得したいところだが・・・
	}
	@Override
	public ReportData getData(int uid) {
		return null;
	}
	/** getter and setter */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
