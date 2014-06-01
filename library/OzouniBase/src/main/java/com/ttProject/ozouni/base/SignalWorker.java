package com.ttProject.ozouni.base;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 一定時間ごとに処理をするbean
 * @author taktod
 */
public class SignalWorker {
	/** timer動作用executor */
	private ScheduledExecutorService executor;
	/** 動作間隔 */
	private long interval = 1000; // デフォルトは1秒ごと
	/** タイマー動作用future */
	private ScheduledFuture<?> future = null;
	/**
	 * 動作させるのに利用するexecutorを設定する
	 * @param executor
	 */
	public void setSignalExecutor(ScheduledExecutorService executor) {
		this.executor = executor;
		restartTimer();
	}
	/**
	 * 動作間隔を設定(ミリ秒)
	 * @param interval
	 */
	public void setInterval(long interval) {
		this.interval = interval;
		restartTimer();
	}
	/**
	 * 保持データでタイマーを再開する
	 */
	private void restartTimer() {
		if(executor == null) {
			// 先にintervalが設定されることもあるため
			return;
		}
		if(future != null) {
			future.cancel(true); // 前の処理は破棄する。
		}
		future = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// ここでやることは・・・
			}
		}, interval, interval, TimeUnit.MILLISECONDS);
	}
}
