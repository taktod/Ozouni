package com.ttProject.ozouni.output.flv.model.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * listの動作テスト
 * @author taktod
 */
public class ListTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(ListTest.class);
	@Test
	public void test() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("test1");
		data.add("test2");
		data.add("test3");
		data.add("test4");
		data.add("test5");
		data.add("test6");
		data.add("test7");
		logger.info(data);
	}
}
