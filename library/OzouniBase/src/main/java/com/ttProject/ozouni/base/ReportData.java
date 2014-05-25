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
	protected long getFramePts() {
		return framePts;
	}
	protected void setFramePts(long framePts) {
		this.framePts = framePts;
	}
	protected String getHostName() {
		return hostName;
	}
	protected void setHostName(String hostName) {
		this.hostName = hostName;
	}
	protected int getProcessId() {
		return processId;
	}
	protected void setProcessId(int processId) {
		this.processId = processId;
	}
	protected DataShareMethod getMethod() {
		return method;
	}
	protected void setMethod(DataShareMethod method) {
		this.method = method;
	}
	protected long getLastUpdateTime() {
		return lastUpdateTime;
	}
	protected void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
}
