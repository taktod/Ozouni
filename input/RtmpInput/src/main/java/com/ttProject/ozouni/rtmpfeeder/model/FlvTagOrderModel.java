package com.ttProject.ozouni.rtmpfeeder.model;

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
 * @author taktod
 */
public class FlvTagOrderModel {
	/** ソート用比較オブジェクト */
	private static final FlvTagComparator comparator = new FlvTagComparator();
	private List<FlvTag> videoTags = new ArrayList<FlvTag>();
	private List<FlvTag> audioTags = new ArrayList<FlvTag>();
	/**
	 * tagを追加する
	 */
	public void addTag(FlvTag tag) {
		if(tag instanceof AggregateTag) {
			AggregateTag aTag = (AggregateTag) tag;
			for(FlvTag fTag : aTag.getList()) {
				addTag(fTag);
			}
			return;
		}
		if(tag instanceof VideoTag) {
			videoTags.add(tag);
		}
		else if(tag instanceof AudioTag){
			audioTags.add(tag);
		}
	}
	public List<FlvTag> getAudioCompleteTag() {
		Collections.sort(audioTags, comparator);
		List<FlvTag> result = new ArrayList<FlvTag>();
		while(audioTags.size() > 1) {
			result.add(audioTags.remove(0));
		}
		return result;
	}
	public List<FlvTag> getVideoCompleteTag() {
		Collections.sort(videoTags, comparator);
		List<FlvTag> result = new ArrayList<FlvTag>();
		while(videoTags.size() > 1) {
			result.add(videoTags.remove(0));
		}
		return result;
	}
	/**
	 * tagの比較クラス
	 * @author taktod
	 *
	 */
	public static class FlvTagComparator implements Comparator<FlvTag> {
		@Override
		public int compare(FlvTag tag1, FlvTag tag2) {
			return (int)(tag1.getPts() - tag2.getPts());
		}
	}
}
