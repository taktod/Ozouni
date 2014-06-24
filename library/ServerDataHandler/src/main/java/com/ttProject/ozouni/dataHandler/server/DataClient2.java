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
public class DataClient2 {
	/** ロガー */
	private Logger logger = Logger.getLogger(DataClient2.class);
	private Set<IDataListener> listeners = new HashSet<IDataListener>();
	private ClientBootstrap bootstrap = null;
	private ChannelFuture future = null;
	/**
	 * コンストラクタ
	 * @param server 接続先サーバー
	 * @param port 接続先ポート
	 */
	public DataClient2(String server, int port) {
		bootstrap = new ClientBootstrap(
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
		future = bootstrap.connect(new InetSocketAddress(server, port));
		future.awaitUninterruptibly();
		if(!future.isSuccess()) {
			close();
		}
	}
	/**
	 * 閉じる
	 */
	public void close() {
		// これいらないかも
		future.getChannel().getCloseFuture().awaitUninterruptibly(); // ここで処理待ちにすると、ずっと応答がかえってこない(threadが１つつぶされる。)
		bootstrap.releaseExternalResources();
		// ここでは応答がかえってこないっぽい。
		logger.info("処理がおわった");
		future = null;
		bootstrap = null;
	}
	public synchronized void addEventListener(IDataListener listener) {
		if(future != null && bootstrap != null) {
			listeners.add(listener);
		}
	}
	public synchronized boolean removeEventListener(IDataListener listener) {
		if(future == null || bootstrap == null) {
			// すでに停止している
			return false;
		}
		return listeners.remove(listener);
	}
	private synchronized Set<IDataListener> getListener() {
		if(future == null || bootstrap == null) {
			return null;
		}
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
