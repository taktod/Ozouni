package com.ttProject.ozouni.base;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * データをレポートするときに利用するクラス
 * @author taktod
 */
public class ReportData implements Serializable {
	/** ID */
	private static final long serialVersionUID = 812521654687321056L;
	/** 動作プロセスID */
	private static int thisProcessId = -1; // プロセスIDのひな形
	/**
	 * 静的初期化
	 */
	static {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		thisProcessId = Integer.parseInt(bean.getName().split("@")[0]);
	}
	/** モジュールID用の割り当てよう変数 */
	private int moduleId = 1;
	/**
	 * 新たに開始したmoduleにidを割り当てる
	 * @return
	 */
	public synchronized int getNextModuleId() {
		int id = moduleId;
		moduleId <<= 1;
		return id;
	}
	/** 処理途中のpts値(基本的に最終データ出力のモジュールがレポートします) */
	private long framePts = -1;
	/** 動作ホスト名(IServerNameAnalyzerで決定させます。) */
	private String hostName = null;
	/** プロセスID */
	private int processId;
	/** フレーム共有メソッド名 */
	private String method = null;
	/** フレーム共有キー */
	private String key = null;
	/** 最終更新時刻 */
	private long lastUpdateTime = -1;
	/** モジュールのリスト */
	private String moduleList = null;
	/** 各モジュールの動作ステータス情報 */
	private int moduleStatus = 0;
	/**
	 * コンストラクタ
	 */
	public ReportData() {
		processId = thisProcessId;
	}
	public long getFramePts() {
		return framePts;
	}
	public void setFramePts(long framePts) {
		this.framePts = framePts;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getProcessId() {
		return processId;
	}
	/**
	 * 外部からデータを読み込んだときに必要になる。
	 * @param processId
	 */
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getModuleList() {
		return moduleList;
	}
	public void addModule(String moduleData) {
		if(moduleList == null) {
			moduleList = moduleData;
		}
		else {
			moduleList += "\n" + moduleData;
		}
	}
	public void resetWorkStatus() {
		moduleStatus = 0;
	}
	public void reportWorkStatus(int moduleId) {
		moduleStatus |= moduleId;
	}
	public int getModuleStatus() {
		return moduleStatus;
	}
	@Override
	public String toString() {
		StringBuilder data = new StringBuilder("recordData:");
		data.append(" framePts=").append(framePts);
		data.append(" hostName=").append(hostName);
		data.append(" processId=").append(processId);
		data.append(" method=").append(method);
		data.append(" key=").append(key);
		return data.toString();
	}
}
