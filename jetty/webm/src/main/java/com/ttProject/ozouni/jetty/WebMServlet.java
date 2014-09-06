package com.ttProject.ozouni.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ttProject.ozouni.input.FrameInputModule;
import com.ttProject.ozouni.webm.AppConfig;
import com.ttProject.ozouni.work.FeederWorkModule;

/**
 * webmのライブストリーミング用のservlet
 * @author taktod
 */
public class WebMServlet extends HttpServlet {
	/** servletのシリアル番号 */
	private static final long serialVersionUID = -483052854030128014L;
	/** ロガー */
	private static final Logger logger = Logger.getLogger(WebMServlet.class);
	/**
	 * getアクセス
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// webmのstreamingに必要なデータを応答するようにしてやる
		logger.info("output");
		// 新しい接続だった場合は
		// コンテクストをつくる必要あり
		ConfigurableApplicationContext context = null;
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		FeederWorkModule workModule = context.getBean(FeederWorkModule.class);
//		workModule.setApplicatino();
		FrameInputModule inputModule = context.getBean(FrameInputModule.class);
		inputModule.setTargetId("456");
		try {
			// 処理を開始する
			inputModule.start();
		}
		catch(Exception e) {
			;
		}
	}
}
