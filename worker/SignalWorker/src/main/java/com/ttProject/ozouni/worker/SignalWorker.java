package com.ttProject.ozouni.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.ozouni.base.ISignalModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.base.analyzer.IServerNameAnalyzer;
import com.ttProject.ozouni.reportHandler.IReportHandler;

/**
 * 一定時間ごとに処理をするbean
 * @author taktod
 */
public class SignalWorker implements ISignalModule, Runnable {
	/** ロガー */
	private Logger logger = Logger.getLogger(SignalWorker.class);
	/** タイマー動作用future */
	private ScheduledFuture<?> future = null;
	/** レポート用オブジェクト */
	private ReportData reportData = new ReportData();
	/** このプロセスの一意のID(外部から設定してもらうことにする) */
	private String uid;
	// 以下beanで設定できるもの
	/** サーバー名を解決するための動作 */
	@Autowired
	private IServerNameAnalyzer serverNameAnalyzer;
	/** timer動作用executor */
	private ScheduledExecutorService executor; // デフォルトはsingleThreadedScheduledExecutor
	/** 動作間隔 */
	private long interval; // デフォルトは1秒ごと
	/** レポート動作 */
	@Autowired
	private IReportHandler reportHandler;
	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public SignalWorker() throws Exception {
		executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		interval = 1000;
		// UIDはシステムプロパティー(-DuniqueId=xxx)で設定するものとする。
		uid = System.getProperty("uniqueId");
		restartTimer();
	}
	/**
	 * タイマー処理の実体
	 */
	@Override
	public void run() {
		// host名を更新しておく。
		reportData.setHostName(serverNameAnalyzer.getServerName());
		// IReportHandlerで応答する。
		if(reportHandler != null) {
			ReportData data = reportHandler.getReportData(uid);
			if(data != null && (data.getProcessId() != reportData.getProcessId()
					|| !data.getHostName().equals(reportData.getHostName()))) {
				// processIdかhostNameが一致しない同じuniqueIdのプロセスがある場合は、重複しているので、おかしい。
				logger.error("uniqueIdが重複して動作しています。");
				System.exit(0); // 強制終了する。
				return;
			}
			reportHandler.reportData(uid, reportData);
		}
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
		// このタイミングで自分のprocessIdとuniqueIdとして登録しているデータが一致するのがある場合はエラーとして、プロセスを殺しておきたいところ。
		future = executor.scheduleAtFixedRate(this, interval, interval, TimeUnit.MILLISECONDS);
	}
	// 以下setter getter
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
	 * reportData参照(別のところで参照します)
	 * @return
	 */
	@Override
	public ReportData getReportData() {
		return reportData;
	}
}
