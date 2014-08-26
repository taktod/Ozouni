package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.frame.Frame;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.work.xuggle.AudioWorkerModule;
import com.ttProject.ozouni.work.xuggle.VideoWorkerModule;

/**
 * xuggleをつかってframeを変換する動作
 * @author taktod
 */
public class XuggleWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(XuggleWorkModule.class);
	/** 処理済みpts値 */
	private long passedPts = 0;
	/** 再生やり直し等で追加されるptsの差分値 */
	private long ptsDiff = 0;
	/** リセット判定のinterval、この値以上、過去のデータがきた場合はストリームが再会されたと判定する */
	private long resetInterval = 1000L;
	private AudioWorkerModule audioWorkerModule = new AudioWorkerModule(8);
	private VideoWorkerModule videoWorkerModule = new VideoWorkerModule(9);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		audioWorkerModule.setWorkModule(workModule);
		videoWorkerModule.setWorkModule(workModule);
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
		f.setTimebase(1000); // こっちもtimebaseでミリ秒を強制しているのが、ちょっと微妙かも・・・
		// こっちもh264のdtsについてはスルーしておく
//		logger.info(frame);
		audioWorkerModule.pushFrame(frame, id);
		videoWorkerModule.pushFrame(frame, id);
	}
}
