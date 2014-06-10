package com.ttProject.ozouni.rtmpInput;

import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.ozouni.base.entry.IEntry;

public class RtmpEntry implements IEntry {
	private RtmpClient client = null;
	public void setRtmpClient(RtmpClient client) {
		this.client = client;
	}
	@Override
	public void start(String[] args) throws Exception {
		ClientOptions options = client.getClientOptions();
		if(!options.parseCli(args)) {
			return;
		}
		options.setSaveAs("test.flv");
		// 処理開始
		client.start();
	}
}
