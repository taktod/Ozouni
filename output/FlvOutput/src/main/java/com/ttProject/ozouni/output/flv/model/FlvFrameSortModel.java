package com.ttProject.ozouni.output.flv.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ttProject.frame.IAudioFrame;
import com.ttProject.frame.IFrame;
import com.ttProject.frame.IVideoFrame;

/**
 * flvのtagのソートを実施するモデル
 * ozouniシステム内のデータは各トラックのデータはソートしているのだが、トラックをまたがるデータのソートはしていません。
 * よって、音声と映像の同期がずれることがありえます。
 * それをソートするためのモデル
 * @author taktod
 */
public class FlvFrameSortModel {
	/** ロガー */
	private Logger logger = Logger.getLogger(FlvFrameSortModel.class);
	private final boolean videoFlg;
	private final boolean audioFlg;
	private List<IAudioFrame> audioFrameList = new ArrayList<IAudioFrame>();
	private List<IVideoFrame> videoFrameList = new ArrayList<IVideoFrame>();
	private List<IFrame> completeFrameList = new ArrayList<IFrame>();
	private int videoId = -1;
	private int audioId = -1;
	/**
	 * videoId参照
	 * @return
	 */
	public int getVideoId() {
		return videoId;
	}
	/**
	 * audioId参照
	 * @return
	 */
	public int getAudioId() {
		return audioId;
	}
	/**
	 * コンストラクタ
	 * @param videoFlg
	 * @param audioFlg
	 */
	public FlvFrameSortModel(boolean videoFlg, boolean audioFlg) {
		this.videoFlg = videoFlg;
		this.audioFlg = audioFlg;
	}
	/**
	 * フレームを追加します。
	 * @param frame
	 * @param id
	 */
	public void addFrame(IFrame frame, int id) throws Exception {
		if(frame instanceof IVideoFrame) {
			if(!videoFlg) {
				return;
			}
			logger.info("videoAdd:" + frame.getPts());
			if(videoId != -1 && videoId != id) {
				throw new Exception("videoFrameIdがかわっています。");
			}
			if(audioFlg) {
				// audioもためている場合は比較して、completeFrameListにいれる
				videoFrameList.add((IVideoFrame)frame); // 先に足す
				while(videoFrameList.size() != 0) {
					IVideoFrame vFrame = videoFrameList.get(0);
					double pos = 1D * vFrame.getPts() / vFrame.getTimebase();
					while(audioFrameList.size() != 0) {
						IAudioFrame aFrame = audioFrameList.get(0);
						double apos = 1D * aFrame.getPts() / aFrame.getTimebase();
						logger.info("apos:" + apos + " pos:" + pos);
						if(apos > pos) {
							break;
						}
						// 音声フレームの方が映像フレームより若い場合登録されている音声フレームは・・・
						completeFrameList.add(audioFrameList.remove(0));
					}
					if(audioFrameList.size() == 0) {
						break;
					}
					completeFrameList.add(videoFrameList.remove(0));
				}
			}
			else {
				// videoのみの場合は比較するまでもなく、completeListにいれる
				completeFrameList.add(frame);
			}
		}
		else if(frame instanceof IAudioFrame) {
			if(!audioFlg) {
				return;
			}
			logger.info("audioAdd:" + frame.getPts());
			if(audioId != -1 && audioId != id) {
				throw new Exception("audioFrameIdがかわっています。");
			}
			if(videoFlg) {
				audioFrameList.add((IAudioFrame)frame);
				while(audioFrameList.size() != 0) {
					IAudioFrame aFrame = audioFrameList.get(0);
					double pos = 1D * frame.getPts() / frame.getTimebase();
					while(videoFrameList.size() != 0) {
						IVideoFrame vFrame = videoFrameList.get(0);
						double vpos = 1D * vFrame.getPts() / vFrame.getTimebase();
						logger.info("vpos:" + vpos + " pos:" + pos);
						if(vpos > pos) {
							break;
						}
						completeFrameList.add(videoFrameList.remove(0));
					}
					if(videoFrameList.size() == 0) {
						break;
					}
					completeFrameList.add(audioFrameList.remove(0));
				}
			}
			else {
				completeFrameList.add(frame);
			}
		}
		logger.info("vls:" + videoFrameList.size());
		logger.info("sls:" + audioFrameList.size());
	}
	/**
	 * ソート済みのtagを参照します。
	 * @return
	 */
	public List<IFrame> getOrderedTags() {
		List<IFrame> result = new ArrayList<IFrame>(completeFrameList);
		completeFrameList.clear();
		return result;
	}
}
