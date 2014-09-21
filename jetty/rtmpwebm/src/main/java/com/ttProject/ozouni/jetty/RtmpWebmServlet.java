package com.ttProject.ozouni.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * httpServletの導入部分
 * きっとrangeリクエストでデータがくると思う。
 * 接続しなおしもあるので、各接続に対して正しいwebmのデータを応答するようにしないとだめであろう。
 * ETagをつかって、どの接続がどの応答なのかみたいなことを把握しておかないとだめであろう。
 * rangeリクエストでどの部分のデータが応答されるかわからないんだが・・・
 * そのあたりどうするかが課題だな。
 * データをすべて保持しておいて・・・みたいなことをすれば楽なのかもしれないけど・・・
 * どうするかな・・・
 * とりあえず、次のようにしておこう。
 * header部ができたらファイルに出力する。→他のアクセスがあったときにも再利用する。
 * データがきたら、10kbyteに分割しつつファイルに書き出していく。
 * こんな感じかな。
 * @author taktod
 */
public class RtmpWebmServlet extends HttpServlet {
	/** シリアルID */
	private static final long serialVersionUID = -4190473684343687110L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doLiveTask(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doLiveTask(req, resp);
	}
	/**
	 * live動作の実態
	 * @param request
	 * @param response
	 */
	private void doLiveTask(HttpServletRequest request, HttpServletResponse response) {
		// ここで必要があれば、rtmpの接続をつくっておく必要あり。
		// んでデータをwebmにして応答しなければだめ。
//		System.out.println("あいうえお");
		// 1つのクラスが同じストリームをみているユーザーの処理を肩代わりするみたいな形にしておく。
		// 
	}
}
