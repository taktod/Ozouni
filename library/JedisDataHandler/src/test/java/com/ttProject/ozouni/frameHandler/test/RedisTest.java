package com.ttProject.ozouni.frameHandler.test;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.ozouni.dataHandler.RedisDataHandler;
import com.ttProject.util.HexUtil;

/**
 * redisDataHandler経由でデータのやりとりを実施する動作テスト
 * @author taktod
 */
public class RedisTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(RedisTest.class);
	/**
	 * データの設定動作テスト
	 * @throws Exception
	 */
//	@Test
	public void setData() throws Exception {
		logger.info("setDataTest");
		RedisDataHandler handler = RedisDataHandler.getInstance();
		handler.setId("test");
		ByteBuffer buffer = HexUtil.makeBuffer("abcdef123456");
		handler.pushData(buffer);
		logger.info("setDataTestおわり");
	}
	/**
	 * データqueue設定動作テスト
	 * @throws Exception
	 */
//	@Test
	public void registTest() throws Exception {
		logger.info("registTest");
		RedisDataHandler handler =RedisDataHandler.getInstance();
		handler.setId("test");
		handler.setProcessId("sub");
		logger.info("registTestおわり");
	}
	/**
	 * データの受け取り動作テスト
	 * @throws Exception
	 */
	@Test
	public void getData() throws Exception {
/*		logger.info("getDataTest");
		RedisDataHandler handler = RedisDataHandler.getInstance();
		handler.setId("test");
		handler.setProcessId("sub");
		ByteBuffer buffer = handler.popData();
		if(buffer != null) {
			logger.info(HexUtil.toHex(buffer));
		}
		else {
			logger.info("data is null");
		}
		logger.info("getDataTestおわり");*/
	}
}
