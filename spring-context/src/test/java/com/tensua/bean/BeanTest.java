package com.tensua.bean;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author zhooke
 * @since 2022/11/24 16:32
 **/
public class BeanTest {
	private static final String PATH = "/org/springframework/context/support/";

	private static final String FQ_SIMPLE_CONTEXT = PATH + "simpleContext.xml";


	@Test
	public void testSingleConfigLocation() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(FQ_SIMPLE_CONTEXT);
		assertThat(ctx.containsBean("someMessageSource")).isTrue();
		Object someMessageSource = ctx.getBean("someMessageSource");
		ctx.close();
	}
}
