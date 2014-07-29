package com.ttProject.ozouni.input;

import org.apache.log4j.Logger;

import com.ttProject.container.IContainer;
import com.ttProject.container.mkv.MkvBlockTag;
import com.ttProject.container.mkv.MkvTagReader;
import com.ttProject.nio.channels.FileReadChannel;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.nio.channels.StdinReadChannel;
import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IOutputModule;

/**
 * matroska形式の入力モジュール
 * @author taktod
 */
public class MkvInputModule implements IInputModule {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(MkvInputModule.class);
	/** 出力モジュール */
	private IOutputModule outputModule = null;
	/** 該当ファイル名 */
	private String targetFile = null;
	/**
	 * 入力ファイル設定
	 * デフォルトは標準入力
	 * @param file
	 */
	public void setInputFile(String file) {
		targetFile = file;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOutputModule(IOutputModule outputModule) {
		this.outputModule = outputModule;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
		IReadChannel channel = null;
		if(targetFile == null) {
			channel = new StdinReadChannel();
		}
		else {
			channel = FileReadChannel.openFileReadChannel(targetFile);
		}
		MkvTagReader reader = new MkvTagReader();
		IContainer container = null;
		while((container = reader.read(channel)) != null) {
			if(container instanceof MkvBlockTag) {
				MkvBlockTag blockTag = (MkvBlockTag) container;
				blockTag.getFrame();
				outputModule.pushFrame(blockTag.getFrame(), blockTag.getTrackId().get());
			}
		}
	}
}
