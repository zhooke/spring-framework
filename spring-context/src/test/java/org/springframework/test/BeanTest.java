package org.springframework.test;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author zhouhd
 * @since 2021/12/9 10:50
 **/
public class BeanTest {
	@Test
	public void initBean(){
		ApplicationContext factory = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
	}
}
