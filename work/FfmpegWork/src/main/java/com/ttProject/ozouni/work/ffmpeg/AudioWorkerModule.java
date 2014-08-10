package com.ttProject.ozouni.work.ffmpeg;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;

/**
 * 音声の動作について実行しておく
 * やること
 * ・音声フレームが途切れて再開した場合に、sampleRateやchannel数に変化がない場合は同じトラックとして動作するようにすること
 * ・映像フレームの進み具合を確認しつつ、音声フレームが抜けていることがわかったら、無音の音声フレームを適宜挿入してやる必要がある
 * 
 * とりあえず映像フレームとの差分が１秒以上になったら無音frameを挿入して補完してやる
 * 
 * 映像データがきた場合
 * 映像Pts < passedPts + allowedDelayForVideo →なにもしない
 * それ以外passedPtsが映像Ptsの1秒前までになるように無音frameを挿入してやる
 * 
 * 音声データがきた場合
 * codecが違う場合 → 動作やり直し
 * passedPts < pts → 抜けている部分に無音frame挿入して追記する
 * 
 * 面倒なので、無音frameを1つだけ挿入する形でごまかして、asyncで無音を埋めておく
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
	/**
	 * 動画フレームだった場合に動作を調整する
	 * @param frame
	 * @return false 処理すべきでない true 処理すべき
	 */
	private boolean checkVideoFrame(IFrame frame) {
		// 映像フレームでなかったら関係ない
		if(!(frame instanceof IVideoFrame)) {
			return true;
		}
		// 音声データが１つもきていない場合は、追加する無音frameが決定しないので、なにもしない
		if(lastAudioFrame == null) {
			return false;
		}
		if(frame.getPts() > passedPts + allowedDelayForVideo) {
			// frameのptsが経過pts + 許容delayよりも大きい場合
			// こっちでは挿入する必要あり、ffmpegでは、フレームを適当に挿入してやると、変換を強制することが可能なため
			passedPts = frame.getPts() - allowedDelayForVideo;
			logger.info("無音frameが必要:" + passedPts);
		}
		return false;
	}
	/**
	 * 音声フレームを確認する
	 * @param frame
	 * @param pts
	 * @return true 処理すべき false 処理すべきでない
	 */
	private boolean checkAudioFrame(IFrame frame) {
		// 音声フレームでなかったら関係ない
		if(!(frame instanceof IAudioFrame)) {
			return false;
		}
		IAudioFrame aFrame = (IAudioFrame)frame;
		if(lastAudioFrame != null) {
			// フレームデータが入れ替わっていないか確認する必要あり
		}
		// あとは問題ないので、frameを追記しておく。
		// 音声フレームだった場合
		if(frame.getPts() < passedPts) {
			// 過去のフレームだったら追加してもffmpegが混乱するだけなので、捨てる
			return false;
		}
		// ここの30はframeデータを確認してきめる
		if(frame.getPts() - 30 > passedPts) {
			// ffmpegの動作では挿入する必要ない。
			// xuggleでは必要あり(無音部を自動的に埋める方法がわからないため。)
			logger.info("無音frameが必要その２:" + (frame.getPts() - 30));
		}
		return true;
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
		logger.info("普通に書き込む:" + frame.getPts());
		passedPts = frame.getPts();
		lastAudioFrame = (IAudioFrame)frame;
	}
	private void writeFrame() throws Exception {
		
	}
}
