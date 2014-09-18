package com.ttProject.ozouni.reportHandler;

import com.ttProject.ozouni.base.ReportData;

/**
 * 何もしないレポートハンドラー
 * @author taktod
 */
public class NullReportHandler implements IReportHandler {
	@Override
	public ReportData getReportData(String uid) {
		return null;
	}
	@Override
	public void reportData(String uid, ReportData data) {
		
	}
}
