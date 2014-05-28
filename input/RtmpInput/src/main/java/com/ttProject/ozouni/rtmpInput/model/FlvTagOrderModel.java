package com.ttProject.ozouni.rtmpInput.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ttProject.container.flv.AggregateTag;
import com.ttProject.container.flv.FlvTag;
import com.ttProject.container.flv.type.AudioTag;
import com.ttProject.container.flv.type.VideoTag;

/**
 * flvのtagの整列を実施するモデル
 * 単純に２つ以上タグがある場合に新しいものになっているとして扱っておく。
 * @author taktod
 */
public class FlvTagOrderModel implements IFlvTagOrderModel {
	/** ソート用比較オブジェクト */
	private static final FlvTagComparator comparator = new FlvTagComparator();
	private List<FlvTag> videoTags = new ArrayList<FlvTag>();
	private List<FlvTag> audioTags = new ArrayList<FlvTag>();
	/** 処理済みvideoTagのpts値 */
	private long passedVideoPts = -1;
	/** 処理済みaudioTagのpts値 */
	private long passedAudioPts = -1;
	/**
	 * tagを追加する
	 */
	@Override
	public void addTag(FlvTag tag) {
		if(tag instanceof AggregateTag) {
			AggregateTag aTag = (AggregateTag) tag;
			for(FlvTag fTag : aTag.getList()) {
				addTag(fTag);
			}
			return;
		}
		if(tag instanceof VideoTag) {
			if(tag.getPts() < passedVideoPts) {
				// 処理済みのtimestamp以前のデータなら捨てておく
				return;
			}
			videoTags.add(tag);
		}
		else if(tag instanceof AudioTag){
			if(tag.getPts() < passedAudioPts) {
				// 処理済みのtimestamp以前のデータなら捨てておく
				return;
			}
			audioTags.add(tag);
		}
	}
	@Override
	public List<FlvTag> getAudioCompleteTag() {
		Collections.sort(audioTags, comparator);
		List<FlvTag> result = new ArrayList<FlvTag>();
		while(audioTags.size() > 1) {
			FlvTag tag = audioTags.remove(0);
			passedAudioPts = tag.getPts();
			result.add(tag);
		}
		return result;
	}
	@Override
	public List<FlvTag> getVideoCompleteTag() {
		Collections.sort(videoTags, comparator);
		List<FlvTag> result = new ArrayList<FlvTag>();
		while(videoTags.size() > 1) {
			FlvTag tag = videoTags.remove(0);
			passedVideoPts = tag.getPts();
			result.add(tag);
		}
		return result;
	}
	/**
	 * tagの比較クラス
	 * @author taktod
	 */
	public static class FlvTagComparator implements Comparator<FlvTag> {
		@Override
		public int compare(FlvTag tag1, FlvTag tag2) {
			return (int)(tag1.getPts() - tag2.getPts());
		}
	}
}
