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
import com.ttProject.util.BufferUtil;

/**
 * データをやり取りするクライアント
 */
public class DataClient {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(DataClient.class);
	/** データ転送の結果を受け取るlistener */
	private Set<IDataListener> listeners = new HashSet<IDataListener>();
	/** 接続bootstrap */
	private ClientBootstrap bootstrap;
	/** 接続状況用future */
	private ChannelFuture future = null;
	/**
	 * コンストラクタ
	 * @param server 接続先サーバー
	 * @param port 接続先ポート
	 */
	public DataClient() {
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
	}
	/**
	 * コネクト実施
	 */
	public boolean connect(String server, int port) {
		future = bootstrap.connect(new InetSocketAddress(server, port));
		future.awaitUninterruptibly();
		return future.isSuccess();
	}
	/**
	 * 処理がおわるまで待機します。
	 */
	public void waitForClose() {
		// 処理がおわるまで待っておく。
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		// リソースを解放しておく。
		bootstrap.releaseExternalResources();
	}
	/**
	 * 閉じる
	 */
	public void close() {
		// その場で接続を閉じます。
		future.getChannel().close();
		// リソースを解放しておく。
		bootstrap.releaseExternalResources();
	}
	/**
	 * イベントリスナーを追加する
	 * @param listener
	 */
	public synchronized void addEventListener(IDataListener listener) {
		listeners.add(listener);
	}
	/**
	 * イベントリスナーを削除する
	 * @param listener
	 * @return
	 */
	public synchronized boolean removeEventListener(IDataListener listener) {
		return listeners.remove(listener);
	}
	/**
	 * listenerリストを参照
	 * @return
	 */
	private synchronized Set<IDataListener> getListener() {
		return listeners;
	}
	/**
	 * データをやりとりしたときの動作設定
	 * @author taktod
	 */
	private class ClientHandler extends SimpleChannelUpstreamHandler {
		private int size = -1; // 処理用のデータサイズ -1だと未設定
		private ByteBuffer buffer = null; // 処理途上データ用のbuffer
		@Override
		public synchronized void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			buffer = BufferUtil.connect(buffer, ((ChannelBuffer)e.getMessage()).toByteBuffer());
			while(buffer.remaining() > 0) {
				if(size == -1) {
					// はじめのデータ
					if(buffer.remaining() < 4) {
						return; // データが足りない
					}
					size = buffer.getInt();
				}
				if(buffer.remaining() < size) {
					return; // データが足りない
				}
				// データが足りるので処理する
				ByteBuffer data = ByteBuffer.allocate(size);
				byte[] tmp = new byte[size];
				buffer.get(tmp);
				data.put(tmp);
				data.flip();
				for(IDataListener listener : getListener()) {
					// listenerに通知してやる
					listener.receiveData(data.duplicate());
				}
				// 次のデータ待ち
				size = -1;
			}
		}
	}
}
