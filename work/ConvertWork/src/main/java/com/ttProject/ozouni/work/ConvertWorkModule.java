package com.ttProject.ozouni.work;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.ttProject.nio.channels.ReadableByteReadChannel;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * 変換動作モジュール
 * @author taktod
 *
 */
public class ConvertWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(ConvertWorkModule.class);
	private boolean startFlg = false;
	private FlvTagWriter audioWriter = null;
	private FlvTagWriter videoWriter = null;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void pushFrame(IFrame frame, int id) throws Exception {
		if(!startFlg) {
			initialize();
			startFlg = true;
		}
		// その他いろいろやっておく。
		if(frame instanceof IVideoFrame) {
			try {
				if(videoWriter != null) {
					videoWriter.addFrame(id, frame);
				}
			}
			catch(IOException e) {
				logger.info("videopipeが壊れました。", e);
			}
		}
		else if(frame instanceof IAudioFrame) {
			try {
				if(audioWriter != null) {
					audioWriter.addFrame(id, frame);
				}
			}
			catch(IOException e) {
				logger.info("audiopipeが壊れました。", e);
			}
		}
	}
	private void initialize() throws Exception {
		// ここの処理をつくっていきます。
		makeNamedPipes();
		// 音声は、無音frameを挿入して、なんとかしておく。(これはあとで考えるようにしておこう。)
		// 映像は映像だけ、あるだけつくる感じにしておく。
		// processBuilderを利用して、出力に関しては、このプログラムでうけとっておく。
		// processBuilderをつくって、出力を取り出せるようにしておく
		makeConvertProcesses();
		
		// flv出力なので、flv出力をつくっておく。
		setupFlvWriters();
	}
	private void setupFlvWriters() throws Exception {
		audioWriter = new FlvTagWriter("audio");
		videoWriter = new FlvTagWriter("video");
		FlvHeaderTag audioHeaderTag = new FlvHeaderTag();
		audioHeaderTag.setAudioFlag(true);
		audioHeaderTag.setVideoFlag(false);
		audioWriter.addContainer(audioHeaderTag);
		FlvHeaderTag videoHeaderTag = new FlvHeaderTag();
		videoHeaderTag.setAudioFlag(false);
		videoHeaderTag.setVideoFlag(true);
		videoWriter.addContainer(videoHeaderTag);
		// ここまででheaderの書き込みOK
	}
	private void makeConvertProcesses() throws Exception {
		// 音声
		StringBuilder audioCommand = new StringBuilder();
		audioCommand.append("avconv -copyts -i audio -acodec adpcm_ima_wav -ar 22050 -ac 1 -f matroska -");
		ProcessBuilder audioProcessBuilder = new ProcessBuilder("/bin/bash", "-c", audioCommand.toString());
		audioProcessBuilder.environment().put("LD_LIBRARY_PATH", "/usr/local/lib");
		// 映像
		StringBuilder videoCommand = new StringBuilder();
		videoCommand.append("avconv -copyts -i video -vcodec mjpeg -q 10 -r 10 -s 160x120 -f matroska -");
		ProcessBuilder videoProcessBuilder = new ProcessBuilder("/bin/bash", "-c", videoCommand.toString());
		videoProcessBuilder.environment().put("LD_LIBRARY_PATH", "/usr/local/lib");

		final Process audioProcess = audioProcessBuilder.start();
		final Process videoProcess = videoProcessBuilder.start();
		ExecutorService ex = Executors.newCachedThreadPool();
		ex.execute(new Runnable() {
			@Override
			public void run() {
				try {
					IReadChannel channel = new ReadableByteReadChannel(Channels.newChannel(audioProcess.getInputStream()));
					MkvTagReader reader = new MkvTagReader();
					IContainer container = null;
					while((container = reader.read(channel)) != null) {
						if(container instanceof MkvBlockTag) {
							MkvBlockTag blockTag = (MkvBlockTag)container;
							IFrame frame = blockTag.getFrame();
							logger.info(frame + ":" + frame.getPts() + "/" + frame.getTimebase());
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		ex.execute(new Runnable() {
			@Override
			public void run() {
				try {
					IReadChannel channel = new ReadableByteReadChannel(Channels.newChannel(videoProcess.getInputStream()));
					MkvTagReader reader = new MkvTagReader();
					IContainer container = null;
					while((container = reader.read(channel)) != null) {
						if(container instanceof MkvBlockTag) {
							MkvBlockTag blockTag = (MkvBlockTag)container;
							IFrame frame = blockTag.getFrame();
							logger.info(frame + ":" + frame.getPts() + "/" + frame.getTimebase());
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void makeNamedPipes() throws Exception {
		namedPipe("audio");
		namedPipe("video");
	}
	private void namedPipe(String name) throws Exception {
		new File(name).delete();
		StringBuilder command = new StringBuilder();
		command.append("mkfifo " + name);
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command.toString());
		Process p = builder.start();
		p.waitFor();
	}
}
