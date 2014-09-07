package com.ttProject.ozouni.dataHandler.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.ttProject.util.BufferUtil;

/**
 * 他のプロセスにサーバーとしてデータを送信します
 * @author taktod
 */
public class DataServer {
	/** ロガー */
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(DataServer.class);
	private final Set<Channel> channels = new HashSet<Channel>();
	private final Channel serverChannel;
	private final ServerBootstrap bootstrap;
	/** 接続時に共有する初期バッファ値 */
	private Map<Integer, ByteBuffer> initBufferMap = new ConcurrentHashMap<Integer, ByteBuffer>();
	/**
	 * コンストラクタ
	 * @param port
	 */
	public DataServer(int port) {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("DataServerThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		bootstrap = new ServerBootstrap(
			new NioServerSocketChannelFactory(Executors.newCachedThreadPool(factory), Executors.newCachedThreadPool(factory)));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handler", new ServerHandler());
				return pipeline;
			}
		});
		serverChannel = bootstrap.bind(new InetSocketAddress(port));
	}
	/**
	 * データを送信する
	 * @param buffer
	 */
	public void sendData(ByteBuffer buffer) {
		ByteBuffer size = ByteBuffer.allocate(4);
		size.putInt(buffer.remaining());
		size.flip();
		ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(BufferUtil.connect(size, buffer));
		synchronized(channels) {
			for(Channel channel : channels) {
				// データの先頭に通信データ量をいれておく。
				channel.write(channelBuffer);
			}
		}
	}
	/**
	 * 初期データ保持
	 * @param id
	 * @param buffer
	 */
	public void setInitialData(int id, ByteBuffer buffer) {
		initBufferMap.put(id, buffer);
	}
	/**
	 * サーバーを閉じます
	 */
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
	 * 単一チャンネルにデータをおくる
	 * @param buffer
	 * @param channel
	 */
	private void sendData(ByteBuffer buffer, Channel channel) {
		ByteBuffer size = ByteBuffer.allocate(4);
		size.putInt(buffer.remaining());
		size.flip();
		ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(BufferUtil.connect(size, buffer));
		channel.write(channelBuffer);
	}
	/**
	 * アクセスをコントロールするhandler
	 * @author taktod
	 */
	private class ServerHandler extends SimpleChannelUpstreamHandler {
		/** ロガー */
		private Logger logger = Logger.getLogger(ServerHandler.class);
		/**
		 * 例外を捕捉しておきます。
		 */
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			super.exceptionCaught(ctx, e);
		}
		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			logger.info("コネクト");
			Channel channel = e.getChannel();
			// 初期情報をおくっておく。
			for(ByteBuffer buffer : initBufferMap.values()) {
				sendData(buffer.duplicate(), channel);
			}
			// 初期データを送る
			channels.add(channel);
			// ここで初期データを送ってやる必要あり。
		}
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			logger.info("close");
			channels.remove(e.getChannel());
		}
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			logger.info("disconnect");
			channels.remove(e.getChannel());
		}
	}
}
