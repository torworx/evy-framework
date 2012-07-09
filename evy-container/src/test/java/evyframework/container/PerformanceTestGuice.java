package evyframework.container;

import com.google.inject.*;

import evyframework.container.Container;
import evyframework.container.DefaultContainer;
import evyframework.container.TestProduct;
import evyframework.container.factory.GlobalFactory;
import evyframework.container.java.JavaFactory;
import evyframework.container.java.JavaFactoryBuilder;
import evyframework.container.script.ScriptFactoryBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**

 */
public class PerformanceTestGuice {

	public static int iterationsPerTest = 10 * 1000 * 1000;
	public static int iterationsOfAllTests = 10;

	public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException,
			InstantiationException, IllegalAccessException {

		/* NEW INSTANCE TEST */
		for (int i = 0; i < iterationsOfAllTests; i++) {
			long javaNewTime = javaNewTest();
			long javaReflectionTime = javaReflectionTest();
			long evyContainerScriptTime = evyContainerScriptTest();
			long evyContainerScript2Time = evyContainerScriptTest2();
			long evyContainerJavaTime = evyContainerJavaTest();
			long evyContainerJava2Time = evyContainerJava2Test();
			long guiceTime = guiceTest();
			long guice2Time = guiceTest2();
			long guiceProviderTime = guiceProviderTest();
			long guiceProvider2Time = guiceProvider2Test();

			long javaReflectionTimeRatio = (javaReflectionTime * 100) / javaNewTime;
			long evyContainerJavaTimeRatio = (evyContainerJavaTime * 100) / javaNewTime;
			long evyContainerJava2TimeRatio = (evyContainerJava2Time * 100) / javaNewTime;
			long evyContainerScript2TimeRatio = (evyContainerScript2Time * 100) / javaNewTime;
			long evyContainerScriptTimeRatio = (evyContainerScriptTime * 100) / javaNewTime;
			long guiceTimeRatio = (guiceTime * 100) / javaNewTime;
			long guiceTime2Ratio = (guice2Time * 100) / javaNewTime;
			long guiceProviderTimeRatio = (guiceProviderTime * 100) / javaNewTime;
			long guiceProvider2TimeRatio = (guiceProvider2Time * 100) / javaNewTime;

			System.out.println("-- NEW BASED INSTANTIATION --------------");
			System.out.println("Java new           :  100%,   " + javaNewTime);
			System.out.println("EvyContainer Java     :  " + evyContainerJavaTimeRatio + "%,  " + evyContainerJavaTime);
			System.out.println("EvyContainer Java 2   :  " + evyContainerJava2TimeRatio + "%,  " + evyContainerJava2Time);
			System.out.println("Guice Provider     : " + guiceProviderTimeRatio + "%, " + guiceProviderTime);
			System.out.println("Guice Provider 2   : " + guiceProvider2TimeRatio + "%, " + guiceProvider2Time);
			System.out.println("-- REFLECTION BASED INSTANTIATION ------");
			System.out.println("Java Reflection    : " + javaReflectionTimeRatio + "%,  " + javaReflectionTime);
			System.out.println("EvyContainer Script   : " + evyContainerScriptTimeRatio + "%, " + evyContainerScriptTime);
			System.out.println("EvyContainer Script 2 : " + evyContainerScript2TimeRatio + "%, " + evyContainerScript2Time);
			System.out.println("Guice Reflection   : " + guiceTimeRatio + "%, " + guiceTime);
			System.out.println("Guice Reflection 2 : " + guiceTime2Ratio + "%, " + guice2Time);
			System.out.println("========================================");
		}
	}

	protected static long javaNewTest() {
		long newStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			new TestProduct();
		}
		long newEnd = System.currentTimeMillis();
		return newEnd - newStart;
	}

	protected static long javaReflectionTest() throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, InstantiationException {
		Constructor<TestProduct> constructor = TestProduct.class.getConstructor();
		long newStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			constructor.newInstance();
		}
		long newEnd = System.currentTimeMillis();
		return newEnd - newStart;
	}

	protected static long evyContainerScriptTest() {
		DefaultContainer container = new DefaultContainer();
		ScriptFactoryBuilder builder = new ScriptFactoryBuilder(container);
		builder.addFactory("script = * evyframework.container.TestProduct();");

		long evyContainerScriptStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			container.getInstance("script");
		}
		long evyContainerScriptEnd = System.currentTimeMillis();
		return evyContainerScriptEnd - evyContainerScriptStart;
	}

	protected static long evyContainerScriptTest2() {
		DefaultContainer container = new DefaultContainer();
		ScriptFactoryBuilder builder = new ScriptFactoryBuilder(container);
		builder.addFactory("script = * evyframework.container.TestProduct();");

		GlobalFactory<TestProduct> factory = container.getFactory("script");

		long evyContainerScriptStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			factory.instance();
		}
		long evyContainerScriptEnd = System.currentTimeMillis();
		return evyContainerScriptEnd - evyContainerScriptStart;
	}

	protected static long evyContainerJavaTest() {
		Container container = new DefaultContainer();
		JavaFactoryBuilder javaBuilder = new JavaFactoryBuilder(container);

		javaBuilder.addFactory("java", TestProduct.class, new JavaFactory<TestProduct>() {
			public TestProduct instance(Object... parameters) {
				return new TestProduct();
			}
		});

		long evyContainerJavaStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			container.getInstance("java");
		}
		long evyContainerJavaEnd = System.currentTimeMillis();
		return evyContainerJavaEnd - evyContainerJavaStart;
	}

	protected static long evyContainerJava2Test() {
		Container container = new DefaultContainer();
		JavaFactoryBuilder javaBuilder = new JavaFactoryBuilder(container);

		javaBuilder.addFactory("java", TestProduct.class, new JavaFactory<TestProduct>() {
			public TestProduct instance(Object... parameters) {
				return new TestProduct();
			}
		});

		GlobalFactory<TestProduct> javaFactory = container.getFactory("java");

		long evyContainerJavaStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			javaFactory.instance();
		}
		long evyContainerJavaEnd = System.currentTimeMillis();
		return evyContainerJavaEnd - evyContainerJavaStart;
	}

	protected static long guiceTest() {
		/* GUICE TEST */
		Injector injector = Guice.createInjector(new Module() {
			public void configure(Binder binder) {
				binder.bind(TestProduct.class);
			}
		});

		long guiceStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			injector.getInstance(TestProduct.class);
		}
		long guiceEnd = System.currentTimeMillis();

		return guiceEnd - guiceStart;
	}

	protected static long guiceTest2() {
		/* GUICE TEST */
		Injector injector = Guice.createInjector(new Module() {
			public void configure(Binder binder) {
				binder.bind(TestProduct.class);
			}
		});

		Provider<TestProduct> provider = injector.getProvider(TestProduct.class);

		long guiceStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			provider.get();
		}
		long guiceEnd = System.currentTimeMillis();

		return guiceEnd - guiceStart;
	}

	protected static long guiceProviderTest() {

		final Provider<TestProduct> provider = new Provider<TestProduct>() {
			public TestProduct get() {
				return new TestProduct();
			}
		};

		/* GUICE TEST */
		Injector injector = Guice.createInjector(new Module() {
			public void configure(Binder binder) {
				binder.bind(TestProduct.class).toProvider(provider);
			}
		});

		long guiceStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			injector.getInstance(TestProduct.class);
		}
		long guiceEnd = System.currentTimeMillis();

		return guiceEnd - guiceStart;
	}

	protected static long guiceProvider2Test() {
		Injector injector = Guice.createInjector(new Module() {
			public void configure(Binder binder) {
				binder.bind(TestProduct.class).toProvider(new Provider<TestProduct>() {
					public TestProduct get() {
						return new TestProduct();
					}
				});
			}
		});

		Provider<TestProduct> provider = injector.getProvider(TestProduct.class);

		long guiceStart = System.currentTimeMillis();
		for (int i = 0; i < iterationsPerTest; i++) {
			provider.get();
		}
		long guiceEnd = System.currentTimeMillis();

		return guiceEnd - guiceStart;
	}
}