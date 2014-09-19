package com.ttProject.ozouni.work;

import org.apache.log4j.Logger;

import com.ttProject.container.webm.WebmTagWriter;
import com.ttProject.frame.CodecType;
import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 接続にデータを送信するworkModule
 * @author taktod
 */
public class FeederWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FeederWorkModule.class);
	private WebmTagWriter writer;
	public FeederWorkModule() {
		try {
			writer = new WebmTagWriter("output.webm");
			writer.prepareHeader(CodecType.VP8, CodecType.VORBIS);
		}
		catch(Exception e) {
			
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
//		logger.info(frame);
		logger.info(frame + " / " + (frame.getPts() * 1000L / frame.getTimebase()));
		writer.addFrame(id, frame);
	}
	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void setWorkModule(IWorkModule workModule) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(int num) throws Exception {
		
	}
}
