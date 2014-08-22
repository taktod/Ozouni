package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.frame.Frame;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.work.ffmpeg.AudioWorkerModule;
import com.ttProject.ozouni.work.ffmpeg.VideoWorkerModule;

/**
 * ffmpegを使ってframeを変換する動作
 * h264のdtsがある場合の動作に大いに問題があるはず。(いまのところそんなデータは見たことないので問題視しないけど)
 * TODO ffmpegの変換コマンド等を外部からの設定で動作できるようにしておきたいところ・・・
 * あと、flv特化しているので、そのあたり調整しておかないと・・・せめて名前だけでも・・・
 * @author taktod
 */
public class FfmpegWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FfmpegWorkModule.class);
	/** 処理済みpts値 */
	private long passedPts = 0;
	/** 再生のやり直し等で追加される、ptsの差分値 */
	private long ptsDiff = 0;
	/** リセット判定のinterval、この値以上、過去のデータがきた場合は、ストリームが再開されていると判定しておきます */
	private long resetInterval = 1000L;
	/** 映像の処理module */
	private VideoWorkerModule videoWorkerModule = new VideoWorkerModule();
	/** 音声の処理module */
	private AudioWorkerModule audioWorkerModule = new AudioWorkerModule();
	/**
	 * 出力モジュールを設定
	 * @param outputModule
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		videoWorkerModule.setWorkModule(workModule);
		audioWorkerModule.setWorkModule(workModule);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		if(frame instanceof AudioMultiFrame) {
			AudioMultiFrame multiFrame = (AudioMultiFrame)frame;
			for(IAudioFrame aFrame : multiFrame.getFrameList()) {
				pushFrame(aFrame, id);
			}
			return;
		}
		if(frame instanceof VideoMultiFrame) {
			VideoMultiFrame multiFrame = (VideoMultiFrame)frame;
			for(IVideoFrame vFrame : multiFrame.getFrameList()) {
				pushFrame(vFrame, id);
			}
			return;
		}
		long orgPts = (1000L * frame.getPts() / frame.getTimebase());
		long pts = orgPts + ptsDiff;
		// フレームのデータが巻き戻った場合は、そのデータは前のデータになったと見るべき
		if(pts < passedPts - resetInterval) {
			// これだけ離れている場合は、ストリームがリセットされたと判定する。
			logger.info("リセットされた");
			// 今回のpts値が開始位置であると判定する。
			ptsDiff = passedPts - 1000L * frame.getPts() / frame.getTimebase();
			pts = passedPts;
		}
		// pts値が進んでいる場合は更新しておく
		if(pts > passedPts) {
			passedPts = pts;
		}
		// flvしか扱わないつもりなので、このタイミングでptsを強制的に直してしまう。
		Frame f = (Frame)frame;
		f.setPts(pts);
		f.setTimebase(1000);
		// h264のdtsについては、あとで考えることにしよう。
		videoWorkerModule.pushFrame(frame, id);
		audioWorkerModule.pushFrame(frame, id);
	}
}
