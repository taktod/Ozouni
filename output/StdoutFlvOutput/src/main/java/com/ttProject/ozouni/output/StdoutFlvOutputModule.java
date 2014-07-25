package com.ttProject.ozouni.output;

import java.nio.channels.Channels;

import org.apache.log4j.Logger;

import com.ttProject.container.flv.FlvHeaderTag;
import com.ttProject.container.flv.FlvTagWriter;
import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IOutputModule;

/**
 * 標準出力としてflvデータを出力するモジュール
 * @author taktod
 */
public class StdoutFlvOutputModule implements IOutputModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(StdoutFlvOutputModule.class);
	private final FlvTagWriter writer;
	/**
	 * コンストラクタ
	 */
	public StdoutFlvOutputModule() throws Exception {
		// 標準出力としてデータを出力するwriter
//		writer = new FlvTagWriter(Channels.newChannel(System.out));
		writer = new FlvTagWriter("hogehoge.flv");
		FlvHeaderTag headerTag = new FlvHeaderTag();
		headerTag.setAudioFlag(false);
		headerTag.setVideoFlag(true);
		writer.addContainer(headerTag);
;	}
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		logger.info(frame);
		writer.addFrame(id, frame);
	}
}
