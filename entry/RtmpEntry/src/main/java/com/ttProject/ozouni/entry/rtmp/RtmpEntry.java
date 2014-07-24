package com.ttProject.ozouni.entry.rtmp;

import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.ozouni.entry.IEntry;
import com.ttProject.ozouni.rtmpInput.RtmpClient;

/**
 * rtmpの起動時の動作補助
 * @author taktod
 */
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
		client.start();
	}
}
