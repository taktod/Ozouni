package com.ttProject.ozouni.rtmpwtstest;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.input.RtmpInputModule;
import com.ttProject.ozouni.work.FeederWorkModule;
import com.ttProject.ozouni.work.XuggleAudioWorkModule;
import com.ttProject.ozouni.work.XuggleVideoWorkModule;

/**
 * rtmpによるデータのDL動作テスト
 * @author taktod
 */
public class RtmpDownloadTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(RtmpDownloadTest.class);
	/**
	 * 動作開始
	 * @throws Excetion
	 */
	@Test
	public void test() {
		try {
			logger.info("動作テスト開始");
			String host, port, app, stream;
			host = "49.212.39.17";
			port = "1935";
			app = "live";
			stream = "test";
			String[] options = new String[7];
			options[0] = "-host";
			options[1] = host;
			options[2] = "-port";
			options[3] = port;
			options[4] = "-app";
			options[5] = app;
			options[6] = stream;
			ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("rtmpdownload.xml");
			logger.info(context);
			RtmpInputModule rtmpInputModule = context.getBean(RtmpInputModule.class);
			ClientOptions clientOptions = rtmpInputModule.getClientOptions();
			if(!clientOptions.parseCli(options)) {
				throw new Exception("アクセスアドレスデータをパースすることができませんでした。");
			}
			IWorkModule feederWorkModule = new FeederWorkModule();
			XuggleAudioWorkModule audioModule = context.getBean(XuggleAudioWorkModule.class);
			audioModule.setWorkModule(feederWorkModule);
			XuggleVideoWorkModule videoModule = context.getBean(XuggleVideoWorkModule.class);
			videoModule.setWorkModule(feederWorkModule);
			rtmpInputModule.start(); // 開始するけど、このままだと、rtmpの転送がおわるまでずっとうごきっぱになってるはず。
			context.close();
			logger.info("ここまでOK");
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.info("ここまでNG");
		}
	}
}
