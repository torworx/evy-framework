package evyframework.container.benchmark;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

import evyframework.container.DefaultContainer;
import evyframework.container.MutableContainer;
import evyframework.container.script.ScriptFactoryBuilder;
import evyframework.container.spring.ContainerBeanFactory;

public class PerformanceTest_Evy extends StandardPerformanceTest {
	
	private MutableContainer container;
	private BeanFactory beanFactory;

	@Before
	public void setUp() throws Exception {
		container = new DefaultContainer();
		ScriptFactoryBuilder builder = new ScriptFactoryBuilder(container);
		builder.addFactories("classpath:evyframework/container/benchmark/applicationContext.ecs");
		beanFactory = new ContainerBeanFactory(container);
		Thread.sleep(100);
		System.gc();
		Thread.sleep(100);
		System.gc();
		Thread.sleep(100);
	}
	
	@After
	public void tearDown() throws Exception {
		
	}

	@Override
	protected String getContainerName() {
		return "Evy Container";
	}

	@Override
	protected Object getInstance(String name) throws Exception {
		Object result = container.getInstance(name);
		if (result instanceof BeanFactoryAware) {
			((BeanFactoryAware) result).setBeanFactory(beanFactory);
		}
		if (result instanceof FactoryBean<?>) {
			return ((FactoryBean<?>) result).getObject();
		}
		return result;
	}

	@Test
	@Override
	public void testBenchCreateComponentInstance() throws Exception {
		super.testBenchCreateComponentInstance();
	}

	@Test
	@Override
	public void testBenchConstructorInjection() throws Exception {
		super.testBenchConstructorInjection();
	}

	@Test
	@Override
	public void testBenchSetterInjectio() throws Exception {
		super.testBenchSetterInjectio();
	}

	@Override
	public void testBenchAutowiredSetterInjection() throws Exception {
		super.testBenchAutowiredSetterInjection();
	}

	@Test
	@Override
	public void testBenchSingleton() throws Exception {
		super.testBenchSingleton();
	}

	@Test
	@Override
	public void testBenchEmptyInterceptor() throws Exception {
		super.testBenchEmptyInterceptor();
	}

	@Test
	@Override
	public void testBenchCreateAsectizedBean() throws Exception {
		super.testBenchCreateAsectizedBean();
	}
	
	
}
