package com.ttProject.ozouni.work.xuggle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.xuggle.frame.Packetizer;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IStreamCoder.Direction;

/**
 * audioデータを変換するworker
 * もともとつくっていたiOSLiveTurboのデモ動作では、frameを取得したところから別threadで実行していたので、それを踏襲する形にしておきたいと思う。
 * @author taktod
 */
public class AudioWorkerModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(AudioWorkerModule.class);
	/** 経過Pts */
	private long passedPts = 0;
	/** 処理済みサンプル数 */
	private long passedSampleNum = 0;
	/** 映像に対する許可遅延量 */
	private final long allowedDelayForVideo = 500;
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
	/** リサンプラー */
	private IAudioResampler resampler = null;

	/** 次に処理するモジュール */
	private IWorkModule workModule = null;
	/**
	 * @param workModule
	 */
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * コンストラクタ
	 */
	public AudioWorkerModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("AudioWorkerThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		// singleThreadにすることで順番に処理できるようにしておく
		exec = Executors.newSingleThreadExecutor(factory);
		// 変換用のエンコーダーをつくっておく。
		IStreamCoder coder = IStreamCoder.make(Direction.ENCODING, ICodec.ID.CODEC_ID_ADPCM_IMA_WAV);
		coder.setSampleRate(44100);
		coder.setChannels(1);
		coder.setBitRate(48000); // 48kにするけど、adpcmでは意味はない
		encoder = coder;
		packet = IPacket.make();
		packetizer = new Packetizer();
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
		if(frame.getPts() > passedPts + allowedDelayForVideo) {
			// frameのptsが経過pts + 許容delayよりも大きい場合
			// こっちでは挿入する必要あり、ffmpegでは、フレームを適当に挿入してやると、変換を強制することが可能なため
			// この部分でIAudioSamplesをつかった変換を促す動作が必要になる。
			logger.info("映像データが先攻しているので、無音データを挿入します");
			insertNoSound(frame.getPts() - allowedDelayForVideo); // ここまでデータをうめておく
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
		IAudioFrame aFrame = (IAudioFrame)frame;
		// あとは問題ないので、frameを追記しておく。
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
		// timebaseは1000強制になっている
		logger.info(passedPts + " -> " + pts + "までうめておく。");
		logger.info("targetSampleNum:" + (pts * encoder.getSampleRate() / 1000));
		long filledSampleNum = (pts * encoder.getSampleRate() / 1000 - passedSampleNum);
		if(filledSampleNum == 0) {
			// 特に埋める必要がないなら、処理しない
			return;
		}
		logger.info("埋めるsample数:" + filledSampleNum);
		passedSampleNum += filledSampleNum;
		IAudioSamples samples = IAudioSamples.make(encoder.getSampleRate(), encoder.getChannels(), encoder.getSampleFormat());
		samples.setComplete(true, filledSampleNum, encoder.getSampleRate(), encoder.getChannels(), encoder.getSampleFormat(), passedPts);
		encodeSound(samples);
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
		logger.info(decodedPacket);
		IAudioSamples samples = IAudioSamples.make(aFrame.getSampleNum(), decoder.getChannels());
		int offset = 0;
		while(offset < decodedPacket.getSize()) {
			int bytesDecoded = decoder.decodeAudio(samples, decodedPacket, offset);
			if(bytesDecoded < 0) {
				throw new Exception("データのデコードに失敗しました。");
			}
			offset += bytesDecoded;
			if(samples.isComplete()) {
				logger.info(samples);
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
				logger.info("変換できた");
				logger.info(packet);
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
			IAudioSamples.Format findFormat = null;
			for(IAudioSamples.Format format : codec.getSupportedAudioSampleFormats()) {
				if(findFormat == null) {
					findFormat = format;
				}
				if(findFormat == IAudioSamples.Format.FMT_S16) {
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
	 * フレームを受け入れる
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	public void pushFrame(IFrame frame, int id) throws Exception {
		openEncoder();
		if(!checkVideoFrame(frame)) {
			return;
		}
		if(!checkAudioFrame(frame)) {
			return;
		}
		// 特に問題ないので、このframeを書き込む
		IAudioFrame aFrame = (IAudioFrame) frame;
		insertNoSound(aFrame.getPts());
		decodeSound(aFrame);
		passedPts = aFrame.getPts() + 1000 * aFrame.getSampleNum() / aFrame.getSampleRate();
	}
}
