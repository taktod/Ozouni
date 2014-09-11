package com.ttProject.ozouni.output.frame.test;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * byteBufferの動作について、調べておく
 * @author taktod
 */
public class BufferTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(BufferTest.class);
	/**
	 * 動作テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("テスト開始");
		byte[] b = new byte[]{0x00, 0x00, 0x00, 0x02};
		ByteBuffer buffer = ByteBuffer.wrap(b);
		logger.info(buffer.hashCode()); // A
		buffer = ByteBuffer.allocate(4);
		buffer.putInt(2);
		logger.info(buffer.hashCode()); // D
		buffer.flip();
		logger.info(buffer.hashCode()); // B
		buffer = ByteBuffer.allocate(8);
		buffer.putShort((short)0);
		buffer.putShort((short)2);
		logger.info(buffer.hashCode()); // E
		buffer.flip();
		logger.info(buffer.hashCode()); // C
		// A,B,Cは同じになる
		// D,Eは同じにはならない
		// というわけでflipして作ったデータが同じなら同じhashCodeになると思ってよさそう。
	}
}
