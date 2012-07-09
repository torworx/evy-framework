package evyframework.container.benchmark;

public abstract class AbstractPerformanceTest {
	
	protected long getLoop() {
		return 200000;
	}
	
	protected abstract String getContainerName();
	
	protected abstract Object getInstance(String name) throws Exception;
}
