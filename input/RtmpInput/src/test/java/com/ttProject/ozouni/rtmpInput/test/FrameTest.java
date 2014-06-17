package com.ttProject.ozouni.rtmpInput.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.frame.vp6.type.IntraFrame;
import com.ttProject.ozouni.base.CodecType;

/**
 * frameの確認を実施する
 * @author taktod
 */
public class FrameTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(FrameTest.class);
	@Test
	public void test() throws Exception {
		logger.info(CodecType.getCodecTypeFromFrame(new IntraFrame(null, null, null)));
	}
}
