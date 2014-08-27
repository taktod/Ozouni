package com.ttProject.ozouni.work.xuggle.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.frame.IFrame;
import com.ttProject.frame.aac.AacFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.work.XuggleAudioWorkModule;

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
		audioWorkModule.setCodec("CODEC_ID_ADPCM_IMA_WAV");
		audioWorkModule.setWorkModule(new IWorkModule() {
			@Override
			public void setWorkModule(IWorkModule workModule) {
			}
			@Override
			public void pushFrame(IFrame frame, int id) throws Exception {
				logger.info(frame.getCodecType() + " " + frame.getPts() + " / " + frame.getTimebase());
			}
		});
		logger.info("テスト開始");
		AacFrame frame = null;
//		NellymoserFrame nFrame = null;
		for(int i = 0;i < 100;i ++) {
			frame = AacFrame.getMutedFrame(44100, 1, 16);
			frame.setPts(1000 * i);
			frame.setTimebase(1000);
			audioWorkModule.pushFrame(frame, 8);
/*			nFrame = NellymoserFrame.getMutedFrame(22050, 1, 16);
			nFrame.setPts(1000 * i + 500);
			nFrame.setTimebase(1000);
			audioWorkerModule.pushFrame(nFrame, 8);*/
		}
		// 本当はここでexecutorsから処理がおわるまで待機しないと最後まで処理したことにならないけど・・・
	}
}
