package evyframework.container.benchmark.models;

public class Soo {
	private Bar bar;

	public Bar getBar() {
		return bar;
	}

	/**
	 * @@InjectAttribute();
	 * @param bar
	 */
	public void setBar(Bar bar) {
		this.bar = bar;
	}

	public void noop() {
		// noop;
	}

}
