package com.ttProject.ozouni.work.xuggle.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.aac.AacFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.xuggle.frameutil.Packetizer;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IAudioSamples.Format;
import com.xuggle.xuggler.IStreamCoder.Direction;

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
	/** エンコーダー */
	private IStreamCoder encoder = null;
	/**
	 * テスト
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		logger.info("テスト開始");
		encoder = IStreamCoder.make(Direction.ENCODING, ICodec.ID.CODEC_ID_MP3);
		encoder.setChannels(1);
		encoder.setSampleRate(44100);
		encoder.setBitRate(96000);
		if(encoder.open(null, null) < 0) {
			throw new Exception("エンコーダーが開けませんでした");
		}
		AacFrame frame = null;
		for(int i = 0;i < 100;i ++) {
			frame = AacFrame.getMutedFrame(44100, 1, 16);
			frame.setPts(1000 * i);
			frame.setTimebase(1000);
			processAudioDecode(frame);
		}
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
				IAudioSamples s = IAudioSamples.make(44100, 1, Format.FMT_S16);
				s.setComplete(true, 44100, 44100, 1, Format.FMT_S16, samples.getPts());
				s.setTimeBase(samples.getTimeBase());
				processAudioEncode(s);
			}
		}
	}
	/**
	 * エンコード実施
	 * @param samples
	 * @throws Exception
	 */
	private void processAudioEncode(IAudioSamples samples) throws Exception {
		IPacket packet = IPacket.make();
		int sampleConsumed = 0;
		while(sampleConsumed < samples.getNumSamples()) {
			int retval = encoder.encodeAudio(packet, samples, sampleConsumed);
			if(retval < 0) {
				throw new Exception("変換失敗");
			}
			sampleConsumed += retval;
			if(packet.isComplete()) {
				logger.info("変換できた");
				logger.info(packet);
			}
		}
	}
}
