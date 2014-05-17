package com.ttProject.ozouni.dataHandler.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * 他のプロセスにサーバーとしてデータを送信します
 * @author taktod
 */
public class DataServer {
	/** ロガー */
//	private final Logger logger = Logger.getLogger(DataServer.class);
	private final Set<Channel> channels = new HashSet<Channel>();
	private final Channel serverChannel;
	private final ServerBootstrap bootstrap;
	/**
	 * コンストラクタ
	 * @param port
	 */
	public DataServer(int port) {
		bootstrap = new ServerBootstrap(
			new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				// なにもなしのpipelineってのもめずらしいなw
				pipeline.addLast("handler", new ServerHandler());
				return pipeline;
			}
		});
		serverChannel = bootstrap.bind(new InetSocketAddress(port));
	}
	public void sendData(ChannelBuffer buffer) {
		synchronized(channels) {
			for(Channel channel : channels) {
				channel.write(buffer);
			}
		}
	}
	public void close() {
		synchronized(channels) {
			for(Channel channel : channels) {
				channel.close();
			}
			channels.clear();
		}
		ChannelFuture future = serverChannel.close();
		future.awaitUninterruptibly(); // timeoutいれておいた方がいいかも
		bootstrap.releaseExternalResources();
	}
	/**
	 * アクセスをコントロールするhandler
	 * @author taktod
	 */
	private class ServerHandler extends SimpleChannelUpstreamHandler {
		/** ロガー */
		private Logger logger = Logger.getLogger(ServerHandler.class);
		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			logger.info("コネクト");
			channels.add(e.getChannel());
		}
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			logger.info("close");
		}
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			logger.info("disconnect");
		}
	}
}
