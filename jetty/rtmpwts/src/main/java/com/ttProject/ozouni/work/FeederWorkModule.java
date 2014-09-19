package com.ttProject.ozouni.work;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.ttProject.frame.IFrame;
import com.ttProject.frame.adpcmimawav.AdpcmImaWavFrame;
import com.ttProject.frame.mjpeg.MjpegFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.jetty.IApplication;
import com.ttProject.unit.extra.BitConnector;
import com.ttProject.unit.extra.bit.Bit32;
import com.ttProject.unit.extra.bit.Bit8;
import com.ttProject.util.BufferUtil;

/**
 * jettyサーバーの内部でデータを送信するworker
 * @author taktod
 */
public class FeederWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FeederWorkModule.class);
	// 送信先のapplicationについて保持しておけば良い感じかね？
	private final IApplication app;
	/**
	 * コンストラクタ
	 * @param app
	 */
	public FeederWorkModule(IApplication app) {
		this.app = app;
	}
	/**
	 * {@inheritDoc}
	 * 使いません
	 */
	@Deprecated
	@Override
	public void setWorkModule(IWorkModule workModule) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		// フレームをうけいれた場合の動作
		Bit32 timestamp = new Bit32();
		Bit8 type = new Bit8();
		long pts = 1000L * frame.getPts() / frame.getTimebase();
		timestamp.setLong(pts); // 端数部分は抜け落ちるけど、とりあえず仕方ない
		if(frame instanceof AdpcmImaWavFrame) {
			// 音声
			AdpcmImaWavFrame audioFrame = (AdpcmImaWavFrame)frame;
			switch(audioFrame.getSampleRate()) {
			case 44100:
				type.set(0);
				break;
			case 22050:
				type.set(1);
				break;
			case 11025:
				type.set(2);
				break;
			case 5512:
				type.set(3);
				break;
			default:
				return;
			}
		}
		else if(frame instanceof MjpegFrame) {
			// 映像
			type.set(4);
		}
		else {
			logger.error("想定外のフレームを取得しました。:" + frame.getClass());
//			throw new RuntimeException("想定外のフレームを取得しました");
			return;
		}
		logger.info(frame.getClass() + ":" + pts);
		BitConnector connector = new BitConnector();
		ByteBuffer sendData = BufferUtil.connect(
				connector.connect(timestamp, type),
				frame.getData()
		);
		app.sendMessage(sendData);
	}
}