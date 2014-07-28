package com.ttProject.ozouni.input;

import org.apache.log4j.Logger;

import com.ttProject.container.IContainer;
import com.ttProject.container.mkv.MkvBlockTag;
import com.ttProject.container.mkv.MkvTagReader;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.nio.channels.StdinReadChannel;
import com.ttProject.ozouni.base.IInputModule;
import com.ttProject.ozouni.base.IOutputModule;

/**
 * 標準入力として、mkvを受け取るモジュール
 * @author taktod
 */
public class StdinMkvInputModule implements IInputModule {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(StdinMkvInputModule.class);
	/** 出力モジュール */
	private IOutputModule outputModule = null;
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
		// ここで開始します。
		IReadChannel channel = new StdinReadChannel();
		MkvTagReader reader = new MkvTagReader();
		IContainer container = null;
		while((container = reader.read(channel)) != null) {
			if(container instanceof MkvBlockTag) {
				MkvBlockTag blockTag = (MkvBlockTag) container;
				blockTag.getFrame(); // これがターゲットのframe
				// この段階でoutputFrameにデータをおくってやる
				outputModule.pushFrame(blockTag.getFrame(), blockTag.getTrackId().get());
			}
		}
	}
}
