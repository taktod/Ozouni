package com.ttProject.ozouni.reportHandler;

import com.ttProject.ozouni.base.ReportData;

/**
 * 動作の状態をレポートするhandler
 * 共有しておきたいデータは次のとおり。
 * ・動作しているサーバーのホストネーム
 * ・PID
 * ・動作の一意番号
 * ・dataHandlerとreportHandlerの状態
 * @author taktod
 */
public interface IReportHandler {
	/**
	 * データのレポートを実施する
	 * @param uid このプロセスの一意のID
	 * @param data 共有するデータ
	 */
	public void reportData(
			String uid,
			ReportData data);
	/**
	 * データを参照する
	 * @param uid 参照するプロセスのuid
	 * @return ReportData 共有しているデータ
	 */
	public ReportData getData(int uid);
}
