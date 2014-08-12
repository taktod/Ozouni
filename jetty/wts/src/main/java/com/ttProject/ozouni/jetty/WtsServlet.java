package com.ttProject.ozouni.jetty;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

/**
 * websocketのアクセスエントリー
 * @author taktod
 */
public class WtsServlet extends WebSocketServlet {
	/** servletのシリアル番号 */
	private static final long serialVersionUID = 2982098341377632654L;
	/**
	 * 接続があったときの動作
	 */
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		// アクセスに問題がないなら、clientオブジェクトをinstance化して応答する
		return new Client();
	}
}
