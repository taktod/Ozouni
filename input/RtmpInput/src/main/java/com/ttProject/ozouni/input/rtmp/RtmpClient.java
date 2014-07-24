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
	/**
	 * 設定clientOptionsを応答します。
	 * @return
	 */
	public ClientOptions getClientOptions() {
		return options;
	}
	/**
	 * 接続clientOptionsを設定(上書きします)
	 * @param options
	 */
	public void setClientOptions(ClientOptions options) {
		this.options = options;
	}
	/**
	 * 接続動作(処理がおわるまで応答は帰ってきません)
	 */
	public void connect() {
		final ClientBootstrap bootstrap = getBootstrap(Executors.newCachedThreadPool());
		final ChannelFuture future = bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
		future.awaitUninterruptibly();
		if(!future.isSuccess()) {
			
		}
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		bootstrap.getFactory().releaseExternalResources();
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
