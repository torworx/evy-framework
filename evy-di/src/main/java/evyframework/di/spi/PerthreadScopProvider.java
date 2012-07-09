package evyframework.di.spi;

import evyframework.di.ConfigurationException;
import evyframework.di.Provider;

public class PerthreadScopProvider <T> implements Provider<T> {

	private Provider<T> delegate;
	private PerthreadScope scope;

	private final ThreadLocal<T> instanceHolder = new ThreadLocal<T>();

	public PerthreadScopProvider(PerthreadScope scope, Provider<T> delegate) {
		this.scope = scope;
		this.delegate = delegate;

		scope.addScopeEventListener(this);
	}

	public T get() {
		T instance = instanceHolder.get();
		if (instance == null) {
			synchronized (this) {
				if (instance == null) {
					instance = delegate.get();

					if (instance == null) {
						throw new ConfigurationException("Underlying provider (%s) returned NULL instance", delegate
								.getClass().getName());
					}
					instanceHolder.set(instance);
					scope.addScopeEventListener(instance);
				}
			}
		}

		return instance;
	}

	@AfterScopeEnd
	public void afterScopeEnd() throws Exception {
		Object localInstance = instanceHolder.get();

		if (localInstance != null) {
			instanceHolder.set(null);
			scope.removeScopeEventListener(localInstance);
		}
	}
}