package com.ttProject.ozouni.work;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.IStreamCoder.Direction;
import com.xuggle.xuggler.IStreamCoder.Flags;

/**
 * xuggleを使った映像の変換モジュール
 * 音声とのgapはそのままおいておきます
 * @author taktod
 */
public class XuggleVideoWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(XuggleVideoWorkModule.class);
	/** 経過pts */
	private long passedPts = 0;
	/** 処理するID */
	private int id = -1;
	/** Threadのexecutor */
	private final ExecutorService exec;

	/** エンコード情報 */
	private int GroupOfPictures = 15;
	private int bitrate = 650000;
	private int bitrateTolerance = 9000;
	private int width = 320;
	private int height = 240;
	private int globalQuality = 10;
	private ICodec.ID codecId;
	private IRational frameRate = IRational.make(15, 1);
	private IRational timebase = IRational.make(1, 1000);
	private Map<String, String> properties = new HashMap<String, String>();
	private Map<String, Boolean> flags = new HashMap<String, Boolean>();

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
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * groupOfPicture(keyFrame間隔)を設定
	 * @param groupOfPictures
	 */
	public void setGroupOfPictures(int groupOfPictures) {
		GroupOfPictures = groupOfPictures;
	}
	/**
	 * bitrate設定
	 * @param bitrate
	 */
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
	/**
	 * bitrateTolerance設定
	 * @param bitrateTolerance
	 */
	public void setBitrateTolerance(int bitrateTolerance) {
		this.bitrateTolerance = bitrateTolerance;
	}
	/**
	 * width設定
	 * @param width
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * height設定
	 * @param height
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * 画質設定(guality)を設定(動作があやしい)
	 * @param globalQuality
	 */
	public void setGlobalQuality(int globalQuality) {
		this.globalQuality = globalQuality;
	}
	/**
	 * コーデックを設定
	 * @param codecName
	 */
	public void setCodec(String codecName) {
		this.codecId = ICodec.ID.valueOf(codecName);
	}
	/**
	 * frameRateを設定
	 * @param frameRate
	 */
	public void setFrameRate(int frameRate) {
		this.frameRate = IRational.make(frameRate, 1);
	}
	/**
	 * timebase値を設定
	 * @param timebase
	 */
	public void setTimebase(int timebase) {
		this.timebase = IRational.make(1, timebase);
	}
	/**
	 * h264等のproperties設定
	 * @param properties
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
	}
	/**
	 * h264等のflags設定
	 * @param flags
	 */
	public void setFlags(Map<String, Boolean> flags) {
		this.flags.putAll(flags);
	}
	/**
	 * 処理IDを設定
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * コンストラクタ
	 */
	public XuggleVideoWorkModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("XuggleVideoWorkThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		exec = Executors.newSingleThreadExecutor(factory);
		packet = IPacket.make();
		decodedPacket = IPacket.make();
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
	private synchronized void openEncoder() throws Exception {
		if(encoder == null) {
			IStreamCoder coder = IStreamCoder.make(Direction.ENCODING, codecId);
			coder.setNumPicturesInGroupOfPictures(GroupOfPictures);
			coder.setBitRate(bitrate);
			coder.setBitRateTolerance(bitrateTolerance);
			coder.setWidth(width);
			coder.setHeight(height);
			coder.setGlobalQuality(globalQuality);
			coder.setFrameRate(frameRate);
			coder.setTimeBase(timebase);
			for(Entry<String, String> entry : properties.entrySet()) {
				coder.setProperty(entry.getKey(), entry.getValue());
			}
			for(Entry<String, Boolean> entry : flags.entrySet()) {
				coder.setFlag(Flags.valueOf(entry.getKey()), entry.getValue());
			}
			encoder = coder;
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
	 * {@inheritDoc}
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
