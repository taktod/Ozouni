package com.ttProject.ozouni.rtmpfeeder;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.flazr.rtmp.RtmpDecoderEx;
import com.ttProject.flazr.rtmp.RtmpEncoderEx;

/**
 * rtmpInputのエントリー
 * @author taktod
 */
public class RtmpInput {
	/** ロガー */
	private static Logger logger = LoggerFactory.getLogger(RtmpInput.class);
	/**
	 * エントリー
	 * @param args
	 */
	public static void main(String[] args) {
		final ClientOptions options = new ClientOptions();
		if(!options.parseCli(args)) {
			return;
		}
		// objectEncoding=3を強制しておきます。(配信クライアントによっては、ObjectEncoding=0で動作できないことがまれにあるため)
		options.putParam("objectEncoding", 3.0);
		if(options.getLoad() == 1 && options.getClientOptionsList() == null) {
			options.setWriterToSave(new ReceiveWriter());
			connect(options);
			return;
		}
		logger.error("マルチアクセスは実装していません。");
	}
	private static void connect(final ClientOptions options) {
		final ClientBootstrap bootstrap = getBootstrap(Executors.newCachedThreadPool(), options);
		final ChannelFuture future = bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
		future.awaitUninterruptibly();
		if(!future.isSuccess()) {
			logger.error("error creating client connection", future.getCause());
		}
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		bootstrap.getFactory().releaseExternalResources();
	}
	private static ClientBootstrap getBootstrap(final Executor executor, final ClientOptions options) {
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
