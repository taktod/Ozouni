package com.ttProject.ozouni.work.ffmpeg.test;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * 文字列のreplace動作について確認しておく
 * @author taktod
 */
public class ReplaceTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ReplaceTest.class);
	/**
	 * 動作テスト
	 */
	@Test
	public void test() {
		logger.info("テスト開始");
		String data = "abc - $\\{pipe}";
		logger.info(data);
		logger.info(data.replaceAll("\\$\\\\", "\\$"));
	}
}
