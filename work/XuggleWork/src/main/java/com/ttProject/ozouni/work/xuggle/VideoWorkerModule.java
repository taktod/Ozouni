package com.ttProject.ozouni.work.xuggle;

import org.apache.log4j.Logger;

import com.ttProject.frame.IVideoFrame;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 映像の動作について、実行しておく
 * @author taktod
 */
public class VideoWorkerModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(VideoWorkerModule.class);
	/** 経過pts */
	private long passedPts = 0;
	/** 最後に処理したvideoFrame */
	private IVideoFrame lastVideoFrame = null;
	/** 次の処理として割り当てておくworkModule */
	private IWorkModule workModule = null;
	/**
	 * @param workModule
	 */
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	
}
