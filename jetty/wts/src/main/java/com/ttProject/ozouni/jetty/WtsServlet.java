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
		// アクセスしてもいいクライアントであるかの判定等、面倒なものがいっぱいあるけど
		// そういうのはなしにして、アクセスしたら見れるという体でいきたいと思う。
		// 内部データ的には、targetIdのデータをうけとって動作するという形になるので、
		IApplication app = Application.getInstance(request.getRequestURI());
		IClient client = new Client(app);
		return client;
	}
}
