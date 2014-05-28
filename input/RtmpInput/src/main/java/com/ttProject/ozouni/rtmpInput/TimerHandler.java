package com.ttProject.ozouni.rtmpInput;

/**
 * timer動作
 * たぶんozouniBaseにいくことになると思う。
 * @author taktod
 */
public class TimerHandler implements Runnable {
	@Override
	public void run() {
		// timerの動作としては・・・
		// frameの補完 こっちはreceiveWriterに必要だったらやらせるとかでいいと思われる
		// こっちについては、FlvTagOrderModelの拡張でなんとかすればいいかな？
		// flvTagOrderModelにexecutorをつけるところをつくっておいて・・・とか
		
		// 動作状態の報告 restAPIで報告するとかredisに報告しておくとか
		// ここについては、なにかしらbeanをつくっておきたいところ。
		/*
		 * ・動作しているサーバーを報告する
		 * ・動作しているポートも報告する
		 * @see com.ttProject.ozouni.base.ReportData
		 */
		// この２点を実施する必要あり。
		// というわけでどちらもtimerHandlerから呼び出すではなく、beanをつくっておいてそこにexecutorServiceを設定する形でつくっておけばよさそう。
		/*
  <!-- executorServiceを作ることは可能っぽい -->
  <bean id="scheduledExecutorService" class="java.util.concurrent.Executors" factory-method="newSingleThreadScheduledExecutor"/>
		 */
	}
}
