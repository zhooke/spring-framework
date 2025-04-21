/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;

/**
 * Utility class for handling registration of auto-proxy creators used internally
 * by the '{@code aop}' namespace tags.
 *
 * <p>Only a single auto-proxy creator should be registered and multiple configuration
 * elements may wish to register different concrete implementations. As such this class
 * delegates to {@link AopConfigUtils} which provides a simple escalation protocol.
 * Callers may request a particular auto-proxy creator and know that creator,
 * <i>or a more capable variant thereof</i>, will be registered as a post-processor.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @see AopConfigUtils
 */
public abstract class AopNamespaceUtils {

	/**
	 * The {@code proxy-target-class} attribute as found on AOP-related XML tags.
	 */
	public static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	/**
	 * The {@code expose-proxy} attribute as found on AOP-related XML tags.
	 */
	private static final String EXPOSE_PROXY_ATTRIBUTE = "expose-proxy";


	public static void registerAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {

		BeanDefinition beanDefinition = AopConfigUtils.registerAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	public static void registerAspectJAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {

		BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	/**
	 * 注册AspectJAnnotationAutoProxyCreator
	 *
	 * @param parserContext
	 * @param sourceElement
	 */
	public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {
		/**
		 * 1.注册或升级AutoproxyCreator定义的beanName为org.springframework.aop.config.internalAutoProxyCreator
		 *
		 * 对于AOP的实现，基本上都是靠AnnotationAwareAspectJAutoProxyCreator去完成的，他可以根据
		 * @point 注解定义的切点来自动代理匹配的bean。但是为了配置简便，spring使用了自定义配置来帮助我们
		 * 自动注册AnnotationAwareAspectJAutoProxyCreator，其注册过程就是registerAspectJAnnotationAutoProxyCreatorIfNecessary()
		 */
		BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		//2.对proxy-target-class及expose-proxy属性的处理
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		//3.注册组件并通知，便于监控器做进一步的处理，其中beanDefinition的className为AnnotationAwareAspectJAutoProxyCreator（传入的cls）
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, @Nullable Element sourceElement) {
		if (sourceElement != null) {
			/**
			 * 对于proxy-target-class属性的处理
			 * proxy-target-class：Spring AOP部分使用JDK动态代理或者CGLIB来为目标对象创建代理（
			 * 建议尽量使用JDK的动态代理）。如果被代理的目标对象实现了至少一个接口，则会使用JDK动态
			 * 代理，所有该目标类型实现的接口都将被代理。若该目标对象没有实现任何接口，则创建一个
			 * CGLIB代理（例如希望代理目标对象的所有方法，而不是实现自接口的方法），那也可以。但是
			 * 需要考虑一下两个问题：
			 * 		1.无法通知（advise）Final方法，因为他们不能被覆盖
			 * 		2.你需要将CGLIB二进制发行保放在classpath下面
			 * 与之相比，JDK本身就提供了动态代理，强制使用CGLIB代理需要将<apo:config>的proxy-target-class属性设置为true
			 * 当需要使用CGLIB代理和@AspectJ自动代理支持，可以配置<aop:aspectj-autoproxy proxy-target-class="true"/>
			 *
			 * JDK动态代理：其代理对象必须是某个接口的实现，它是通过在运行期间创建一个接口的实现类来完成对目标对象的代理。
			 * CGLIB代理：实现原理类似于JDK动态代理，只是它在运行期间生成的代理对象是针对目标类扩展的子类。CGLIB是高效的代码生成包，
			 * 底层是依靠ASM（开源的Java字节码编辑类库）操作字节码实现的，性能比JDK强。
			 * expose-proxy：有时候目标对象内部的自我调用将无法实施切面中的增强
			 */
			boolean proxyTargetClass = Boolean.parseBoolean(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
			if (proxyTargetClass) {
				//BeanDefinition中propertyValues属性proxyTargetClass的设置
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			//对于expose-proxy属性的处理
			boolean exposeProxy = Boolean.parseBoolean(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
			if (exposeProxy) {
				//BeanDefinition中propertyValues属性exposeProxy的设置
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

	private static void registerComponentIfNecessary(@Nullable BeanDefinition beanDefinition, ParserContext parserContext) {
		if (beanDefinition != null) {
			parserContext.registerComponent(
					new BeanComponentDefinition(beanDefinition, AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME));
		}
	}

}
