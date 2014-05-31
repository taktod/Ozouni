package com.ttProject.ozouni.reportHandler;

import com.ttProject.ozouni.base.ReportData;

/**
 * ehcache経由でレポートデータをやり取りするプログラム
 * @author taktod
 */
public class EhcacheReportHandler implements IReportHandler {
	/** 利用するehcacheのmanager名 */
	private String managerName;
	/** 利用するcache設定名 */
	private String cacheName;
	@Override
	public void reportData(int uid, ReportData data) {
		
	}
	@Override
	public ReportData getData(int uid) {
		return null;
	}
}
