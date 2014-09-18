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
		String host, port, app, stream;
		host = request.getParameter("host");
		port = request.getParameter("port");
		app  = request.getParameter("app");
		stream = request.getParameter("stream");
		if(port == null || port.equals("")) {
			port = "1935";
		}
		if(host == null || host.equals("")) {
			return null;
		}
		if(app == null || app.equals("")) {
			return null;
		}
		if(stream == null || stream.equals("")) {
			return null;
		}
		IApplication appInst = Application.getInstance(host, port, app, stream);
		// 取得できたappInstance
		logger.info(appInst);
		IClient client = new Client(appInst);
		// この段階でport番号がなかったら1935にしておく。
		return client;
	}
}
