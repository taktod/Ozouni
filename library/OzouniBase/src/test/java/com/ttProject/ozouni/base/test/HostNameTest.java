package com.ttProject.ozouni.base.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.ozouni.base.analyzer.HostNameAnalyzer;
import com.ttProject.ozouni.base.analyzer.IpAddressAnalyzer;

/**
 * ホスト名確認
 * @author taktod
 */
public class HostNameTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(HostNameTest.class);
	@Test
	public void test() throws Exception {
		logger.info(new HostNameAnalyzer().getServerName());
		logger.info(new IpAddressAnalyzer().getServerName());
	}
}
