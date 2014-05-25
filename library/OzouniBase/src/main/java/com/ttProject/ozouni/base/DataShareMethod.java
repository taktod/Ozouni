package com.ttProject.ozouni.base;

/**
 * データ共有方法
 * @author taktod
 */
public enum DataShareMethod {
	JedisDataHandler,
	ServerDataHandler;
	public static DataShareMethod getMethod(String data) {
		for(DataShareMethod method : values()) {
			if(data.equals(method.toString())) {
				return method;
			}
		}
		throw new RuntimeException("未定義のmethodでした");
	}
}
