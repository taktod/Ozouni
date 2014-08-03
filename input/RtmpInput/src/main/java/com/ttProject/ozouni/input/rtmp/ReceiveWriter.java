/*
 * RtmpInput(Ozouni) - https://github.com/taktod/Ozouni
 * Copyright (c) 2014 ttProject. All rights reserved.
 * 
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3.
 */
package com.ttProject.ozouni.input.rtmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.flazr.rtmp.RtmpMessage;
import com.ttProject.container.flv.FlvTag;
import com.ttProject.container.flv.type.AudioTag;
import com.ttProject.container.flv.type.VideoTag;
import com.ttProject.flazr.unit.MessageManager;
import com.ttProject.ozouni.base.ISignalModule;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.input.rtmp.model.FlvTagOrderModel;
import com.ttProject.ozouni.input.rtmp.model.IFlvTagOrderModel;

/**
 * rtmp経由でデータをうけとったときのwriter
 * 再度publishしたときに前の時刻から続きを実施すると助かる。
 * width x heightやcodec、サンプルレート等が変更になった場合は、データをやり直す必要がある。(特に変換プログラム)
 * 
 * あと時間に関するデータも報告する必要あり。
 * @author taktod
 */
public class ReceiveWriter implements IReceiveWriter {
	/** ロガー */
	private final Logger logger = LoggerFactory.getLogger(ReceiveWriter.class);
	/** flvAtom -> flvTagへの変換用マネージャー */
	private final MessageManager messageManager = new MessageManager();
	/** データのソートを実施するモデル */
	private IFlvTagOrderModel orderModel = new FlvTagOrderModel();
	/** 出力モジュール */
	private IWorkModule workModule;
	/** アクセスシグナルモジュール */
	@Autowired
	private ISignalModule signalWorker;
	/**
	 * 出力モジュール設定
	 * @param workModule
	 */
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * publish通知をうけとったときの処理
	 */
	public void publish() {
	}
	/**
	 * unpublish通知をうけとったときの処理
	 */
	public void unpublish() {
		// unpublishしたときに、orderModelをクリアしておかないとだめ(クリアしないと、orderModel用に次のデータがこないため、処理が進まない)
		orderModel.reset();
		// このタイミングでreportDataにアクセスしてframePtsをリセットしておく
		signalWorker.getReportData().setFramePts(0);
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
			// 変更があったとわかったときに、トリガーとして、誰かに処理開始させるものが必要になりそうです。
			// どうやるかね？(redisのpubsubか？それとも直接phpたたくか？)
			// とりあえず、直接phpをたたくのがちょうどよさそうだが・・・
			for(FlvTag t : orderModel.getAudioCompleteTag()) {
				if(t instanceof AudioTag) {
					AudioTag aTag = (AudioTag)t;
					workModule.pushFrame(aTag.getFrame(), 0x08);
				}
			}
			for(FlvTag t : orderModel.getVideoCompleteTag()) {
				if(t instanceof VideoTag) {
					VideoTag vTag = (VideoTag)t;
					workModule.pushFrame(vTag.getFrame(), 0x09);
				}
			}
		}
		catch(Exception e) {
			logger.error("例外発生", e);
		}
	}
}
