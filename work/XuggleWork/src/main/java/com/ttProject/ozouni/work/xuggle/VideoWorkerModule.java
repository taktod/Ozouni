package com.ttProject.ozouni.work.xuggle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.xuggle.frameutil.Depacketizer;
import com.ttProject.xuggle.frameutil.Packetizer;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IStreamCoder.Direction;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;

/**
 * 映像の動作について、実行しておく
 * @author taktod
 */
public class VideoWorkerModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(VideoWorkerModule.class);
	/** 経過pts */
	private long passedPts = 0;
	/** 処理するID */
	private final int id;
	/** Threadのexecutor */
	private final ExecutorService exec;

	/** エンコーダー */
	private IStreamCoder encoder = null;
	/** エンコード用処理パケット */
	private IPacket packet = null;

	/** デコーダー */
	private IStreamCoder decoder = null;
	/** デコード用処理パケット */
	private IPacket decodedPacket = null;

	/** frame -> packet変換 */
	private Packetizer packetizer = null;
	/** packet -> frame変換 */
	private Depacketizer depacketizer = null;
	/** リサンプラー */
	private IVideoResampler resampler = null;
	
	/** 次の処理として割り当てておくworkModule */
	private IWorkModule workModule = null;
	/**
	 * @param workModule
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * コンストラクタ
	 */
	public VideoWorkerModule(int id) {
		this.id = id;
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("VideoWorkerThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		exec = Executors.newSingleThreadExecutor(factory);
		// 変換用のエンコーダーをつくっておく。
		IStreamCoder coder = IStreamCoder.make(Direction.ENCODING, ICodec.ID.CODEC_ID_MJPEG);
		coder.setWidth(160);
		coder.setHeight(120);
		coder.setFrameRate(IRational.make(10, 1));
		coder.setTimeBase(IRational.make(1, 1000));
		encoder = coder;
		packet = IPacket.make();
		packetizer = new Packetizer();
		depacketizer = new Depacketizer();
	}
	/**
	 * 音声フレームだった場合、動作を調整する
	 * @param frame
	 * @return true 処理すべき false 処理すべきでない
	 * @throws Exception
	 */
	private boolean checkAudioFrame(IFrame frame) throws Exception {
		if(!(frame instanceof IAudioFrame)) {
			return true;
		}
		// 特になにもしない、変換が遅れても特にやることはない
		return false;
	}
	/**
	 * 動画フレームだった場合に動作を確認する
	 * @param frame
	 * @return true 処理すべき false 処理すべきでない
	 * @throws Exception
	 */
	private boolean checkVideoFrame(IFrame frame) throws Exception {
		if(!(frame instanceof IVideoFrame)) {
			return false;
		}
		// 過去のデータをうけとった場合は処理できない
		if(frame.getPts() < passedPts) {
			logger.warn("過去のフレームなので、ドロップします");
			return false;
		}
		return true;
	}
	/**
	 * デコード処理を実施します。
	 * @param vFrame
	 * @throws Exception
	 */
	private void decodeVideo(IVideoFrame vFrame) throws Exception {
		decoder = packetizer.getDecoder(vFrame, decoder);
		if(decoder == null) {
			logger.warn("フレームのデコーダーが決定できませんでした。");
			return;
		}
		if(!decoder.isOpen()) {
			logger.info("デコーダーを開きます。");
			if(decoder.open(null, null) < 0) {
				throw new Exception("デコーダーが開けませんでした。");
			}
		}
		IPacket pkt = packetizer.getPacket(vFrame, decodedPacket);
		if(pkt == null) {
			return;
		}
		decodedPacket = pkt;
		IVideoPicture picture = IVideoPicture.make(decoder.getPixelType(), vFrame.getWidth(), vFrame.getHeight());
		int offset = 0;
		while(offset < decodedPacket.getSize()) {
			int bytesDecoded = decoder.decodeVideo(picture, decodedPacket, offset);
			if(bytesDecoded <= 0) {
				throw new Exception("データのデコードに失敗しました。");
			}
			offset += bytesDecoded;
			if(picture.isComplete()) {
				// リサンプルにかけてみる
				picture = getResampled(picture);
				encodeVideo(picture);
			}
		}
	}
	/**
	 * リサンプル処理を実施します
	 * @param picture
	 * @return
	 * @throws Exception
	 */
	private IVideoPicture getResampled(IVideoPicture picture) throws Exception {
		if(picture.getWidth()     != encoder.getWidth()
		|| picture.getHeight()    != encoder.getHeight()
		|| picture.getPixelType() != encoder.getPixelType()) {
			if(resampler == null
			||    (picture.getWidth()     != resampler.getInputWidth()
				|| picture.getHeight()    != resampler.getInputHeight()
				|| picture.getPixelType() != resampler.getInputPixelFormat())) {
				resampler = IVideoResampler.make(
						encoder.getWidth(), encoder.getHeight(), encoder.getPixelType(),
						picture.getWidth(), picture.getHeight(), picture.getPixelType());
			}
			IVideoPicture pct = IVideoPicture.make(encoder.getPixelType(), encoder.getWidth(), encoder.getHeight());
			int retval = resampler.resample(pct, picture);
			if(retval <= 0) {
				throw new Exception("映像のリサンプルに失敗しました。");
			}
			pct.setPts(picture.getPts());
			pct.setTimeBase(picture.getTimeBase());
			return pct;
		}
		return picture;
	}
	/**
	 * エンコード処理を実施します。
	 * @param picture
	 * @throws Exception
	 */
	private void encodeVideo(IVideoPicture picture) throws Exception {
		int retval = encoder.encodeVideo(packet, picture, 0);
		if(retval < 0) {
			throw new Exception("変換失敗");
		}
		if(packet.isComplete()) {
			IFrame frame = depacketizer.getFrame(encoder, packet);
//			logger.info(frame.getCodecType() + " " + frame.getPts() + " / " + frame.getTimebase());
			if(workModule != null) {
				if(frame instanceof VideoMultiFrame) {
					VideoMultiFrame multiFrame = (VideoMultiFrame)frame;
					for(IVideoFrame vFrame : multiFrame.getFrameList()) {
						workModule.pushFrame(vFrame, id);
					}
				}
				else {
					workModule.pushFrame(frame, id);
				}
			}
		}
	}
	/**
	 * エンコーダーを開きます。
	 * @throws Exception
	 */
	private void openEncoder() throws Exception {
		if(!encoder.isOpen()) {
			ICodec codec = encoder.getCodec();
			IPixelFormat.Type findType = null;
			for(IPixelFormat.Type type : codec.getSupportedVideoPixelFormats()) {
				if(findType == null) {
					findType = type;
				}
				if(type == IPixelFormat.Type.YUV420P) {
					findType = type;
					break;
				}
			}
			if(findType == null) {
				throw new Exception("対応しているPixelFormatが不明です。");
			}
			encoder.setPixelType(findType);
			if(encoder.open(null, null) < 0) {
				throw new Exception("映像エンコーダーが開けませんでした");
			}
		}
	}
	/**
	 * フレームを受け入れる
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		openEncoder();
		// 音声フレームとの兼ね合いを調べる
		if(!checkAudioFrame(frame)) {
			return;
		}
		// 映像フレームの兼ね合いを調べる
		if(!checkVideoFrame(frame)) {
			return;
		}
		// idが一致しない場合は処理しない
		if(this.id != id) {
			return;
		}
		// 問題なければ書き込む
		final IVideoFrame vFrame = (IVideoFrame)frame;
		exec.execute(new Runnable() {
			@Override
			public void run() {
				try {
					decodeVideo(vFrame);
				}
				catch(Exception e) {
					logger.error("デコード処理で例外が発生しました。", e);
				}
			}
		});
		// このタイミングでframeの変換のthreadにまわす必要あり
		passedPts = frame.getPts();
	}
}
