package com.ttProject.ozouni.work;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ttProject.container.IContainer;
import com.ttProject.container.IReader;
import com.ttProject.container.mkv.MkvBlockTag;
import com.ttProject.container.mkv.MkvTagReader;
import com.ttProject.convertprocess.ProcessHandler;
import com.ttProject.convertprocess.ProcessManager;
import com.ttProject.convertprocess.process.FlvAudioOutputEntry;
import com.ttProject.convertprocess.process.FlvOutputEntry;
import com.ttProject.convertprocess.process.FlvVideoOutputEntry;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 変換動作モジュール
 * @author taktod
 *
 */
public class ConvertWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(ConvertWorkModule.class);
	/** 動作プロセスマネージャー */
	private ProcessManager manager;
	/** 動作開始フラグ */
	private boolean startFlg = false;
	/**
	 * コンストラクタ
	 */
	public ConvertWorkModule() throws Exception {
		try {
			manager = new ProcessManager();
		}
		catch(Exception e) {
			logger.error("例外が発生しました。", e);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void pushFrame(IFrame frame, int id) throws Exception {
		// この部分で単体フレームがくるとは限らないので、multiFrame対策やっといた方がいいと思う。
		if(!startFlg) {
			// 開始処理を実施する
			// 必要なプロセスをつくっておく。
			// プロセスからデータを取り出したときの動作をつくっておく。
			final ProcessHandler videoHandler = manager.getProcessHandler("video");
			final ProcessHandler audioHandler = manager.getProcessHandler("audio");
			videoHandler.setTargetClass(FlvVideoOutputEntry.class);
			videoHandler.setCommand("/usr/local/bin/avconv -copyts -i - -vcodec mjpeg -s 160x120 -g 10 -q 20 -f matroska - 2>ffmpeg.video.log");
			audioHandler.setTargetClass(FlvAudioOutputEntry.class);
			audioHandler.setCommand("/usr/local/bin/avconv -copyts -i - -acodec adpcm_ima_wav -ac 1 -ar 22050 -f matroska - 2>ffmpeg.audio.log");
/*			final ProcessHandler handler = manager.getProcessHandler("all");
			handler.setTargetClass(FlvOutputEntry.class);
			handler.setCommand("/usr/local/bin/avconv -copyts -i - acodec adpcm_ima_wav -ac 1 -ar 22050 -vcodec mjpeg -s 160x120 -g 10 -q 20 -f matroska - 2>ffmpeg.log");*/
			manager.start(); // 動作開始
			ExecutorService executor = Executors.newFixedThreadPool(2);
			Thread.sleep(1000);
			// 映像の出力結果を受け取る
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						IReadChannel channel = videoHandler.getReadChannel();
						IReader reader = new MkvTagReader();
						IContainer container = null;
						while((container = reader.read(channel)) != null) {
							if(container instanceof MkvBlockTag) {
								MkvBlockTag blockTag = (MkvBlockTag)container;
								logger.info(blockTag.getFrame());
							}
						}
					}
					catch(Exception e) {
					}
				}
			});
			// 音声の出力結果を受け取る
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						IReadChannel channel = videoHandler.getReadChannel();
						IReader reader = new MkvTagReader();
						IContainer container = null;
						while((container = reader.read(channel)) != null) {
							if(container instanceof MkvBlockTag) {
								MkvBlockTag blockTag = (MkvBlockTag)container;
								logger.info(blockTag.getFrame());
							}
						}
					}
					catch(Exception e) {
					}
				}
			});// */
/*			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						IReadChannel channel = handler.getReadChannel();
						IReader reader = new MkvTagReader();
						IContainer container = null;
						while((container = reader.read(channel)) != null) {
							if(container instanceof MkvBlockTag) {
								MkvBlockTag blockTag = (MkvBlockTag)container;
								logger.info(blockTag.getFrame());
							}
						}
					}
					catch(Exception e) {
					}
				}
			});// */
			startFlg = true;
		}
//		logger.info(frame);
		// とりあえずここまでデータをもってくることはできた。
		// flvデータをmkvデータに変換して動作させてみます。
		manager.pushFrame(frame, id);
	}
}
