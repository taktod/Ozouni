package com.ttProject.ozouni.base.worker;

import com.ttProject.frame.IFrame;

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
	/**
	 * frameを他のプロセスに送信する
	 * @param frame
	 */
	public void pushFrame(IFrame frame) {
		
	}
}
