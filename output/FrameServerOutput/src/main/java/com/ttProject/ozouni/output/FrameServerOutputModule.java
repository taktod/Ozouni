package com.ttProject.ozouni.output;

import com.ttProject.frame.IFrame;
import com.ttProject.ozouni.base.IOutputModule;

/**
 * frameServerとして、ozouniシステム間でデータを共有するモジュール
 * @author taktod
 */
public class FrameServerOutputModule implements IOutputModule {
	/**
	 * frameを送信します。
	 */
	@Override
	public void pushFrame(IFrame frame, int id) throws Exception {
		
	}
}
