package com.ttProject.ozouni.base.analyzer;

/**
 * プロセスが動作しているサーバーのサーバー名を応答するAnalyzer
 * とりあえず、ipアドレスから設定するようにしておきますが
 * クラスをつくって、beanを登録すれば、別の方法でも構築できるようにしておきます。
 * hostnameからつくるとか・・・
 * @author taktod
 */
public interface IServerNameAnalyzer {
	/** このプロセスにアクセスする場合のサーバー名を指定します。 */
	public String getServerName();
}
