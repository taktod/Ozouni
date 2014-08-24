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
	/** 最後に処理したaudioFrame */
	private IAudioFrame lastAudioFrame = null;
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
		// データがかわったことについては、IStreamCoder側で調整できるので問題なし
/*		if(lastAudioFrame != null) {
			// フレームデータが入れ替わっていないか確認する必要あり
			if(lastAudioFrame.getCodecType() != aFrame.getCodecType()
			|| lastAudioFrame.getChannel() != aFrame.getChannel()
			|| lastAudioFrame.getSampleRate() != aFrame.getSampleRate()
			|| lastAudioFrame.getBit() != aFrame.getBit()) {
				logger.info("データが変更になっているので、なんとかしておかないとだめ。");
				// 今までの接続があったら切っておく
//				openFlvTagWriter();
			}
		}*/
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
	private void insertNoSound(long pts) {
		// timebaseは1000強制になっている
		logger.info(passedPts + " -> " + pts + "までうめておく。");
		passedPts = pts; // ここまで過ぎたことにしておく。
		encoder.getSampleRate(); // この
	}
	/**
	 * フレームを受け入れる
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	public void pushFrame(IFrame frame, int id) throws Exception {
		if(!checkVideoFrame(frame)) {
			return;
		}
		if(!checkAudioFrame(frame)) {
			return;
		}
		// 特に問題ないので、このframeを書き込む
		passedPts = frame.getPts();
		lastAudioFrame = (IAudioFrame)frame;
		insertNoSound(passedPts);
//		convertSound(frame);
	}
}
