package com.ttProject.ozouni.frame;

import com.ttProject.container.IContainer;
import com.ttProject.container.mkv.MkvBlockTag;
import com.ttProject.container.mkv.MkvTagReader;
import com.ttProject.frame.IFrame;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.ozouni.frame.worker.IFrameListener;

/**
 * mkvのデータを読み込みます。
 * @author taktod
 */
public class MkvReader implements IFrameReader {
	/** フレームを見つけたときの処理 */
	private IFrameListener listener = null;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFrameListener(IFrameListener listener) {
		this.listener = listener;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(IReadChannel source) throws Exception {
		MkvTagReader reader = new MkvTagReader();
		IContainer container = null;
		while((container = reader.read(source)) != null) {
			if(container instanceof MkvBlockTag) {
				MkvBlockTag blockTag = (MkvBlockTag)container;
				IFrame frame = blockTag.getFrame();
				listener.receiveFrame(frame);
			}
		}
	}
}
