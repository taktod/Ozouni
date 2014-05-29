package com.ttProject.ozouni.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * 単純なrestAPIで応答するサーバー実装
 * ozouniシステムのプロセス状態を管理するために利用します。
 * @author taktod
 */
public class EasyRestServer {
	/**
	 * メインエントリー
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Server server = new Server(12345);
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(RestServlet.class, "/");
		server.setHandler(handler);
		server.start();
		server.join();
	}
	@SuppressWarnings("serial")
	public static class RestServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("response");
		}
	}
}
