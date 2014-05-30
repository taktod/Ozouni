/*
 * EasyRestServer(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletMapping;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
		ConfigurableApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("easyRestServer.xml");
			ServletHandler handler = context.getBean("handler", ServletHandler.class);
			ServletMapping sm = new ServletMapping();
			handler.addServletWithMapping(RestServlet.class, "/");
			Server server = context.getBean("server", Server.class);
			server.start();
			server.join();
		}
		finally {
			if(context != null) {
				context.close();
				context = null;
			}
		}
/*		Server server = new Server(12345);
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(RestServlet.class, "/");
		server.setHandler(handler);
		server.start();
		server.join();*/
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
