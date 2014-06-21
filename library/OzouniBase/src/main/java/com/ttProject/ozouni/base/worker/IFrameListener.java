package com.ttProject.ozouni.base.worker;

import com.ttProject.frame.IFrame;

/**
 * frameを受け取るlistener
 * @author taktod
 */
public interface IFrameListener {
	/**
	 * frameを受け取った時の動作
	 * @param frame
	 */
	public void receiveFrame(IFrame frame);
}
