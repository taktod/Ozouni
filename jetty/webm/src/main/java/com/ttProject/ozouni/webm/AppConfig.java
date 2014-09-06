package com.ttProject.ozouni.webm;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.base.analyzer.IServerNameAnalyzer;
import com.ttProject.ozouni.base.analyzer.IpAddressAnalyzer;
import com.ttProject.ozouni.dataHandler.ServerReceiveDataHandler;
import com.ttProject.ozouni.input.FrameInputModule;
import com.ttProject.ozouni.reportHandler.IReportHandler;
import com.ttProject.ozouni.reportHandler.RedisReportHandler;
import com.ttProject.ozouni.work.FeederWorkModule;

/**
 * アプリケーションの動作
 * @author taktod
 */
@Configuration
public class AppConfig {
	private static JedisConnectionFactory jedisConnectionFactory;
	private static IServerNameAnalyzer serverNameAnalyzer = null;
	static {
		try {
			Properties prop = new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ozouni.properties"));
			jedisConnectionFactory = new JedisConnectionFactory();
			jedisConnectionFactory.setHostName(prop.getProperty("redis.server"));
			jedisConnectionFactory.setPort(Integer.parseInt(prop.getProperty("redis.port")));
			jedisConnectionFactory.setDatabase(Integer.parseInt(prop.getProperty("redis.db")));
			jedisConnectionFactory.afterPropertiesSet(); // propertyを設定したら、これが必要
		}
		catch(Exception e) {
			throw new RuntimeException("ozouni.propertiesを読み込むときに例外が発生しました。");
		}
		try {
			serverNameAnalyzer = new IpAddressAnalyzer();
		}
		catch(Exception e) {
			throw new RuntimeException("serverNameAnalyzer動作時に例外が発生しました。");
		}
	}
	/**
	 * serverNameAnalyzerの定義
	 * @return
	 */
	@Bean
	public IServerNameAnalyzer serverNameAnalyzer() {
		return serverNameAnalyzer;
	}
	/**
	 * 動作レポートの定義
	 * @return
	 */
	@Bean
	public IReportHandler reportHandler() {
		RedisReportHandler redisReportHandler = new RedisReportHandler();
		redisReportHandler.setStringRedisTemplate(
				new StringRedisTemplate(jedisConnectionFactory)
		);
		return redisReportHandler;
	}
	@Bean
	public IWorkModule workModule() {
		return new FeederWorkModule();
	}
	@Bean
	public IInputModule inputModule() {
		FrameInputModule inputModule = new FrameInputModule();
		inputModule.setReceiveDataHandler(new ServerReceiveDataHandler());
		inputModule.setWorkModule(workModule());
		return inputModule;
	}
}
