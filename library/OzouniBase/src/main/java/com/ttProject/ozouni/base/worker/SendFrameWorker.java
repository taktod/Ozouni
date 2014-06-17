package com.ttProject.ozouni.base.worker;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.base.ShareFrameData;
import com.ttProject.ozouni.dataHandler.ISendDataHandler;

/**
 * データの送信を実施するworker
 * 特に必要ないんだが、reportDataとの兼ね合いの処理とかをここでやっつけておけば、余計な処理が見えなくなっていい感じになりそうなので、
 * やっぱりつくっておきたい。
 * あと、ISendDataHandlerとは違い、frameを扱うものとする。
 * @author taktod
 */
public class SendFrameWorker {
	/** signalWorkerからreportDataを引っ張り出す形にしておきます。 */
	private SignalWorker signalWorker = null;
	private ISendDataHandler sendDataHandler = null;
	public void setSendDataHandler(ISendDataHandler sendDataHandler) {
		this.sendDataHandler = sendDataHandler;
		// methodを登録しておく。(本当に登録できるのか？)
		signalWorker.getReportData().setMethod(sendDataHandler.getMethod());
	}
	/**
	 * frameを他のプロセスに送信する
	 * @param frame
	 */
	public void pushFrame(IFrame frame) {
		// 処理フレームの値を記録する動作が必要
		ReportData reportData = signalWorker.getReportData();
		// frameが戻るようなことがあったらこまるが・・・
		// とりあえず戻らないようにチェックだけやっとく
		if(reportData.getFramePts() < frame.getPts()) {
			reportData.setFramePts(frame.getPts());
		}
		// 現在時刻を登録しておく
		reportData.setLastUpdateTime(System.currentTimeMillis());
		// frameのtypeを知る必要があるが・・・
//		ShareFrameData shareFrameData = new ShareFrameData(, frame);
//		sendDataHandler.pushData();
	}
}
