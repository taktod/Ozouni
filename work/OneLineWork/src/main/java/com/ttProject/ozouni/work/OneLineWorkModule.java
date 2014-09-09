package com.ttProject.ozouni.work;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ttProject.frame.Frame;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * ffmpegやxuggleに変換を促すために、入力frameをミリ秒による単純増加データにソートします。
 * @author taktod
 */
public class OneLineWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(OneLineWorkModule.class);
	/** 処理済みpts値 */
	private long passedPts = 0;
	/** 再生のやり直し等で追加される、ptsの差分値 */
	private long ptsDiff = 0;
	/** リセット判定のinterval、この値以上、過去のデータがきた場合は、ストリームが再開されていると判定しておきます */
	private long resetInterval = 1000L;
	/** 次の処理に回すリスト */
	private List<IWorkModule> workModules = new ArrayList<IWorkModule>();
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		// こっちは単体のworkModule用の動作
		workModules.add(workModule);
	}
	/**
	 * 複数のworkModuleを一括して付加するためのプロパティ
	 * @param workModules Listの形で追加してほしい
	 */
	public void setWorkList(List<IWorkModule> workModules) {
		this.workModules.addAll(workModules);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		// マルチフレームは分解しておきます。
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
		if(frame == null) {
			return; // frameがnullだったら処理できない。
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
		Frame f = (Frame)frame;
		f.setPts(pts);
		f.setTimebase(1000); // timebaseミリ秒を強制していますが、flv以外を扱うなら微妙かも・・・
		for(IWorkModule workModule : workModules) {
			workModule.pushFrame(frame, id);
		}
	}
}
