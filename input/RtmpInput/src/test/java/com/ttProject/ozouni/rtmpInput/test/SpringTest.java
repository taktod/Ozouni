package com.ttProject.ozouni.rtmpInput.test;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * springFrameworkの動作テスト
 * @author taktod
 */
public class SpringTest {
	@Test
	public void test() throws Exception {
		ConfigurableApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("test.xml");
			System.out.println(context.getBean("b1"));
			System.out.println(context.getBean("b2"));
//			System.out.println(context.getBean("b3"));
			System.out.println(context.getBean("b4"));
//			System.out.println(context.getBean("b5"));
			System.out.println(context.getBean("b6"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(context != null) {
				context.close();
				context = null;
			}
		}
	}
}
