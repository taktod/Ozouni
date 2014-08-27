package com.ttProject.ozouni.frame;

import java.io.FileOutputStream;
import java.nio.channels.WritableByteChannel;

import com.ttProject.container.IWriter;

/**
 * myLib.containerのIWriterの拡張
 * @author taktod
 */
public interface IFrameWriter extends IWriter {
	/**
	 * 記入先ファイル名設定
	 * @param fileName
	 */
	public void setFileName(String fileName) throws Exception;
	/**
	 * 記入先ファイル設定
	 * @param file
	 */
	public void setOutputStream(FileOutputStream outputStream) throws Exception;
	/**
	 * 記入先チャンネル設定
	 * @param outputChannel
	 */
	public void setOutputChannel(WritableByteChannel outputChannel) throws Exception;
}
