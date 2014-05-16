package com.ttProject.ozouni.dataHandler;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

/**
 * Redisを利用して、frameデータを他のプロセスと共有します。
 * こっちは単一jedisで動作させるバージョン
 * 
 * ほしい動作はデータを報告する動作
 * データを取得する動作
 * データを登録してほしいキーを登録する動作
 * 
 * ・共有データキーはあらかじめしめし合わせる必要あり。
 * ・共有データキーに自分のプロセス用のデータをいれてほしい場所(プロセスキー)について登録する。
 * ・発信プロセスは対応するプロセスキーにフレームデータをいれていく。
 * プレームデータはbase64で文字列化したデータとしていれておく感じ
 * 
 * redisのキーは次のようにしておく。
 * 共有キー
 * ozouni:target:[ID] // ここにはset形式でデータをいれておく。
 * ozouni:buffer:[ID]:[process] // ここにデータをlist形式でいれておく。
 * おくっているbinaryデータを共有する動作はとりあえずできあがり。
 * 適当なthreadをつくって、blockingpopさせてやることで、データを受け取っておきたいところ。
 * @author taktod
 */
public class RedisDataHandler implements IDataHandler {
	/** ロガー */
	private Logger logger = Logger.getLogger(RedisDataHandler.class);
	private static RedisDataHandler instance = new RedisDataHandler();
	private Jedis jedis = null;
	/** 共有ターゲットID */
	private String id = null;
	/** 動作プロセスID */
	private String processId = null;
	/** データを受け取ったときに通知する先 */
	private Set<IDataListener> listeners = new HashSet<IDataListener>();
	/** threadPoolをつくっておく */
	private ExecutorService executor = null;
	private Future<Boolean> workerFuture = null;
	/**
	 * コンストラクタ
	 */
	private RedisDataHandler() {
		this(Executors.newFixedThreadPool(1));
	}
	/**
	 * コンストラクタ
	 * @param executor
	 */
	private RedisDataHandler(ExecutorService executor) {
		jedis = new Jedis("localhost", 6379);
		this.executor = executor;
		checkThread();
	}
	/**
	 * インスタンス取得
	 * @return
	 */
	public static RedisDataHandler getInstance() {
		return instance;
	}
	/**
	 * 動作IDを登録
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
		checkThread();
	}
	/**
	 * プロセスIDを登録
	 * @param processId
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
		StringBuilder key = new StringBuilder("ozouni:target:");
		key.append(this.id);
		jedis.sadd(key.toString(), this.processId);
		checkThread();
	}
	private void checkThread() {
		if(this.id == null || this.processId == null) {
			return;
		}
		if(workerFuture != null && !workerFuture.isDone()) {
			// 現状動作しているものをとめてしまう。
			workerFuture.cancel(true);
		}
		workerFuture = executor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				ByteBuffer buffer = popData();
				for(IDataListener listener : listeners) {
					listener.receiveData(buffer.duplicate());
				}
				// redisで応答があったら・・・云々
				// 次の動作を登録しておく。(このままやるとたぶんメモリーリークが発生するので、クラスをきちんとつくっておきたい。)
				return false;
			}
		});
	}
	@Override
	public void registerListener(IDataListener listener) {
		listeners.add(listener);
	}
	@Override
	public boolean unregisterListener(IDataListener listener) {
		return listeners.remove(listener);
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
		Set<String> keySet = jedis.smembers(key.toString());
		// このsetに対してデータをいれていく必要あり。
		for(String processId : keySet) {
			key = new StringBuilder("ozouni:buffer:");
			key.append(this.id).append(":").append(processId);
//			logger.info("targetKey:" + key.toString());
			jedis.rpush(key.toString(), base64Data);
		}
	}
	/**
	 * 共有データを取得していく
	 * lpopで登録する
	 * @return
	 */
	private ByteBuffer popData() throws Exception {
		// 取得するべきデータをみつくろう
		StringBuilder key = new StringBuilder("ozouni:buffer:");
		key.append(this.id).append(":").append(this.processId);
		List<String> data = jedis.blpop(0, key.toString());
		logger.info(data);
		if(data.size() != 2 || !(data.get(0).equals(key.toString()))) {
			throw new Exception("応答データがおかしいです。");
		}
		ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(data.get(1)));
		return buffer;
	}
	/**
	 * jedisを閉じる
	 */
	public void close() {
		if(jedis != null) {
			jedis.close();
			jedis = null;
		}
	}
}
