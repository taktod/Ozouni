package com.ttProject.ozouni.frameHandler.test;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.util.HexUtil;

/**
 * bufferの動作について確認するテスト
 * @author taktod
 */
public class BufferTest {
	
	private Logger logger = Logger.getLogger(BufferTest.class);
	/**
	 * 動作テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("bufferの動作テスト");
		ByteBuffer buf = HexUtil.makeBuffer("0001020304050607");
		buf.getInt();
		ByteBuffer buf2 = ByteBuffer.allocate(buf.remaining());
		buf2.put(buf);
		buf2.flip();
		logger.info(HexUtil.toHex(buf2));
	}
}
