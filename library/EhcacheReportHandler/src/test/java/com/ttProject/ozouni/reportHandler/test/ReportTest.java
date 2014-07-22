package com.ttProject.ozouni.reportHandler.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.reportHandler.EhcacheReportHandler;

public class ReportTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ReportTest.class);
//	@Test
	public void test() throws Exception {
		// reportの内容をうけとって確認する
		EhcacheReportHandler reportHandler = new EhcacheReportHandler();
		while(true) {
			ReportData reportData = reportHandler.getData("1234");
			logger.info(reportData);
			Thread.sleep(1000);
		}
	}
}
