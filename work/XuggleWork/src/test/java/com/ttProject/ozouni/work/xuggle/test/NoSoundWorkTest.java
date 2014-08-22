package com.ttProject.ozouni.work.xuggle.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.aac.AacFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.xuggle.frame.Packetizer;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;

/**
 * 無音データでgapがある場合にどうなるか調べておきたい
 * @author taktod
 */
public class NoSoundWorkTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(NoSoundWorkTest.class);
	/** デコーダー */
	private IStreamCoder decoder = null;
	/** frame -> packet化 */
	private Packetizer packetizer = new Packetizer();
	/** xuggleのpacket */
	private IPacket packet = null;
	/**
	 * テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("テスト開始");
		AacFrame frame = AacFrame.getMutedFrame(44100, 1, 16);
		frame.setPts(0);
		frame.setTimebase(1000);
		processAudioDecode(frame);
		frame = AacFrame.getMutedFrame(44100, 1, 16);
		frame.setPts(1000);
		frame.setTimebase(1000);
		processAudioDecode(frame);
	}
	/**
	 * frameデコードを実施する
	 * @param frame
	 * @throws Exception
	 */
	private void processAudioDecode(IAudioFrame frame) throws Exception {
		if(frame instanceof AudioMultiFrame) {
			AudioMultiFrame multiFrame = (AudioMultiFrame)frame;
			for(IAudioFrame audioFrame : multiFrame.getFrameList()) {
				processAudioDecode(audioFrame);
			}
			return;
		}
		decoder = packetizer.getDecoder(frame, decoder);
		if(decoder == null) {
			return; // frameがデコーダーに対応していないものもあるので、その場合は次にまわす
		}
		if(!decoder.isOpen()) {
			if(decoder.open(null, null) < 0) {
				throw new Exception("デコーダーが開けません");
			}
		}
		IPacket pkt = packetizer.getPacket(frame, packet);
		if(pkt == null) {
			return;
		}
		packet = pkt;
		logger.info(packet);
		IAudioSamples samples = IAudioSamples.make(1024, decoder.getChannels());
		int offset = 0;
		while(offset < packet.getSize()) {
			int bytesDecoded = decoder.decodeAudio(samples, packet, offset);
			if(bytesDecoded < 0) {
				throw new Exception("データのデコードに失敗しました。");
			}
			offset += bytesDecoded;
			if(samples.isComplete()) {
				logger.info(samples);
			}
		}
	}
}
