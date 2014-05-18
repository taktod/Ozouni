package com.ttProject.ozouni.dataHandler.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.ttProject.ozouni.dataHandler.IDataListener;

/**
 * データをやり取りするクライアント
 */
public class DataClient {
	/** ロガー */
	private Logger logger = Logger.getLogger(DataClient.class);
	private Set<IDataListener> listeners = new HashSet<IDataListener>();
	/**
	 * コンストラクタ
	 * @param server 接続先サーバー
	 * @param port 接続先ポート
	 */
	public DataClient(String server, int port) {
		ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handler", new ClientHandler());
				return pipeline;
			}
		});
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		logger.info("コネクト開始します。");
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(server, port));
		future.awaitUninterruptibly();
		if(future.isSuccess()) {
			future.getChannel().getCloseFuture().awaitUninterruptibly();
		}
		bootstrap.releaseExternalResources();
		// ここでは応答がかえってこないっぽい。
		logger.info("処理がおわった");
	}
	/**
	 * 閉じる
	 */
	public void close() {
		// これいらないかも
	}
	public synchronized void addEventListener(IDataListener listener) {
		listeners.add(listener);
	}
	public synchronized boolean removeEventListener(IDataListener listener) {
		return listeners.remove(listener);
	}
	private synchronized Set<IDataListener> getListener() {
		return listeners;
	}
	/**
	 * データをやりとりしたときの動作設定
	 * @author taktod
	 */
	private class ClientHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			logger.info("データをうけとった");
			ByteBuffer buffer = ((ChannelBuffer)e.getMessage()).toByteBuffer();
			for(IDataListener listener : getListener()) {
				listener.receiveData(buffer.duplicate());
			}
		}
	}
}
