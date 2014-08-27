package com.ttProject.ozouni.work;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.frame.IFrameReader;
import com.ttProject.ozouni.frame.IFrameWriter;
import com.ttProject.ozouni.frame.worker.IFrameListener;
import com.ttProject.pipe.PipeHandler;
import com.ttProject.pipe.PipeManager;

/**
 * 映像の動作について、実行しておく
 * @author taktod
 */
public class FfmpegVideoWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FfmpegVideoWorkModule.class);
	/** 経過Pts */
	private long passedPts = 0;
	/** 最後に処理したvideoFrame */
	private IVideoFrame lastVideoFrame = null;
	/** pipeline動作のマネージャー */
	private PipeManager pipeManager = new PipeManager();
	/** pipeline動作のhandler */
	private PipeHandler handler = null;
	/** マルチスレッド動作 */
	private final ExecutorService exec;
	/** マルチスレッドの制御用future */
	private Future<?> future = null;
	/** 処理ID */
	private int id = -1;
	/** 外部から設定するデータ */
	/** 動作コマンド */
	private String command;
	/** 追加環境変数 */
	private Map<String, String> envExtra = new HashMap<String, String>();
	/** pipeへの書き込み処理 */
	private IFrameWriter writer = null;
	/** pipeプロセスの標準出力読み込み処理 */
	private IFrameReader reader = null;
	/** 次のworkModule */
	private IWorkModule workModule = null;
	/**
	 * コマンドの設定
	 * @param command
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	/**
	 * 環境変数設定
	 * @param env
	 */
	public void setEnvExtra(Map<String, String> env) {
		this.envExtra.putAll(env);
	}
	/**
	 * 処理IDを設定
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
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
	public FfmpegVideoWorkModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("FfmpegVideoWorkThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		exec = Executors.newCachedThreadPool(factory);
		handler = pipeManager.getPipeHandler("videoConvert");
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
	private synchronized void initializePipe() {
		if(future == null) {
			try {
				handler.setCommand(command);
				handler.setEnvExtra(envExtra);
				openFlvTagWriter();
			}
			catch(Exception e) {
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		initializePipe();
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
		writer.prepareTailer();
		if(future != null) {
			future.cancel(true);
		}
		handler.close();
		handler.executeProcess();
		future = exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					reader.setFrameListener(new IFrameListener() {
						@Override
						public void receiveFrame(IFrame frame) {
							try {
								workModule.pushFrame(frame, id);
							}
							catch(Exception e) {
							}
						}
					});
					reader.start(handler.getReadChannel());
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		writer.setFileName(handler.getPipeTarget().getAbsolutePath());
		writer.prepareHeader();
	}
	private void writeFrame(IFrame frame, int id) throws Exception {
		writer.addFrame(id, frame);
	}
}
