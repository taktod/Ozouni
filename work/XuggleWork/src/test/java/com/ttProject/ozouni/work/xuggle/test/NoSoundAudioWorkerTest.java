package com.ttProject.ozouni.work.xuggle.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.frame.aac.AacFrame;
import com.ttProject.ozouni.work.xuggle.AudioWorkerModule;

/**
 * 音声フレームが定期的にぬけている場合のaudioWorkerの動作テスト
 * @author taktod
 */
public class NoSoundAudioWorkerTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(NoSoundAudioWorkerTest.class);
	/** テスト用のaudioWorkerModule */
	private AudioWorkerModule audioWorkerModule;
	/**
	 * テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		audioWorkerModule = new AudioWorkerModule(8);
		logger.info("テスト開始");
		AacFrame frame = null;
//		NellymoserFrame nFrame = null;
		for(int i = 0;i < 100;i ++) {
			frame = AacFrame.getMutedFrame(44100, 1, 16);
			frame.setPts(1000 * i);
			frame.setTimebase(1000);
			audioWorkerModule.pushFrame(frame, 8);
/*			nFrame = NellymoserFrame.getMutedFrame(22050, 1, 16);
			nFrame.setPts(1000 * i + 500);
			nFrame.setTimebase(1000);
			audioWorkerModule.pushFrame(nFrame, 8);*/
		}
	}
}
