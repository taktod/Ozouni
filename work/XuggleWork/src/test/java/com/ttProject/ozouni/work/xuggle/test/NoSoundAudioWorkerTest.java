package com.ttProject.ozouni.work.xuggle.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.frame.IFrame;
import com.ttProject.frame.aac.AacFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.work.XuggleAudioWorkModule;
import com.ttProject.util.HexUtil;

/**
 * 音声フレームが定期的にぬけている場合のaudioWorkerの動作テスト
 * @author taktod
 */
public class NoSoundAudioWorkerTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(NoSoundAudioWorkerTest.class);
	/** テスト用のaudioWorkerModule */
	private XuggleAudioWorkModule audioWorkModule;
	/**
	 * テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		audioWorkModule = new XuggleAudioWorkModule();
		audioWorkModule.setId(8);
		audioWorkModule.setCodec("CODEC_ID_NELLYMOSER");
		audioWorkModule.setSampleRate(44100);
		audioWorkModule.setChannels(1);
		audioWorkModule.setBitRate(96000);
		audioWorkModule.setWorkModule(new IWorkModule() {
			@Override
			public void setWorkModule(IWorkModule workModule) {
			}
			@Override
			public void start(int num) throws Exception {
			}
			@Override
			public synchronized void pushFrame(IFrame frame, int id) throws Exception {
				logger.info("ここから");
				logger.info(frame.getCodecType() + " " + frame.getPts() + " / " + frame.getTimebase());
				logger.info(HexUtil.toHex(frame.getData()));
				logger.info("ここまで");
			}
		});
		logger.info("テスト開始");
		AacFrame frame = null;
		for(int i = 0;i < 100;i ++) {
			frame = AacFrame.getMutedFrame(44100, 1, 16);
			frame.setPts(1000 * i);
			frame.setTimebase(1000);
			audioWorkModule.pushFrame(frame, 8);
		}
		// 本当はここでexecutorsから処理がおわるまで待機しないと最後まで処理したことにならないけど・・・
		Thread.sleep(1000);
	}
}
