package evyframework.container.benchmark.models;

public class FooImpl implements Foo {

	private Bar bar;

	public FooImpl() {

	}

	/**
	 * @@InjectAttribute();
	 * @param bar
	 */
	public FooImpl(Bar bar) {
		this.bar = bar;
	}

	public String hello() {
		return "Hello";
	}

	public Bar getBar() {
		return bar;
	}

	public void noop() {
		// noop
	}
}
