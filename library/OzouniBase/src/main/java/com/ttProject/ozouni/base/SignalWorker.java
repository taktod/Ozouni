package com.ttProject.ozouni.base;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.ttProject.ozouni.base.analyzer.IServerNameAnalyzer;
import com.ttProject.ozouni.base.analyzer.IpAddressAnalyzer;
import com.ttProject.ozouni.reportHandler.IReportHandler;

/**
 * 一定時間ごとに処理をするbean
 * @author taktod
 */
public class SignalWorker implements Runnable {
	/** タイマー動作用future */
	private ScheduledFuture<?> future = null;
	/** レポート用オブジェクト */
	private ReportData reportData = new ReportData();
	/** このプロセスの一意のID(外部から設定してもらうことにする) */
	private String uid;
	// 以下beanで設定できるもの
	/** サーバー名を解決するための動作 */
	private IServerNameAnalyzer serverNameAnalyzer; // デフォルトはipAddressAnalyzer
	/** timer動作用executor */
	private ScheduledExecutorService executor; // デフォルトはsingleThreadedScheduledExecutor
	/** 動作間隔 */
	private long interval; // デフォルトは1秒ごと
	/** レポート動作 */
	private IReportHandler reportHandler;
	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public SignalWorker() throws Exception {
		executor = Executors.newSingleThreadScheduledExecutor();
		serverNameAnalyzer = new IpAddressAnalyzer();
		interval = 1000;
		// UIDはシステムプロパティー(-DuniqueId=xxx)で設定するものとする。
		uid = System.getProperty("uniqueId");
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
	public ReportData getReportData() {
		return reportData;
	}
	/**
	 * serverNameの解析用のプログラムを入れ替えます。
	 * @param serverNameAnalyzer
	 */
	public void setServerNameAnalyzer(IServerNameAnalyzer serverNameAnalyzer) {
		this.serverNameAnalyzer = serverNameAnalyzer;
	}
	/**
	 * reportDataをレポートする動作
	 * @param reportHandler
	 */
	public void setReportHandler(IReportHandler reportHandler) {
		this.reportHandler = reportHandler;
	}
}
