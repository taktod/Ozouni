package com.ttProject.ozouni.frame;

import com.ttProject.container.IContainer;
import com.ttProject.container.riff.RiffFrameUnit;
import com.ttProject.container.riff.RiffUnitReader;
import com.ttProject.frame.IFrame;
import com.ttProject.nio.channels.IReadChannel;
import com.ttProject.ozouni.frame.worker.IFrameListener;

/**
 * aviのデータを読み込ませます。
 * @author taktod
 */
public class AviReader implements IFrameReader {
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
		RiffUnitReader reader = new RiffUnitReader();
		IContainer container = null;
		while((container = reader.read(source)) != null) {
			if(container instanceof RiffFrameUnit) {
				RiffFrameUnit frameUnit = (RiffFrameUnit)container;
				IFrame frame = frameUnit.getFrame();
				listener.receiveFrame(frame);
			}
		}
	}
}
