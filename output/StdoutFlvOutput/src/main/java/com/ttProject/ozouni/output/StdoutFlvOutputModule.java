package com.ttProject.ozouni.output;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.container.flv.FlvHeaderTag;
import com.ttProject.container.flv.FlvTagWriter;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IOutputModule;
import com.ttProject.ozouni.base.ISignalModule;
import com.ttProject.ozouni.base.ReportData;

/**
 * 標準出力としてflvデータを出力するモジュール
 * @author taktod
 */
public class StdoutFlvOutputModule implements IOutputModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(StdoutFlvOutputModule.class);
	private final FlvTagWriter writer;
	/** reportDataを引き出すためのsignalWorker */
	@Autowired
	private ISignalModule signalWorker;
	/**
	 * コンストラクタ
	 */
	public StdoutFlvOutputModule() throws Exception {
		// 標準出力としてデータを出力するwriter(stdoutの場合)
//		writer = new FlvTagWriter(Channels.newChannel(System.out));
		// 適当なファイルとして出力してみます
		writer = new FlvTagWriter("hogehoge.flv");
		FlvHeaderTag headerTag = new FlvHeaderTag();
		headerTag.setAudioFlag(false);
		headerTag.setVideoFlag(true);
		writer.addContainer(headerTag);
;	}
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		if(frame instanceof AudioMultiFrame) {
			AudioMultiFrame multiFrame = (AudioMultiFrame)frame;
			for(IAudioFrame audioFrame : multiFrame.getFrameList()) {
				pushFrame(audioFrame, id);
			}
			return;
		}
		else if(frame instanceof VideoMultiFrame) {
			VideoMultiFrame multiFrame = (VideoMultiFrame)frame;
			for(IVideoFrame videoFrame : multiFrame.getFrameList()) {
				pushFrame(videoFrame, id);
			}
			return;
		}
//		logger.info(frame);
		// この部分でframeがmultiFrameだったら分解しておく必要がある。
		// 処理フレームの値を記録する動作が必要
		ReportData reportData = signalWorker.getReportData();
		// frameが戻るようなことがあったらこまるが・・・
		// とりあえず戻らないようにチェックだけやっとく
		if(reportData.getFramePts() < frame.getPts()) {
			reportData.setFramePts(frame.getPts());
		}
		// 現在時刻を登録しておく
		reportData.setLastUpdateTime(System.currentTimeMillis());
		writer.addFrame(id, frame);
	}
}
