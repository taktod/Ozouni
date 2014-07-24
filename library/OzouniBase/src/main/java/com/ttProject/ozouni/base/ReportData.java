package com.ttProject.ozouni.base;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * データをレポートするときに利用するクラス
 * @author taktod
 */
public class ReportData implements Serializable {
	private static final long serialVersionUID = 812521654687321056L;
	private long framePts = -1;
	private String hostName = null;
	private static int thisProcessId = -1; // プロセス番号は自動的に拾えるはず
	static {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		thisProcessId = Integer.parseInt(bean.getName().split("@")[0]);
	}
	/**
	 * コンストラクタ
	 */
	public ReportData() {
		processId = thisProcessId;
	}
	private int processId;
	private String method = null;
	private long lastUpdateTime = -1;
	// TODO 共有方法を知るすべをいれておく必要がある。jedisならキーとか
	private String key = null; // データにアクセスするのに必要となるキー
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
