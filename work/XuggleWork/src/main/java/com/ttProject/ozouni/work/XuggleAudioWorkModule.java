package com.ttProject.ozouni.work;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.ozouni.base.ISignalModule;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.xuggle.frameutil.Depacketizer;
import com.ttProject.xuggle.frameutil.Packetizer;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IStreamCoder.Direction;

/**
 * xuggleをつかった音声の変換モジュール
 * 映像とのgapには無音を挿入します
 * @author taktod
 */
public class XuggleAudioWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(XuggleAudioWorkModule.class);
	/** 経過Pts */
	private long passedPts = 0;
	/** 処理済みサンプル数 */
	private long passedSampleNum = 0;
	/** 映像に対する許可遅延量 */
	private long allowedDelay = 500;
	/** 処理するID */
	private int id = -1;
	/** Threadのexecutor */
	private final ExecutorService exec;

	/** エンコード情報 */
	private int channels = 2;
	private int bitRate = 96000;
	private int sampleRate = 44100;
	private ICodec.ID codecId;

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
	private IAudioResampler resampler = null;

	/** 次に処理するモジュール */
	private IWorkModule workModule = null;
	/** モジュールのID番号 */
	private int moduleId = 0;
	/** アクセスシグナルモジュール */
	@Autowired
	private ISignalModule signalWorker;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
		if(moduleId != 0) {
			return;
		}
		ReportData reportData = signalWorker.getReportData();
		moduleId = reportData.getNextModuleId();
		String moduleData = moduleId + ":" + getClass().getSimpleName();
		reportData.addModule(moduleData);
		workModule.start();
	}
	/**
	 * チャンネルを設定
	 * @param channels
	 */
	public void setChannels(int channels) {
		this.channels = channels;
	}
	/**
	 * ビットレートを設定
	 * @param bitRate
	 */
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}
	/**
	 * サンプルレートを設定
	 * @param sampleRate
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	/**
	 * コーデックを設定
	 * @param codecName
	 */
	public void setCodec(String codecName) {
		this.codecId = ICodec.ID.valueOf(codecName);
	}
	/**
	 * 処理IDを設定
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * 許容delay用を設定
	 * @param delay ミリ秒
	 */
	public void setAllowedDelay(long delay) {
		this.allowedDelay = delay;
	}
	/**
	 * コンストラクタ
	 */
	public XuggleAudioWorkModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("XuggleAudioWorkThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		// singleThreadにすることで順番に処理できるようにしておく
		exec = Executors.newSingleThreadExecutor(factory);
		packet = IPacket.make();
		decodedPacket = IPacket.make();
		packetizer = new Packetizer();
		depacketizer = new Depacketizer();
	}
	/**
	 * 動画フレームだった場合に動作を調整する
	 * @param frame
	 * @return false 処理すべきでない true 処理すべき
	 */
	private boolean checkVideoFrame(IFrame frame) throws Exception {
		// 映像フレームでなかったら関係ない
		if(!(frame instanceof IVideoFrame)) {
			return true;
		}
		if(frame.getPts() > passedPts + allowedDelay) {
			// frameのptsが経過pts + 許容delayよりも大きい場合
			// こっちでは挿入する必要あり、ffmpegでは、フレームを適当に挿入してやると、変換を強制することが可能なため
			// この部分でIAudioSamplesをつかった変換を促す動作が必要になる。
			insertNoSound(frame.getPts() - allowedDelay); // ここまでデータをうめておく
		}
		return false;
	}
	/**
	 * 音声フレームを確認する
	 * @param frame
	 * @param pts
	 * @return true 処理すべき false 処理すべきでない
	 */
	private boolean checkAudioFrame(IFrame frame) throws Exception {
		// 音声フレームでなかったら関係ない
		if(!(frame instanceof IAudioFrame)) {
			return false;
		}
		// 音声フレームだった場合
		if(frame.getPts() < passedPts) {
			logger.warn("過去のフレームなので、ドロップします");
			// 過去のフレームだったら追加してもffmpegが混乱するだけなので、捨てる
			return false;
		}
		return true;
	}
	/**
	 * 特定のptsの位置まで
	 * @param pts
	 */
	private void insertNoSound(long pts) throws Exception {
		// ここから先を別threadにやらせておけばいいと思う
		// timebaseは1000強制になっている
		long filledSampleNum = (pts * encoder.getSampleRate() / 1000 - passedSampleNum);
		if(filledSampleNum == 0) {
			// 特に埋める必要がないなら、処理しない
			return;
		}
		passedSampleNum += filledSampleNum;
		final IAudioSamples samples = IAudioSamples.make(encoder.getSampleRate(), encoder.getChannels(), encoder.getSampleFormat());
		samples.setComplete(true, filledSampleNum, encoder.getSampleRate(), encoder.getChannels(), encoder.getSampleFormat(), passedPts);
		exec.execute(new Runnable() {
			@Override
			public void run() {
				try {
					encodeSound(samples);
				}
				catch(Exception e) {
					logger.error("無音音声の追記処理で例外が発生しました", e);
				}
			}
		});
		passedPts = pts; // ここまで過ぎたことにしておく。
	}
	/**
	 * 音声データをデコードします。
	 * @param audioFrame
	 */
	private void decodeSound(IAudioFrame aFrame) throws Exception {
		// このフレームをデコードして、何サンプル取得できたか確認しておきたいところ。
		decoder = packetizer.getDecoder(aFrame, decoder);
		if(decoder == null) {
			logger.warn("フレームのデコーダーが決定できませんでした。");
			return;
		}
		if(!decoder.isOpen()) {
			logger.info("デコーダーを開きます。");
			if(decoder.open(null, null) < 0) {
				throw new Exception("デコーダーが開けませんでした");
			}
		}
		IPacket pkt = packetizer.getPacket(aFrame, decodedPacket);
		if(pkt == null) {
			return;
		}
		decodedPacket = pkt;
		IAudioSamples samples = IAudioSamples.make(aFrame.getSampleNum(), decoder.getChannels());
		int offset = 0;
		while(offset < decodedPacket.getSize()) {
			int bytesDecoded = decoder.decodeAudio(samples, decodedPacket, offset);
			if(bytesDecoded < 0) {
				throw new Exception("データのデコードに失敗しました。");
			}
			offset += bytesDecoded;
			if(samples.isComplete()) {
				// ここで必要だったらリサンプル処理が必要
				samples = getResampled(samples);
				// このサンプルデータを処理にまわしておけばよさげ。
				passedSampleNum += samples.getNumSamples();
				encodeSound(samples);
			}
		}
	}
	/**
	 * リサンプルをかけて、周波数を変換しておきます。
	 * @param samples
	 * @return
	 */
	private IAudioSamples getResampled(IAudioSamples samples) throws Exception {
		if(samples.getSampleRate() != encoder.getSampleRate()
		|| samples.getFormat()     != encoder.getSampleFormat()
		|| samples.getChannels()   != encoder.getChannels()) {
			if(resampler == null
			||    (samples.getSampleRate() != resampler.getInputRate()
				|| samples.getFormat()     != resampler.getInputFormat()
				|| samples.getChannels()   != resampler.getInputChannels())) {
				// リサンプラーがない、もしくは、リサンプラーの入力フォーマットと、現状の入力フォーマットが違う場合
				// リサンプラーを作り直す
				resampler = IAudioResampler.make(
						encoder.getChannels(), samples.getChannels(),
						encoder.getSampleRate(), samples.getSampleRate(),
						encoder.getSampleFormat(), samples.getFormat());
			}
			IAudioSamples spl = IAudioSamples.make(1024, encoder.getChannels());
			int retval = resampler.resample(spl, samples, samples.getNumSamples());
			if(retval <= 0) {
				throw new Exception("音声のリサンプルに失敗しました。");
			}
			spl.setPts(samples.getPts());
			spl.setTimeBase(samples.getTimeBase());
			return spl;
		}
		else {
			return samples;
		}
	}
	/**
	 * エンコード処理を実施します。
	 */
	private void encodeSound(IAudioSamples samples) throws Exception {
		// ここで必要だったらencoderを開く必要あり
		int sampleConsumed = 0;
		while(sampleConsumed < samples.getNumSamples()) {
			int retval = encoder.encodeAudio(packet, samples, sampleConsumed);
			if(retval < 0) {
				throw new Exception("変換失敗");
			}
			sampleConsumed += retval;
			if(packet.isComplete()) {
				IFrame frame = depacketizer.getFrame(encoder, packet);
				if(signalWorker != null) {
					signalWorker.getReportData().reportWorkStatus(moduleId);
				}
				if(workModule != null) {
					if(frame instanceof AudioMultiFrame) {
						AudioMultiFrame multiFrame = (AudioMultiFrame)frame;
						for(IAudioFrame aFrame : multiFrame.getFrameList()) {
							workModule.pushFrame(aFrame, id);
						}
					}
					else {
						workModule.pushFrame(frame, id);
					}
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
			coder.setChannels(channels);
			coder.setSampleRate(sampleRate);
			coder.setBitRate(bitRate);
			encoder = coder;
			// ここでencoderの作成から実施する必要あり
			ICodec codec = encoder.getCodec();
			IAudioSamples.Format findFormat = null;
			for(IAudioSamples.Format format : codec.getSupportedAudioSampleFormats()) {
				if(findFormat == null) {
					findFormat = format;
				}
				if(format == IAudioSamples.Format.FMT_S16) {
					findFormat = format;
					break;
				}
			}
			if(findFormat == null) {
				throw new Exception("対応しているAudioFormatが不明です。");
			}
			encoder.setSampleFormat(findFormat);
			if(encoder.open(null, null) < 0) {
				throw new Exception("音声エンコーダーが開けませんでした");
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		openEncoder();
		if(!checkVideoFrame(frame)) {
			return;
		}
		if(!checkAudioFrame(frame)) {
			return;
		}
		if(this.id != id) {
			// idが一致しないストリームについては、処理しません
			return;
		}
		// 特に問題ないので、このframeを書き込む
		final IAudioFrame aFrame = (IAudioFrame) frame;
		insertNoSound(aFrame.getPts());
		exec.execute(new Runnable() {
			@Override
			public void run() {
				try {
					decodeSound(aFrame);
				}
				catch(Exception e) {
					logger.error("デコード処理で例外が発生しました。", e);
				}
			}
		});
		passedPts = aFrame.getPts() + 1000 * aFrame.getSampleNum() / aFrame.getSampleRate();
	}
}
