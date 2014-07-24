/*
 * RtmpInput(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.input.rtmp;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.ClientHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;

/**
 * クライアントの動作調整
 * @author taktod
 */
public class ClientHandlerEx extends ClientHandler {
	/** オプション保持 */
	private final ClientOptions options;
	/**
	 * コンストラクタ
	 * @param options
	 */
	public ClientHandlerEx(ClientOptions options) {
		super(options);
		this.options = options;
	}
	/**
	 * メッセージ取得動作
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) {
		final RtmpMessage message = (RtmpMessage) me.getMessage();
		switch(message.getHeader().getMessageType()) {
		case COMMAND_AMF0:
		case COMMAND_AMF3:
			Command command = (Command) message;
			String name = command.getName();
			if("onStatus".equals(name)) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> temp = (Map<String, Object>)command.getArg(0);
				final String code = (String)temp.get("code");
				if(code.equals("NetStream.Play.Stop") ||
						code.equals("NetStream.Play.StreamNotFound")) {
					// この動作を大本におくると、プロセスがおわってしまうので、hookしてとめておく。
					// エラーのコードもあるけど、そっちはスルーしておく。
					ReceiveWriter writer = (ReceiveWriter)options.getWriterToSave();
					writer.unpublish();
					return;
				}
				else if(code.equals("NetStream.Play.Start") ||
						code.equals("NetStream.Publish.Start")) {
					// 開始時の動作
					ReceiveWriter writer = (ReceiveWriter)options.getWriterToSave();
					writer.publish();
				}
			}
			break;
		default:
			break;
		}
		super.messageReceived(ctx, me);
	}
}
