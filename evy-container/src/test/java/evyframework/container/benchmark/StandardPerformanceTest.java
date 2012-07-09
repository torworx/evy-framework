package evyframework.container.benchmark;

import static org.junit.Assert.assertNotNull;

import evyframework.container.benchmark.models.Foo;
import evyframework.container.benchmark.models.Soo;

public abstract class StandardPerformanceTest extends AbstractPerformanceTest {

	public void testBenchCreateComponentInstance() throws Exception {
		new Benchmark(getContainerName() + ": Create bean without injection", getLoop()) {
			public void run() throws Exception {
				getInstance("bar");
			}
		}.start(true);

		Soo soo = (Soo) getInstance("soo");
		assertNotNull(soo.getBar());
	}

	public void testBenchConstructorInjection() throws Exception {
		new Benchmark(getContainerName() + ": Create bean with Constructor Dependency Injection", getLoop()) {
			public void run() throws Exception {
				getInstance("foo");
			}
		}.start(true);
		Foo foo = (Foo) getInstance("foo");
		assertNotNull(foo.getBar());
	}

	public void testBenchSetterInjectio() throws Exception {
		new Benchmark(getContainerName() + ": Create bean with Setter Dependency Injection", getLoop()) {
			public void run() throws Exception {
				getInstance("soo");
			}
		}.start(true);
		Soo soo = (Soo) getInstance("soo");
		assertNotNull(soo.getBar());
	}

	public void testBenchAutowiredSetterInjection() throws Exception {
		new Benchmark(getContainerName() + ": Create bean with bytype autowiring and Setter Dependency Injection", getLoop()) {
			public void run() throws Exception {
				getInstance("auto_soo");
			}
		}.start(true);
		Soo soo = (Soo) getInstance("auto_soo");
		assertNotNull(soo.getBar());
	}

	public void testBenchSingleton() throws Exception {
		new Benchmark(getContainerName() + ": Create singleton bean with Setter Dependency Injection", getLoop() * 10) {
			public void run() throws Exception {
				getInstance("ssoo");
			}
		}.start(true);
		Soo soo = (Soo) getInstance("ssoo");
		assertNotNull(soo.getBar());
	}

	public void testBenchEmptyInterceptor() throws Exception {
		Benchmark bench = new Benchmark(getContainerName() + ": Bean method invocation with empty interceptor applied", getLoop() * 100) {
			Soo soo = (Soo) getInstance("sooProxy");

			public void run() throws Exception {
				soo.noop();
			}
		};
		bench.start(true);
	}

	public void testBenchCreateAsectizedBean() throws Exception {
		new Benchmark(getContainerName() + ": Create aspectized bean", getLoop() / 10) {
			public void run() throws Exception {
				getInstance("sooProxy");
			}
		}.start(true);
	}
}
