package com.ttProject.ozouni.work.xuggle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
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
	/** Threadのexecutor */
	private final ExecutorService exec;
	/**
	 * @param workModule
	 */
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * コンストラクタ
	 */
	public VideoWorkerModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("VideoWorkerThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		exec = Executors.newCachedThreadPool(factory);
	}
	/**
	 * 音声フレームだった場合、動作を調整する
	 * @param frame
	 * @return true 処理すべき false 処理すべきでない
	 * @throws Exception
	 */
	private boolean checkAudioFrame(IFrame frame) throws Exception {
		if(!(frame instanceof IAudioFrame)) {
			return true;
		}
		// 特になにもしない、変換が遅れても特にやることはない
		return false;
	}
	/**
	 * 動画フレームだった場合に動作を確認する
	 * @param frame
	 * @return true 処理すべき false 処理すべきでない
	 * @throws Exception
	 */
	private boolean checkVideoFrame(IFrame frame) throws Exception {
		if(!(frame instanceof IVideoFrame)) {
			return false;
		}
		IVideoFrame vFrame = (IVideoFrame)frame;
		// コーデックの変更については、StreamCoderで吸収できるので問題なし
/*		if(lastVideoFrame != null) {
			if(lastVideoFrame.getCodecType() != vFrame.getCodecType()
			|| lastVideoFrame.getWidth() != vFrame.getWidth()
			|| lastVideoFrame.getHeight() != vFrame.getHeight()) {
				logger.info("データが変更になっているので、なんとかしておかないとだめ");
				// 今までの接続があったら切っておく
			}
		}*/
		// 過去のデータをうけとった場合は処理できない
		if(frame.getPts() < passedPts) {
			return false;
		}
		return true;
	}
	/**
	 * フレームを受け入れる
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	public void pushFrame(IFrame frame, int id) throws Exception {
		// 音声フレームとの兼ね合いを調べる
		if(!checkAudioFrame(frame)) {
			return;
		}
		// 映像フレームの兼ね合いを調べる
		if(!checkVideoFrame(frame)) {
			return;
		}
		// 問題なければ書き込む
		passedPts = frame.getPts();
		lastVideoFrame = (IVideoFrame)frame;
		// このタイミングでframeの変換のthreadにまわす必要あり
	}
}
