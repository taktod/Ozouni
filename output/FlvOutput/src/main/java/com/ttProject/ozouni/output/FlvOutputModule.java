package com.ttProject.ozouni.output;

import java.nio.channels.Channels;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.container.flv.FlvHeaderTag;
import com.ttProject.container.flv.FlvTagWriter;
import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;
import com.ttProject.frame.extra.AudioMultiFrame;
import com.ttProject.frame.extra.VideoMultiFrame;
import com.ttProject.ozouni.base.IOutputModule;
import com.ttProject.ozouni.base.ISignalModule;
import com.ttProject.ozouni.base.IWorkModule;
import com.ttProject.ozouni.base.ReportData;
import com.ttProject.ozouni.dataHandler.ISendDataHandler;
import com.ttProject.ozouni.output.flv.model.FlvFrameSortModel;

/**
 * flvとしてデータを出力するモジュール
 * @author taktod
 */
public class FlvOutputModule implements IOutputModule {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(FlvOutputModule.class);
	/** 映像を取り扱うかフラグ */
	private boolean videoFlg = true;
	/** 音声を取り扱うかフラグ */
	private boolean audioFlg = true;
	/** 出力先設定 */
	private String targetFile = null;
	/** reportDataを引き出すためのsignalWorker */
	@Autowired
	private ISignalModule signalWorker;
	/** flvのフレームをソートするモデル */
	private FlvFrameSortModel sortModel = null;
	/** 開始フラグ */
	private boolean start = false; // 開始フラグ
	/** flvの書き込みwriter */
	private FlvTagWriter writer = null;
	/** 出力handlerを定義(とりあえず設定はつくっておくけど、当面つかわない) */
	@SuppressWarnings("unused")
	private ISendDataHandler sendDataHandler = null;
	/**
	 * 出力ファイル設定
	 * デフォルトは標準出力
	 * @param file
	 */
	public void setOutputFile(String file) {
		targetFile = file;
	}
	/**
	 * 音声を有効にするか？
	 * デフォルトは有効
	 * @param flg
	 */
	public void setEnableAudio(boolean flg) {
		audioFlg = flg;
	}
	/**
	 * 映像を有効にするか？
	 * デフォルトは有効
	 * @param flg
	 */
	public void setEnableVideo(boolean flg) {
		videoFlg = flg;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSendDataHandler(ISendDataHandler sendDataHandler) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		// フレームを確認
		if(!checkFrame(frame, id)) {
			return; // 処理できないなら処理おわり
		}
		if(sortModel == null) {
			sortModel = new FlvFrameSortModel(videoFlg, audioFlg);
		}
		if(writer == null) {
			if(targetFile == null) {
				writer = new FlvTagWriter(Channels.newChannel(System.out));
			}
			else {
				writer = new FlvTagWriter(targetFile);
			}
			FlvHeaderTag headerTag = new FlvHeaderTag();
			headerTag.setAudioFlag(audioFlg);
			headerTag.setVideoFlag(videoFlg);
			writer.addContainer(headerTag);
			// writerのcloseを実施したいので、ShutdownHookでcloseするようにしておこうと思います。
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						// writerの終端動作をshutdown時に実行しておく。
						writer.prepareTailer();
					}
					catch(Exception e) {
					}
				}
			});
		}
		sortModel.addFrame(frame, id);
		ReportData reportData = signalWorker.getReportData();
		for(IFrame completeFrame : sortModel.getOrderedTags()) {
			if(completeFrame instanceof IVideoFrame) {
				IVideoFrame vFrame = (IVideoFrame) completeFrame;
				if(vFrame.isKeyFrame()) {
					// キーフレームがみつかったので、ここから開始する。
					start = true;
				}
			}
			if(!start) {
				continue;
			}
			// ここまでこれたら完了しているデータなので、処理しておく。
			reportData.setFramePts(1000 * completeFrame.getPts() / completeFrame.getTimebase());
			reportData.setLastUpdateTime(System.currentTimeMillis());
			if(completeFrame instanceof IAudioFrame) {
				writer.addFrame(sortModel.getAudioId(), completeFrame);
			}
			else if(completeFrame instanceof IVideoFrame) {
				writer.addFrame(sortModel.getVideoId(), completeFrame);
			}
		}
	}
	/**
	 * フレームを確認しておく。
	 * @param frame
	 * @param id
	 * @return true:処理できる false:処理できない
	 * @throws Exception
	 */
	private boolean checkFrame(IFrame frame, int id) throws Exception {
		if(frame == null) {
			// 関数がよばれてもframeがnullだったら動作しない
			return false;
		}
		// マルチフレームは分解して、動作させる
		if(frame instanceof AudioMultiFrame) {
			AudioMultiFrame multiFrame = (AudioMultiFrame)frame;
			for(IAudioFrame audioFrame : multiFrame.getFrameList()) {
				pushFrame(audioFrame, id);
			}
			return false;
		}
		else if(frame instanceof VideoMultiFrame) {
			VideoMultiFrame multiFrame = (VideoMultiFrame)frame;
			for(IVideoFrame videoFrame : multiFrame.getFrameList()) {
				pushFrame(videoFrame, id);
			}
			return false;
		}
		return true;
	}
	@Override
	public void setWorkModule(IWorkModule workModule) {
		// TODO Auto-generated method stub
		
	}
}
