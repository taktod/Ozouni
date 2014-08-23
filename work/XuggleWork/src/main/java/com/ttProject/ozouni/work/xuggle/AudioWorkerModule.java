package com.ttProject.ozouni.work.xuggle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.ozouni.base.IWorkModule;

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
	/** 映像に対する許可遅延量 */
	private final long allowedDelayForVideo = 500;
	/** 最後に処理したaudioFrame */
	private IAudioFrame lastAudioFrame = null;
	private final ExecutorService exec;
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
		exec = Executors.newCachedThreadPool(factory);
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
			passedPts = frame.getPts() - allowedDelayForVideo;
			// この部分でIAudioSamplesをつかった変換を促す動作が必要になる。
			logger.info("映像データが先攻しているので、無音データを挿入します");
//			aFrame.setPts(passedPts);
//			aFrame.setTimebase(1000);
//			writeFrame(aFrame, 0x08);
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
		// フレームの前に無音部がある場合は、IAudioSamplesで空白を埋めておく
		// ここの30はframeデータを確認してきめる
/*		if(frame.getPts() - 30 > passedPts) {
			// ffmpegの動作では挿入する必要ない。
			// xuggleでは必要あり(無音部を自動的に埋める方法がわからないため。)
			logger.info("無音frameが必要その２:" + (frame.getPts() - 30));
		}*/
		return true;
	}
	/**
	 * 特定のptsの位置まで
	 * @param pts
	 * @param timebase
	 */
	private void insertNoSound(long pts, long timebase) {
		
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
	}
}
