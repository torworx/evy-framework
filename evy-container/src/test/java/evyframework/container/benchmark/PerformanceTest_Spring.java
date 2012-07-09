package evyframework.container.benchmark;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PerformanceTest_Spring extends StandardPerformanceTest {
	
	private ApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("evyframework/container/benchmark/applicationContext.xml");
		assertNotNull(applicationContext);
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
		return "Spring";
	}

	@Override
	protected Object getInstance(String name) {
		return applicationContext.getBean(name);
	}

	@Test
	@Override
	public void testBenchCreateComponentInstance() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchCreateComponentInstance();
	}

	@Test
	@Override
	public void testBenchConstructorInjection() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchConstructorInjection();
	}

	@Test
	@Override
	public void testBenchSetterInjectio() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchSetterInjectio();
	}

	@Test
	@Override
	public void testBenchAutowiredSetterInjection() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchAutowiredSetterInjection();
	}

	@Test
	@Override
	public void testBenchSingleton() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchSingleton();
	}

	@Test
	@Override
	public void testBenchEmptyInterceptor() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchEmptyInterceptor();
	}

	@Test
	@Override
	public void testBenchCreateAsectizedBean() throws Exception {
		// TODO Auto-generated method stub
		super.testBenchCreateAsectizedBean();
	}

}
