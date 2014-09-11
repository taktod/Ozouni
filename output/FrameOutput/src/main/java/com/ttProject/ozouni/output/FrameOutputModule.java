package com.ttProject.ozouni.output;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ttProject.frame.CodecType;
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
import com.ttProject.ozouni.frame.ShareFrameData;
import com.ttProject.unit.extra.bit.Bit8;

/**
 * frameServerとして、ozouniシステム間でデータを共有するモジュール
 * @author taktod
 * // クライアントソフトウェアがアクセスした場合には、はじめに、frameデータを送ってやった方がいいかも・・・
 * できたらkeyFrameデータでないと初回起動がエラーになることがままありそう。
 * そのフレームがはじめてきたかという情報がほしいかもしれない・・・
 * codecPrivateがかわっているか判定すればいいか？
 */
public class FrameOutputModule implements IOutputModule {
	/** ロガー */
//	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(FrameOutputModule.class);
	/** reportDataを引き出すために、signalWorkerを参照します。*/
	@Autowired
	private ISignalModule signalWorker;
	/** データ送信用のDataHandler設定 */
	private ISendDataHandler sendDataHandler = null;
	/** privateDataのhashCodeを保持しておいて、変更があるかわかるようになっている */
	private Map<Integer, Integer> privateDataCodeList = new HashMap<Integer, Integer>();
	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void setWorkModule(IWorkModule workModule) {
	}
	/**
	 * データ送信handlerを設定する
	 * @param sendDataHandler
	 */
	@Override
	public void setSendDataHandler(ISendDataHandler sendDataHandler) {
		this.sendDataHandler = sendDataHandler;
		ReportData reportData = signalWorker.getReportData();
		reportData.setMethod(sendDataHandler.getMethod());
		reportData.setKey(sendDataHandler.getKey());
	}
	/**
	 * frameを送信します。
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		// マルチフレームは分解して送ります。
		if(frame instanceof AudioMultiFrame) {
			AudioMultiFrame multiFrame = (AudioMultiFrame)frame;
			for(IAudioFrame audioFrame : multiFrame.getFrameList()) {
				pushFrame(audioFrame, id);
			}
			return;
		}
		else if(frame instanceof VideoMultiFrame) {
			VideoMultiFrame multiFrame = (VideoMultiFrame)frame;
			for(IVideoFrame videoFrame : multiFrame.getFrameList()) {
				pushFrame(videoFrame, id);
			}
			return;
		}
		if(frame == null) {
			// frameがnullの場合はほっとく。
			return;
		}
		// TODO 該当トラックの該当フレームが初めて来たかの判定がほしい。
		switch(frame.getCodecType()) {
		case VORBIS: // vorbisの場合はprivateデータがあります。
			Integer hashCode = privateDataCodeList.get(id);
			if(hashCode == null || hashCode.intValue() != frame.getPrivateData().hashCode()) {
				// hashCodeを確認して違う場合は初データであると判断できます。
				ByteBuffer privateData = frame.getPrivateData();
				privateDataCodeList.put(id, privateData.hashCode());
				// privateDataをつくって、hashCode
				logger.info("privateDataをつくって共有する必要があります。");
				// 現状接続しているクライアントがいる場合はデータを送る必要があるので、送信しておく
				ShareFrameData initFrameData = new ShareFrameData(frame.getCodecType(), new Bit8(0x80), frame, id);
				initFrameData.setFrameData(privateData);
				sendDataHandler.setInitialData(id, initFrameData.getShareData()); // 一番はじめに接続したときに、送信しておくデータを登録しておく。
			}
			break;
		case THEORA: // theoraにもprivateデータがあります。
		default:
			break;
		}
		// そうすればそのCodecPrivateデータを共有するか決めることができるかね。
		// 前回のデータと違うか確認して、ちがったらデータを送らないとだめっぽい。
		// この部分でframeがmultiFrameだったら分解しておく必要がある。
		// 処理フレームの値を記録する動作が必要
		ReportData reportData = signalWorker.getReportData();
		// frameが戻るようなことがあったらこまるが・・・
		// とりあえず戻らないようにチェックだけやっとく
		if(reportData.getFramePts() < frame.getPts()) {
			reportData.setFramePts(frame.getPts());
		}
		// 現在時刻を登録しておく
		reportData.setLastUpdateTime(System.currentTimeMillis());
		// h264のframeの場合はちょっと特殊なことをやる必要がある。
		CodecType codecType = frame.getCodecType();
		// trackIdを作成する必要がある。
		ShareFrameData shareFrameData = new ShareFrameData(codecType, new Bit8(), frame, id);
		ByteBuffer buffer = null;
		if(codecType == CodecType.H264) { // h265でも同じようなことしないとだめかもしれない。
			buffer = frame.getPackBuffer();
		}
		else {
			buffer = frame.getData();
		}
		// データ実態が取得できない場合は処理しない。
		if(buffer == null) {
			return;
		}
		shareFrameData.setFrameData(buffer);
		sendDataHandler.pushData(shareFrameData.getShareData());
	}
}
