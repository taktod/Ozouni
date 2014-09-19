/*
 * RtmpInput(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.input.rtmp;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.flazr.client.ClientOptionsEx;
import com.ttProject.flazr.rtmp.RtmpDecoderEx;
import com.ttProject.flazr.rtmp.RtmpEncoderEx;

/**
 * rtmpClient
 * @author taktod
 */
public class RtmpClient {
	/** 動作clientOptions */
	private ClientOptions options = new ClientOptionsEx();
	/** 接続bootstrap */
	private ClientBootstrap bootstrap;
	/** 接続状況用future */
	private ChannelFuture future = null;
	/**
	 * 設定clientOptionsを応答します。
	 * @return
	 */
	public ClientOptions getClientOptions() {
		return options;
	}
	/**
	 * clientOptionsを設定します(上書き)
	 * @param options
	 */
	public void setClientOptions(ClientOptions options) {
		this.options = options;
	}
	/**
	 * 接続を開始します
	 * @param asyncFlg true:応答がすぐにかえってきて、あとで切断処理をしないとだめです。 false:rtmpの処理がおわるまで応答を返しません
	 */
	public boolean connect() {
		bootstrap = getBootstrap(Executors.newCachedThreadPool());
		future = bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
		future.awaitUninterruptibly();
		return future.isSuccess();
	}
	/**
	 * 終了まで待機します。
	 */
	public void waitForClose() {
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		bootstrap.getFactory().releaseExternalResources();
	}
	/**
	 * 処理を終わらせます。
	 */
	public void close() {
		future.getChannel().close();
		bootstrap.releaseExternalResources();
	}
	/**
	 * bootstrap初期化
	 * @param executor
	 * @return
	 */
	private ClientBootstrap getBootstrap(final Executor executor) {
		final ChannelFactory factory = new NioClientSocketChannelFactory(executor, executor);
		final ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handshaker", new ClientHandshakeHandler(options));
				pipeline.addLast("decoder", new RtmpDecoderEx());
				pipeline.addLast("encoder", new RtmpEncoderEx());
				pipeline.addLast("handler", new ClientHandlerEx(options));
				return pipeline;
			}
		});
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		return bootstrap;
	}
}
