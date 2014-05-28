package com.ttProject.ozouni.base;

/**
 * データをレポートするときに利用するクラス
 * @author taktod
 */
public class ReportData {
	private long framePts = -1;
	private String hostName = null;
	private int processId = -1;
	private DataShareMethod method = null;
	private long lastUpdateTime = -1;
	// TODO 共有方法を知るすべをいれておく必要がある。jedisならキーとか
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
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	public DataShareMethod getMethod() {
		return method;
	}
	public void setMethod(DataShareMethod method) {
		this.method = method;
	}
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
}
