package com.ttProject.ozouni.input.rtmp;

import com.flazr.rtmp.RtmpWriter;
import com.ttProject.ozouni.base.IWorkModule;

/**
 * frameデータを受け取るinterface
 * @author taktod
 *
 */
public interface IReceiveWriter extends RtmpWriter {
	/**
	 * 出力モジュールを設定する(beanでは設定しません。)
	 * @param outputModule
	 */
	public void setWorkModule(IWorkModule workModule);
}
