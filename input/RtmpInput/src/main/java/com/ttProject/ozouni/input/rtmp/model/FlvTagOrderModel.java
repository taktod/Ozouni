package com.ttProject.ozouni.input.rtmp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ttProject.container.flv.AggregateTag;
import com.ttProject.container.flv.FlvTag;
import com.ttProject.container.flv.type.AudioTag;
import com.ttProject.container.flv.type.VideoTag;

/**
 * flvのtagの整列を実施するモデル
 * 単純に２つ以上タグがある場合に新しいものになっているとして扱っておく。
 * TODO 無音フレームのあとみたいに久しくデータをうけとった場合にptsの小さなデータが送り出される可能性があるので、注意が必要
 * とりあえず送信済みpts値未満のデータがあったら送らないようにしたけど、h264の前方参照がある場合とかちょっと心配
 * 音声については、sampleNumから次のデータの予測位置が割り出せるのでデータドロップなしでも動作可能なはず。
 * このあたりは今後の課題
 * @author taktod
 */
public class FlvTagOrderModel implements IFlvTagOrderModel {
	/** ロガー */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(FlvTagOrderModel.class);
	/** ソート用比較オブジェクト */
	private static final FlvTagComparator comparator = new FlvTagComparator();
	private List<FlvTag> videoTags = new ArrayList<FlvTag>();
	private List<FlvTag> audioTags = new ArrayList<FlvTag>();
	/** 処理済みvideoTagのpts値 */
	private long passedVideoPts = -1;
	/** 処理済みaudioTagのpts値 */
	private long passedAudioPts = -1;
	/**
	 * 内部のデータをリセットする
	 * 主にunpublishしたときの処理
	 */
	@Override
	public void reset() {
		videoTags.clear();
		audioTags.clear();
		passedVideoPts = -1;
		passedAudioPts = -1;
	}
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
	/**
	 * 音声のソート済みデータを取得する
	 */
	@Override
	public List<FlvTag> getAudioCompleteTag() {
		Collections.sort(audioTags, comparator);
		List<FlvTag> result = new ArrayList<FlvTag>();
		while(audioTags.size() > 1) {
			FlvTag tag = audioTags.remove(0);
			if(tag.getPts() < passedAudioPts) {
				continue;
			}
			passedAudioPts = tag.getPts();
			if(passedVideoPts < passedAudioPts - 1000) {
				passedVideoPts = passedAudioPts - 1000;
			}
			result.add(tag);
		}
		return result;
	}
	/**
	 * 映像のソート済みデータを取得する
	 */
	@Override
	public List<FlvTag> getVideoCompleteTag() {
		Collections.sort(videoTags, comparator);
		List<FlvTag> result = new ArrayList<FlvTag>();
		while(videoTags.size() > 1) {
			FlvTag tag = videoTags.remove(0);
			if(tag.getPts() < passedVideoPts) {
				continue;
			}
			passedVideoPts = tag.getPts();
			if(passedAudioPts < passedVideoPts - 1000) {
				passedAudioPts = passedVideoPts - 1000;
			}
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
