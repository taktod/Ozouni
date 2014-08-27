package com.ttProject.ozouni.work.ffmpeg;

import java.util.HashMap;
import java.util.Map;
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
import com.ttProject.ozouni.frame.FlvAudioWriter;
import com.ttProject.ozouni.frame.IFrameWriter;
import com.ttProject.ozouni.frame.MkvReader;
import com.ttProject.ozouni.frame.worker.IFrameListener;
import com.ttProject.pipe.PipeHandler;
import com.ttProject.pipe.PipeManager;

/**
 * 音声の動作について実行しておく
 * やること
 * ・音声フレームが途切れて再開した場合に、sampleRateやchannel数に変化がない場合は同じトラックとして動作するようにすること
 * ・映像フレームの進み具合を確認しつつ、音声フレームが抜けていることがわかったら、無音の音声フレームを適宜挿入してやる必要がある
 * 
 * とりあえず映像フレームとの差分が１秒以上になったら無音frameを挿入して補完してやる
 * 
 * 映像データがきた場合
 * 映像Pts < passedPts + allowedDelayForVideo →なにもしない
 * それ以外passedPtsが映像Ptsの1秒前までになるように無音frameを挿入してやる
 * 
 * 音声データがきた場合
 * codecが違う場合 → 動作やり直し
 * passedPts < pts → 抜けている部分に無音frame挿入して追記する
 * 
 * 面倒なので、無音frameを1つだけ挿入する形でごまかして、asyncで無音を埋めておく
 * 
 * 一番初めに音声データとしてなにがくるかわからない状態ができるのがちとこまった感じ。
 * (iOSLiveTurboの動作では、音声データの連続が動作の起点になるため、ぬけている部分があると困る。)
 * @author taktod
 */
public class AudioWorkerModule implements IWorkModule {
	/** ロガー */
	private Logger logger = Logger.getLogger(AudioWorkerModule.class);
	/** 経過Pts */
	private long passedPts = 0;
	/** 映像に対する許可遅延量 */
	private final long allowedDelayForVideo = 500;
	/** 最後に処理したaudioFrame */
	private IAudioFrame lastAudioFrame = null;
	private IFrameWriter writer = new FlvAudioWriter();
//	private FlvTagWriter writer = null;
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
	public AudioWorkerModule() {
		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("AudioWorkerThread:" + t.hashCode());
				t.setDaemon(true);
				return t;
			}
		};
		exec = Executors.newCachedThreadPool(factory);
		try {
			handler = pipeManager.getPipeHandler("audioConvert");
			Map<String, String> envExtra = new HashMap<String, String>();
			envExtra.put("LD_LIBRARY_PATH", "/usr/local/lib");
			handler.setCommand("avconv -copyts -i ${pipe} -acodec adpcm_ima_wav -ar 44100 -ac 1 -async 2 -f matroska - 2>avconv.audio.log");
			handler.setEnvExtra(envExtra);
			openFlvTagWriter();
		}
		catch(Exception e) {
			logger.error("初期化で例外が発生しました。", e);
		}
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
		if(frame.getPts() > passedPts + allowedDelayForVideo) {
			// frameのptsが経過pts + 許容delayよりも大きい場合
			// こっちでは挿入する必要あり、ffmpegでは、フレームを適当に挿入してやると、変換を強制することが可能なため
			passedPts = frame.getPts() - allowedDelayForVideo;
			AudioFrame aFrame = null;
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
	/**
	 * フレームを受け入れる
	 * @param frame
	 * @param id
	 * @throws Exception
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
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
		System.out.println("openFlvTagWirterを実施");
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
					MkvReader reader = new MkvReader();
					reader.setFrameListener(new IFrameListener() {
						@Override
						public void receiveFrame(IFrame frame) {
							try {
								workModule.pushFrame(frame, 0x08);
							}
							catch(Exception e) {
								logger.error("フレームの取得動作で例外が発生しました。", e);
								throw new RuntimeException(e.getMessage());
							}
						}
					});
					reader.start(handler.getReadChannel());
/*					IReadChannel channel = handler.getReadChannel();
					MkvTagReader reader = new MkvTagReader();
					IContainer container = null;
					while((container = reader.read(channel)) != null) {
						if(container instanceof MkvBlockTag) {
							MkvBlockTag blockTag = (MkvBlockTag)container;
							IFrame frame = blockTag.getFrame();
							workModule.pushFrame(frame, 0x08);
						}
					}*/
				}
				catch(Exception e) {
					// ここの例外が発生することがあるっぽいです・・・
					e.printStackTrace();
				}
			}
		});
//		writer = new FlvTagWriter(handler.getPipeTarget().getAbsolutePath());
		writer.setFileName(handler.getPipeTarget().getAbsolutePath());
		writer.prepareHeader();
	}
	private void writeFrame(IFrame frame, int id) throws Exception {
		writer.addFrame(id, frame);
	}
}
