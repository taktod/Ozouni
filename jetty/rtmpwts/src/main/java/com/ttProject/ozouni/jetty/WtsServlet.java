package com.ttProject.ozouni.jetty;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

/**
 * websocketのアクセスエントリー
 * @author taktod
 */
public class WtsServlet extends WebSocketServlet {
	/** 動作ロガー */
	private Logger logger = Logger.getLogger(WtsServlet.class);
	/** servletのシリアル番号 */
	private static final long serialVersionUID = 2982098341377632654L;
	/**
	 * 接続時の動作
	 */
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		// とりあえず、queryを分解して必要なデータを取り出したいですね。
		logger.info(request.getParameter("host"));
		logger.info(request.getParameter("port"));
		logger.info(request.getParameter("app"));
		logger.info(request.getParameter("stream"));
		// この段階でport番号がなかったら1935にしておく。
		
		return null;
	}
}
