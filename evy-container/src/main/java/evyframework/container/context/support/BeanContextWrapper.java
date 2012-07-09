package evyframework.container.context.support;

import java.util.Map;

import evyframework.container.Container;
import evyframework.container.context.ListableBeanContext;

public class BeanContextWrapper implements ListableBeanContext {
	
	private final Container container;

	public BeanContextWrapper(Container container) {
		this.container = container;
	}

	public Container getContainer() {
		return container;
	}
	
	protected void validateContainer() {
		if (this.container == null) {
			throw new IllegalStateException("You cannot use this method unless the ScriptFactoryBuilder"
					+ " was instantiated with a Container instance in the constructor");
		}
	}
	
	public <T> T getInstance(String name) {
		return container.getInstance(name);
	}

	public <T> T getInstance(String name, Class<T> requiredType) {
		return container.getInstance(name, requiredType);
	}

	public <T> T getInstance(Class<T> requiredType) {
		return container.getInstance(requiredType);
	}

	public <T> T getInstance(String name, Object... parameters) {
		return container.getInstance(name, parameters);
	}
	
	public String[] getInstanceNamesForType(Class<?> type) {
		return container.getInstanceNamesForType(type);
	}

	public String[] getInstanceNamesForType(Class<?> type, boolean includeNonSingletons) {
		return container.getInstanceNamesForType(type, includeNonSingletons);
	}

	public <T> Map<String, T> getInstancesOfType(Class<T> type) {
		return container.getInstancesOfType(type);
	}

	public <T> Map<String, T> getInstancesOfType(Class<T> type, boolean includeNonSingletons) {
		return container.getInstancesOfType(type, includeNonSingletons);
	}
	
}
