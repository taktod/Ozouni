package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redisを利用して、frameデータを他のプロセスと共有します。
 * こっちはjedisPoolで動作させるバージョン
 * jedisは例外がでたらreturnBlokenPoolをつかった方がいいみたい。
 * @author taktod
 */
public class RedisPoolDataHandler {
	/** ロガー */
	private Logger logger = Logger.getLogger(RedisPoolDataHandler.class);
	private static RedisPoolDataHandler instance = new RedisPoolDataHandler();
	/** 共有ターゲットID */
	private String id = null;
	/** 動作プロセスID */
	private String processId = null;
	private final JedisPool pool;
	/**
	 * コンストラクタ
	 */
	private RedisPoolDataHandler() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(12);
		pool = new JedisPool(config, "localhost", 6379);
	}
	public static RedisPoolDataHandler getInstance() {
		return instance;
	}
	/**
	 * 動作IDを登録
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * プロセスIDを登録
	 * @param processId
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
		StringBuilder key = new StringBuilder("ozouni:target:");
		key.append(this.id);
		Jedis jedis = pool.getResource();
		jedis.sadd(key.toString(), this.processId);
		pool.returnResource(jedis);
	}
	/**
	 * 共有データを登録しておく
	 * rpushで登録していく
	 * @param frame
	 */
	public void pushData(ByteBuffer buffer) {
		// 登録すべきデータをつくる。
		int position = buffer.position();
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		// 元にポインタを戻しておく。
		buffer.position(position);
		String base64Data = new String(Base64.encodeBase64(data));
		// 登録すべきkeyを調べる
		StringBuilder key = new StringBuilder("ozouni:target:");
		key.append(this.id);
		Jedis jedis = pool.getResource();
		Set<String> keySet = jedis.smembers(key.toString());
		// このsetに対してデータをいれていく必要あり。
		for(String processId : keySet) {
			key = new StringBuilder("ozouni:buffer:");
			key.append(this.id).append(":").append(processId);
//			logger.info("targetKey:" + key.toString());
			jedis.rpush(key.toString(), base64Data);
		}
		pool.returnResource(jedis);
	}
	/**
	 * 共有データを取得していく
	 * lpopで登録する
	 * @return
	 */
	public ByteBuffer popData() throws Exception {
		// 取得するべきデータをみつくろう
		StringBuilder key = new StringBuilder("ozouni:buffer:");
		key.append(this.id).append(":").append(this.processId);
		Jedis jedis = pool.getResource();
		List<String> data = jedis.blpop(0, key.toString());
		pool.returnResource(jedis);
		logger.info(data);
		if(data.size() != 2 || !(data.get(0).equals(key.toString()))) {
			throw new Exception("応答データがおかしいです。");
		}
		ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(data.get(1)));
		return buffer;
	}
	public void close() {
		if(pool != null) {
			pool.destroy();
			pool = null;
		}
	}
}
