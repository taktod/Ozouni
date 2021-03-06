package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.frame.Frame;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.work.ffmpeg.AudioWorkerModule;
import com.ttProject.ozouni.work.ffmpeg.VideoWorkerModule;

/**
 * ffmpegを使ってframeを変換する動作
 * h264のdtsがある場合の動作に大いに問題があるはず。(いまのところそんなデータは見たことないので問題視しないけど)
 * @author taktod
 */
public class FfmpegWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FfmpegWorkModule.class);
	/** 処理済みpts値 */
	private long passedPts = 0; // 処理済みpts値
	/** 再生のやり直し等で追加される、ptsの差分値 */
	private long ptsDiff = 0; // 再生やり直しとかでずれた場合の補完するptsの差分値
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
//		this.workModule = workModule;
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
		// フレームのデータが巻き戻った場合は、そのデータは前のデータになったと見るべき
		long orgPts = (1000L * frame.getPts() / frame.getTimebase());
		long pts = orgPts + ptsDiff;
		if(pts < passedPts - resetInterval) {
			// TODO 音声frameがrtmpで抜け落ちたあとで復帰した場合には、音声frameのpts値が最終データと同じ値らへんになってしまう問題がある模様。
			// そうするとリセットと同じ現象が走ることがありえた
			// どうしたものかね。
			// これだけ離れている場合は、ストリームがリセットされたと判定する。
			logger.info("リセットされた");
			// 今回のpts値が開始位置であると判定する。
			ptsDiff = passedPts - 1000L * frame.getPts() / frame.getTimebase();
			pts = passedPts;
		}
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
