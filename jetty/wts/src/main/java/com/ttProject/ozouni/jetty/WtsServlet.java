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
		// 接続前確認
		
		// 接続実施
		return client;
/*		String path = request.getRequestURI();
		String[] paths = path.split("/");
		if(paths.length < 1) {
			return null;
		}
		// pathが対応しているtargetIdのデータを取得していって、frameデータをDLしないとだめ・・・どうするかね？
		// 新しいアプリの接続があったら、該当targetIdにFrameInputを利用して接続する必要がでてくる。
		// ここでpathをとって、そのpathのアクセスに対してデータを送るという形でいこうか・・・
		// ws://127.0.0.1:8080/wts/targetId/にアクセスしたら該当targetIdのデータをうけとって動作するみたいな感じかな
		// アクセスに問題がないなら、clientオブジェクトをinstance化して応答する
		return new Client(paths[1]);*/
	}
}
