package com.ttProject.ozouni.frame;

import java.io.FileOutputStream;
import java.nio.channels.WritableByteChannel;

import com.ttProject.container.IContainer;
import com.ttProject.container.flv.FlvHeaderTag;
import com.ttProject.container.flv.FlvTagWriter;
import com.ttProject.frame.IFrame;

/**
 * 映像のみのflvとして、frameを出力します。
 * @author taktod
 */
public class FlvVideoWriter implements IFrameWriter {
	/** 処理を実施するwriter */
	private FlvTagWriter writer = null;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFileName(String fileName) throws Exception {
		writer = new FlvTagWriter(fileName);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOutputStream(FileOutputStream outputStream) throws Exception {
		writer = new FlvTagWriter(outputStream);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOutputChannel(WritableByteChannel outputChannel) throws Exception {
		writer = new FlvTagWriter(outputChannel);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public void addContainer(IContainer container) throws Exception {
		if(writer != null) {
			writer.addContainer(container);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFrame(int trackId, IFrame frame) throws Exception {
		if(writer != null) {
			writer.addFrame(trackId, frame);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepareHeader() throws Exception {
		if(writer != null) {
			FlvHeaderTag headerTag = new FlvHeaderTag();
			headerTag.setAudioFlag(false);
			headerTag.setVideoFlag(true);
			writer.addContainer(headerTag);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepareTailer() throws Exception {
		if(writer != null) {
			writer.prepareTailer();
		}
	}}
