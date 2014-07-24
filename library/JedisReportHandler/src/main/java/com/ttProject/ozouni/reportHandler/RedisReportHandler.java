package com.ttProject.ozouni.reportHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.ttProject.ozouni.base.ReportData;

/**
 * redis経由でレポート情報を保存するhandler
 * とりあえずredis経由でReportDataを他のプロセスと共有しておきたい。
 * @author taktod
 */
public class RedisReportHandler implements IReportHandler {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(RedisReportHandler.class);
	private StringRedisTemplate template = null;
	public void setStringRedisTemplate(StringRedisTemplate template) {
		this.template = template;
	}
	@Override
	public void reportData(String uid, ReportData data) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("framePts", Long.toString(data.getFramePts()));
		map.put("hostName", data.getHostName() != null ? data.getHostName() : "null");
		map.put("processId", Integer.toString(data.getProcessId()));
		map.put("method", data.getMethod() != null ? data.getMethod().toString() : "null");
		map.put("lastUpdateTime", Long.toString(data.getLastUpdateTime()));
		map.put("key", data.getKey() != null ? data.getKey() : "null");
		template.opsForHash().putAll(uid, map);
		template.expire(uid, 3, TimeUnit.SECONDS);
	}
	@Override
	public ReportData getData(String uid) {
		Map<Object, Object> data = template.opsForHash().entries(uid);
		if(data.size() == 0) {
			return null;
		}
		ReportData result = new ReportData();
		if(data.get("framePts") != null) {
			try {
				result.setFramePts(Long.parseLong(data.get("framePts").toString()));
			}
			catch(Exception e) {
			}
		}
		if(data.get("hostName") != null && !data.get("hostName").equals("null")) {
			result.setHostName(data.get("hostName").toString());
		}
		if(data.get("processId") != null) {
			try {
				result.setProcessId(Integer.parseInt(data.get("processId").toString()));
			}
			catch(Exception e) {
			}
		}
		if(data.get("method") != null && !data.get("method").equals("null")) {
			result.setMethod(data.get("method").toString());
		}
		if(data.get("lastUpdateTime") != null) {
			try {
				result.setLastUpdateTime(Long.parseLong(data.get("lastUpdateTime").toString()));
			}
			catch(Exception e) {
			}
		}
		if(data.get("key") != null && !data.get("key").equals("null")) {
			result.setKey(data.get("key").toString());
		}
		return result;
	}
}
