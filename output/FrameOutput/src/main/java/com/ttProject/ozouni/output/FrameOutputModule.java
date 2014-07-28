package com.ttProject.ozouni.output;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IOutputModule;
import com.ttProject.ozouni.base.ISignalModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.dataHandler.ISendDataHandler;
import com.ttProject.ozouni.frame.CodecType;
import com.ttProject.ozouni.frame.ShareFrameData;
import com.ttProject.ozouni.frame.analyzer.FrameChecker;
import com.ttProject.ozouni.frame.analyzer.IFrameChecker;

/**
 * frameServerとして、ozouniシステム間でデータを共有するモジュール
 * @author taktod
 */
public class FrameOutputModule implements IOutputModule {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(FrameOutputModule.class);
	/** reportDataを引き出すために、signalWorkerを参照します。*/
	@Autowired
	private ISignalModule signalWorker;
	/** データ送信用のDataHandler設定 */
	private ISendDataHandler sendDataHandler = null;
	/** frameを確認するためのchecker */
	private IFrameChecker frameChecker = new FrameChecker();
	/**
	 * frameの確認モジュールを設定します。
	 * @param frameChecker
	 */
	public void setFrameChecker(IFrameChecker frameChecker) {
		this.frameChecker = frameChecker;
	}
	/**
	 * データ送信handlerを設定する
	 * @param sendDataHandler
	 */
	public void setSendDataHandler(ISendDataHandler sendDataHandler) {
		this.sendDataHandler = sendDataHandler;
		ReportData reportData = signalWorker.getReportData();
		reportData.setMethod(sendDataHandler.getMethod());
		reportData.setKey(sendDataHandler.getKey());
	}
	/**
	 * frameを送信します。
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		// マルチフレームは分解して送ります。
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
		if(frame == null) {
			// frameがnullの場合はほっとく。
			return;
		}
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
		// h264のframeの場合はちょっと特殊なことをやる必要がある。
		CodecType codecType = frameChecker.checkCodecType(frame);
		if(codecType == CodecType.H264) { // h265でも同じようなことしないとだめかもしれない。
			// h264の場合は特殊な動作しなければいけない。
			// sliceIDRの場合は、spsとppsも送る
			// フレームが単一ではなく、マルチで成立している場合は、全部ばらばらに送る必要がある。
		}
		else {
			// trackIdを作成する必要がある。
			ShareFrameData shareFrameData = new ShareFrameData(codecType, frame, id);
			sendDataHandler.pushData(shareFrameData.getShareData());
		}
	}
}
