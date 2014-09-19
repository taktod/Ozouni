package com.ttProject.ozouni.work;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.ttProject.frame.AudioFrame;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.aac.AacFrame;
import com.ttProject.frame.mp3.Mp3Frame;
import com.ttProject.frame.nellymoser.NellymoserFrame;
import com.ttProject.frame.speex.SpeexFrame;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.frame.IFrameReader;
import com.ttProject.ozouni.frame.IFrameWriter;
import com.ttProject.ozouni.frame.worker.IFrameListener;
import com.ttProject.pipe.PipeHandler;
import com.ttProject.pipe.PipeManager;

/**
 * 音声の動作について、実行しておく
 * @author taktod
 */
public class FfmpegAudioWorkModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(FfmpegAudioWorkModule.class);
	/** 経過Pts */
	private long passedPts = 0;
	/** 映像に対する許可遅延量 */
	private long allowedDelay = 500;
	/** 最後に処理したaudioFrame */
	private IAudioFrame lastAudioFrame = null;
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
		this.command = command.replaceAll("\\$\\\\", "\\$");
	}
	/**
	 * 環境変数設定
	 * @param env
	 */
	public void setEnvExtra(Properties env) {
		for(Object key : env.keySet()) {
			String sKey = (String)key;
			this.envExtra.put(sKey, env.getProperty(sKey));
		}
	}
	/**
	 * 処理IDを設定
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * 許容delay用を設定
	 * @param delay ミリ秒
	 */
	public void setAllowedDelay(long delay) {
		this.allowedDelay = delay;
	}
	public void setWriter(IFrameWriter writer) {
		this.writer = writer;
	}
	public void setReader(IFrameReader reader) {
		this.reader = reader;
	}
	/**
	 * {@inheritDoc}
	 * @param workModule
	 */
	@Override
	public void setWorkModule(IWorkModule workModule) {
		this.workModule = workModule;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(int num) throws Exception {
	}
	/**
	 * コンストラクタ
	 */
	public FfmpegAudioWorkModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("FfmpegAudioWorkThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		exec = Executors.newCachedThreadPool(factory);
		handler = pipeManager.getPipeHandler("audioConvert");
	}
	/**
	 * 動画フレームだった場合に動作を調整する
	 * @param frame
	 * @return false 処理すべきでない true 処理すべき
	 */
	private boolean checkVideoFrame(IFrame frame) throws Exception {
		// 映像フレームでなかったら関係ない
		if(!(frame instanceof IVideoFrame)) {
			return true;
		}
		// 音声データが１つもきていない場合は、追加する無音frameが決定しないので、なにもしない
		if(lastAudioFrame == null) {
			lastAudioFrame = Mp3Frame.getMutedFrame(44100, 1, 16);
		}
		if(frame.getPts() > passedPts + allowedDelay) {
			// frameのptsが経過pts + 許容delayよりも大きい場合
			// こっちでは挿入する必要あり、ffmpegでは、フレームを適当に挿入してやると、変換を強制することが可能なため
			passedPts = frame.getPts() - allowedDelay;
			AudioFrame aFrame = null;
			// ここではすべてのcodecに対する無音frameを追加する感じにしておく。
			switch(lastAudioFrame.getCodecType()) {
			case AAC:
				aFrame = AacFrame.getMutedFrame(lastAudioFrame.getSampleRate(), lastAudioFrame.getChannel(), lastAudioFrame.getBit());
				break;
			case ADPCM_SWF:
//				aFrame = AdpcmswfFrame.getMutedFrame(lastAudioFrame.getSampleRate(), lastAudioFrame.getChannel(), lastAudioFrame.getBit());
				throw new Exception("adpcmSwfの無音frameは未確認です。");
//				break;
			case MP3:
				aFrame = Mp3Frame.getMutedFrame(lastAudioFrame.getSampleRate(), lastAudioFrame.getChannel(), lastAudioFrame.getBit());
				break;
			case NELLYMOSER:
				aFrame = NellymoserFrame.getMutedFrame(lastAudioFrame.getSampleRate(), lastAudioFrame.getChannel(), lastAudioFrame.getBit());
				break;
			case SPEEX:
				aFrame = SpeexFrame.getMutedFrame(lastAudioFrame.getSampleRate(), lastAudioFrame.getChannel(), lastAudioFrame.getBit());
				break;
			case PCM_ALAW:
				throw new Exception("pcm_alawの無音frameは未確認です。");
			case PCM_MULAW:
				throw new Exception("pcm_mulawの無音frameは未確認です。");
			case ADPCM_IMA_WAV:
			case OPUS:
			case VORBIS:
			default:
				throw new Exception("flvでは関係のないフレームでした。");
			}
			aFrame.setPts(passedPts);
			aFrame.setTimebase(1000);
			writeFrame(aFrame, 0x08);
		}
		return false;
	}
	/**
	 * 音声フレームを確認する
	 * @param frame
	 * @param pts
	 * @return true 処理すべき false 処理すべきでない
	 */
	private boolean checkAudioFrame(IFrame frame) throws Exception {
		// 音声フレームでなかったら関係ない
		if(!(frame instanceof IAudioFrame)) {
			return false;
		}
		IAudioFrame aFrame = (IAudioFrame)frame;
		if(lastAudioFrame != null) {
			// フレームデータが入れ替わっていないか確認する必要あり
			if(lastAudioFrame.getCodecType() != aFrame.getCodecType()
			|| lastAudioFrame.getChannel() != aFrame.getChannel()
			|| lastAudioFrame.getSampleRate() != aFrame.getSampleRate()
			|| lastAudioFrame.getBit() != aFrame.getBit()) {
				logger.info("データが変更になっているので、なんとかしておかないとだめ。");
				// 今までの接続があったら切っておく
				openFlvTagWriter();
			}
		}
		// あとは問題ないので、frameを追記しておく。
		// 音声フレームだった場合
		if(frame.getPts() < passedPts) {
			// 過去のフレームだったら追加してもffmpegが混乱するだけなので、捨てる
			return false;
		}
		return true;
	}
	private synchronized void initializePipe() {
		if(future == null) {
			try {
				logger.info(command);
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
		if(!checkVideoFrame(frame)) {
			return;
		}
		if(!checkAudioFrame(frame)) {
			return;
		}
		// 特に問題ないので、このframeを書き込む
		passedPts = frame.getPts();
		writeFrame(frame, id);
		lastAudioFrame = (IAudioFrame)frame;
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
								logger.error("フレームの取得動作で例外が発生しました", e);
							}
						}
					});
					reader.start(handler.getReadChannel());
				}
				catch(Exception e) {
					// ここの例外が発生することがあるっぽいです・・・
					e.printStackTrace();
				}
			}
		});
		writer.setFileName(handler.getPipeTarget().getAbsolutePath());
		// ここでは設定する必要なし。(このwriterでは、prepareHeaderでの設定が強制されている。)
		writer.prepareHeader();
	}
	private void writeFrame(IFrame frame, int id) throws Exception {
		writer.addFrame(id, frame);
	}
}
