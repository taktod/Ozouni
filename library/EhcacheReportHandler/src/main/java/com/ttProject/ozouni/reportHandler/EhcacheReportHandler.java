package com.ttProject.ozouni.reportHandler;

import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.reportHandler.ehcache.DefaultEhcacheFactory;

/**
 * ehcache経由でレポートデータをやり取りするプログラム
 * @author taktod
 */
public class EhcacheReportHandler implements IReportHandler {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(EhcacheReportHandler.class);
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
		// 動作が決定していない場合は、つくっておく。
		DefaultEhcacheFactory factory = new DefaultEhcacheFactory();
		// managerが存在していない
		cache = factory.createDefaultEhcache();
		managerName = factory.getManagerName();
		cacheName = factory.getCacheName();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reportData(String uid, ReportData data) {
		checkCache();
//		logger.info("レポートを実施します。:" + uid);
		// 接続したときのみイベントを取得したいところだが・・・
		cache.put(new Element(uid, data));
		// uid -> dataという形にしておく。
		// dataはjsonで持たせておこうか・・・
//		Element e = cache.get(uid);
//		logger.info(e.getObjectValue());
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportData getData(String uid) {
		checkCache();
		Element e = cache.get(uid);
		if(e == null || e.getObjectValue() == null) {
			return null;
		}
		if(e.getObjectValue() instanceof ReportData) {
			return (ReportData)e.getObjectValue();
		}
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
