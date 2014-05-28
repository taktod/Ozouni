package com.ttProject.ozouni.rtmpfeeder.model;

import java.util.List;

import com.ttProject.container.flv.FlvTag;

/**
 * flvTagの順番調整用のbean
 * rtmpで転送されてくるflvTagは、順番が前後したりするので、それを調整します。
 * @author taktod
 */
public interface IFlvTagOrderModel {
	/**
	 * tagを追加する
	 * @param tag
	 */
	public void addTag(FlvTag tag);
	/**
	 * 音声tagで順番が確定したものを応答
	 * @return
	 */
	public List<FlvTag> getAudioCompleteTag();
	/**
	 * 映像tagで順番が確定してものを応答
	 * @return
	 */
	public List<FlvTag> getVideoCompleteTag();
}
