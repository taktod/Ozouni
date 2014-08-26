package com.ttProject.ozouni.work.ffmpeg;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.container.IContainer;
import com.ttProject.container.flv.FlvHeaderTag;
import com.ttProject.container.flv.FlvTagWriter;
import com.ttProject.container.mkv.MkvBlockTag;
import com.ttProject.container.mkv.MkvTagReader;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.pipe.PipeHandler;
import com.ttProject.pipe.PipeManager;

/**
 * 映像の動作について、実行しておく
 * @author taktod
 */
public class VideoWorkerModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(VideoWorkerModule.class);
	/** 経過Pts */
	private long passedPts = 0;
	/** 最後に処理したvideoFrame */
	private IVideoFrame lastVideoFrame = null;
	private FlvTagWriter writer = null;
	private PipeManager pipeManager = new PipeManager();
	private PipeHandler handler = null;
	private final ExecutorService exec;
	private Future<?> future = null;
	private IWorkModule workModule = null;
	/**
	 * @param workModule
	 */
	@Override
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
		try {
			handler = pipeManager.getPipeHandler("videoConvert");
			Map<String, String> envExtra = new HashMap<String, String>();
			envExtra.put("LD_LIBRARY_PATH", "/usr/local/lib");
			handler.setCommand("avconv -copyts -i ${pipe} -vcodec mjpeg -s 320x240 -q 10 -r 10 -f matroska - 2>avconv.video.log");
			handler.setEnvExtra(envExtra);
			openFlvTagWriter();
		}
		catch(Exception e) {
		}
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
		if(lastVideoFrame != null) {
			if(lastVideoFrame.getCodecType() != vFrame.getCodecType()
			|| lastVideoFrame.getWidth() != vFrame.getWidth()
			|| lastVideoFrame.getHeight() != vFrame.getHeight()) {
				logger.info("データが変更になっているので、なんとかしておかないとだめ");
				// 今までの接続があったら切っておく
				openFlvTagWriter();
			}
		}
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
	@Override
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
		writeFrame(frame, id);
		lastVideoFrame = (IVideoFrame)frame;
	}
	private void openFlvTagWriter() throws Exception {
		if(writer != null) {
			writer.prepareTailer();
			future.cancel(true);
			handler.close();
		}
		handler.executeProcess();
		future = exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					IReadChannel channel = handler.getReadChannel();
					MkvTagReader reader = new MkvTagReader();
					IContainer container = null;
					while((container = reader.read(channel)) != null) {
						if(container instanceof MkvBlockTag) {
							MkvBlockTag blockTag = (MkvBlockTag)container;
							IFrame frame = blockTag.getFrame();
							workModule.pushFrame(frame, 0x09);
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		writer = new FlvTagWriter(handler.getPipeTarget().getAbsolutePath());
		FlvHeaderTag headerTag = new FlvHeaderTag();
		headerTag.setAudioFlag(false);
		headerTag.setVideoFlag(true);
		writer.addContainer(headerTag);
	}
	private void writeFrame(IFrame frame, int id) throws Exception {
		writer.addFrame(id, frame);
	}
}
