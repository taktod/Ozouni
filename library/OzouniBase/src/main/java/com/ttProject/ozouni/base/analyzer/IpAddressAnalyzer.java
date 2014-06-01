package com.ttProject.ozouni.base.analyzer;

import java.net.InetAddress;

/**
 * ipアドレスを応答する
 * @author taktod
 */
public class IpAddressAnalyzer implements IServerNameAnalyzer {
	/** ipアドレス保持 */
	private final String ipAddress;
	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public IpAddressAnalyzer() throws Exception {
		ipAddress = InetAddress.getLocalHost().getHostAddress();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServerName() {
		return ipAddress;
	}
}
