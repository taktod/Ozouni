/*
 * RtmpInput(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.rtmpInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.RtmpWriter;
import com.ttProject.container.flv.FlvTag;
import com.ttProject.flazr.unit.MessageManager;
import com.ttProject.ozouni.base.worker.SendFrameWorker;
import com.ttProject.ozouni.rtmpInput.model.FlvTagOrderModel;
import com.ttProject.ozouni.rtmpInput.model.IFlvTagOrderModel;

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
	/** flvAtom -> flvTagへの変換用マネージャー */
	private final MessageManager messageManager = new MessageManager();
	/** データのソートを実施するモデル */
	private IFlvTagOrderModel orderModel = new FlvTagOrderModel();
	/** データ共有用のworker */
	@Autowired
	private SendFrameWorker sendFrameWorker;
	/**
	 * publish通知をうけとったときの処理
	 */
	public void publish() {
	}
	/**
	 * unpublish通知をうけとったときの処理
	 */
	public void unpublish() {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(RtmpMessage message) {
		try {
			FlvTag tag = messageManager.getTag(message);
			orderModel.addTag(tag);
			// audioTagのコーデック情報(サンプル数等も含めて)が変更になった場合等にffmpegとかの変換の場合は作り直す必要がある。
//			ReportData reportData = signalWorker.getReportData();
//			long pts = reportData.getFramePts();
			for(FlvTag t : orderModel.getAudioCompleteTag()) {
				logger.info("atag:{}", t);
				// できたデータを登録しておく。
//				if(pts < t.getPts()) {
//					pts = t.getPts();
//				}
			}
			for(FlvTag t : orderModel.getVideoCompleteTag()) {
				logger.info("vtag:{}", t);
				// こっちもできたデータを登録しておく。
//				if(pts < t.getPts()) {
//					pts = t.getPts();
//				}
			}
/*			// とりあえずここにいれるのはおかしいだろう。
			reportData.setLastUpdateTime(System.currentTimeMillis());
			if(pts == 0) {
				return;
			}
			reportData.setFramePts(pts);*/
		}
		catch(Exception e) {
			logger.error("例外発生", e);
		}
	}
}
