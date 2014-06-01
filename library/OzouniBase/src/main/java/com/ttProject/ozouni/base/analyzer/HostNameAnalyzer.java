package com.ttProject.ozouni.base.analyzer;

import java.net.InetAddress;

/**
 * ホスト名を応答する解析動作
 * @author taktod
 */
public class HostNameAnalyzer implements IServerNameAnalyzer {
	/** ホスト名保持 */
	private final String hostName;
	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public HostNameAnalyzer() throws Exception {
		hostName = InetAddress.getLocalHost().getHostName();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServerName() {
		return hostName;
	}
}
