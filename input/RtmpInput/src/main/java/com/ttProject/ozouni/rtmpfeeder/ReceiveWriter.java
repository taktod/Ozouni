package com.ttProject.ozouni.rtmpfeeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.RtmpWriter;
import com.ttProject.container.flv.FlvTag;
import com.ttProject.flazr.unit.MessageManager;
import com.ttProject.ozouni.rtmpfeeder.model.FlvTagOrderModel;

/**
 * rtmp経由でデータをうけとったときのwriter
 * @author taktod
 */
public class ReceiveWriter implements RtmpWriter {
	/** ロガー */
	private final Logger logger = LoggerFactory.getLogger(ReceiveWriter.class);
	private final MessageManager messageManager = new MessageManager();
	private final FlvTagOrderModel orderModel = new FlvTagOrderModel();
	private long timestamp = 0; // 処理済みtimestamp値を保持timestampを連続で並べることで、ずれとかがでないようにしておく。
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
			}
			for(FlvTag t : orderModel.getVideoCompleteTag()) {
				logger.info("vtag:{}", t);
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
