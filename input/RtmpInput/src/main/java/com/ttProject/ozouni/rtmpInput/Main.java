/*
 * RtmpInput(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.rtmpInput;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.flazr.rtmp.client.ClientOptions;

/**
 * エントリーポイント
 * このbeanはこれとこれを利用して、動作するみたいなものがほしいところ。
 * @author taktod
 */
public class Main {
	/**
	 * メインエントリー
	 * @param args
	 */
	public static void main(String[] args) {
		// argument側に-DuniqIdがない場合はここで載せる必要あり。
		for(String str : args) {
			if(str.startsWith("-DuniqueId")) {
				System.setProperty("uniqueId", str.split("=")[1]);
			}
		}
		ConfigurableApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("rtmpInput.xml");
			RtmpClient rtmpClient = (RtmpClient)context.getBean("rtmpClient");
			// 入力パラメーターからargsをつくる
			ClientOptions options = rtmpClient.getClientOptions();
			if(!options.parseCli(args)) {
				return;
			}
			// とりあえず仮の録画先をいれておく(実際にはなにもしません)
			options.setSaveAs("test.flv");
			// 動作開始
			rtmpClient.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(context != null) {
				context.close();
				context = null;
			}
		}
	}
}
