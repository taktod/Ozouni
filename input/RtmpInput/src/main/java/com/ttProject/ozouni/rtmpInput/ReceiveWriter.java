/*
 * RtmpInput(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.rtmpInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.RtmpWriter;
import com.ttProject.container.flv.FlvTag;
import com.ttProject.flazr.unit.MessageManager;
import com.ttProject.ozouni.rtmpInput.model.FlvTagOrderModel;

/**
 * rtmp経由でデータをうけとったときのwriter
 * 再度publishしたときに前の時刻から続きを実施すると助かる。
 * width x heightやcodec、サンプルレート等が変更になった場合は、データをやり直す必要がある。(特に変換プログラム)
 * 
 * あと時間に関するデータも報告する必要あり。
 * @author taktod
 */
public class ReceiveWriter implements RtmpWriter {
	/** ロガー */
	private final Logger logger = LoggerFactory.getLogger(ReceiveWriter.class);
	private final MessageManager messageManager = new MessageManager();
	private final FlvTagOrderModel orderModel = new FlvTagOrderModel();
	public void publish() {
		// publishしたとき
	}
	public void unpublish() {
		// unpublishしたとき
	}
	@Override
	public void write(RtmpMessage message) {
		try {
			FlvTag tag = messageManager.getTag(message);
			orderModel.addTag(tag);
			// audioTagのコーデック情報(サンプル数等も含めて)が変更になった場合等にffmpegとかの変換の場合は作り直す必要がある。
			for(FlvTag t : orderModel.getAudioCompleteTag()) {
				logger.info("atag:{}", t);
				// できたデータを登録しておく。
			}
			for(FlvTag t : orderModel.getVideoCompleteTag()) {
				logger.info("vtag:{}", t);
				// こっちもできたデータを登録しておく。
			}
		}
		catch(Exception e) {
			logger.error("例外発生", e);
		}
	}
	@Override
	public void close() {
	}
}
