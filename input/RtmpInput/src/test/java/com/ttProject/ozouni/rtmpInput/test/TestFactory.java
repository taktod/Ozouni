package com.ttProject.ozouni.rtmpInput.test;

public class TestFactory {
	/**
	 * コンストラクタは禁止
	 */
	private TestFactory() {
	}
	/**
	 * 引数なし、通常のfactory
	 * @return
	 */
	public static TestFactory create1() {
		System.out.println("create1");
		return new TestFactory();
	}
	/**
	 * 引数付、factoryするがbeanはnullになる。
	 * @param test
	 * @return
	 */
	public static TestFactory create2(String test) {
		System.out.println(test);
		return null;
	}
	/**
	 * 引数付、普通のfactoryメソッド
	 * @param test
	 * @return
	 */
	public static TestFactory create3(String test) {
		System.out.println(test);
		return new TestFactory();
	}
	/**
	 * factoryメソッドだけど、違うオブジェクトを応答してみます。
	 * この場合beanがString型になります。
	 * @return
	 */
	public static String create4() {
		System.out.println("create4");
		return "abc";
	}
	/**
	 * factoryメソッドだけど、応答がない。
	 * この場合はfactoryメソッドとして認識してくれません。
	 * 動作しません。
	 */
	public static void create5() {
		System.out.println("create5");
	}
}
