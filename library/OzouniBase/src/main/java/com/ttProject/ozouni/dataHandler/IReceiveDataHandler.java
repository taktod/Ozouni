package com.ttProject.ozouni.dataHandler;

/**
 * データを受け取るhandler
 * @author taktod
 */
public interface IReceiveDataHandler {
	/**
	 * データ受け取りリスナーを設定する
	 * @param listener
	 */
	public void registerListener(IDataListener listener);
	/**
	 * データ受け取りリスナーを外す
	 * @param listener
	 */
	public boolean unregisterListener(IDataListener listener);
	// 接続先はreceiveHandlerのkeyをみて勝手に指定するほうがいいのだろうか・・・
	// こういうのって、自動判別にしておいて、うまく接続できないと腹立つんだよね。
	/**
	 * keyを設定しておく
	 * @param key
	 * @throws Exception
	 */
	public void setKey(String key) throws Exception;
	/**
	 * 動作を開始する
	 * @throws Exception
	 */
	public boolean connect() throws Exception;
	/**
	 * 閉じる
	 * @throws Exception
	 */
	public void close() throws Exception;
	public void waitForClose() throws Exception;
}
