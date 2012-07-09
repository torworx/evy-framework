package evyframework.di.spi;

import java.lang.annotation.Annotation;

import evyframework.di.Provider;

public class PerthreadScope extends DefaultScope {
	
	@SuppressWarnings("unchecked")
	public static final PerthreadScope INSTANCE = new PerthreadScope();

	public PerthreadScope(Class<? extends Annotation>... customEventTypes) {
		super(customEventTypes);
	}

	@Override
	public <T> Provider<T> scope(Provider<T> unscoped) {
		return new PerthreadScopProvider<T>(this, unscoped);
	}

}
