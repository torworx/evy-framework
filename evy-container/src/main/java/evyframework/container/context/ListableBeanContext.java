package evyframework.container.context;

import java.util.Map;


public interface ListableBeanContext extends BeanContext {

	String[] getInstanceNamesForType(Class<?> type);

	String[] getInstanceNamesForType(Class<?> type, boolean includeNonSingletons);

	<T> Map<String, T> getInstancesOfType(Class<T> type);

	<T> Map<String, T> getInstancesOfType(Class<T> type, boolean includeNonSingletons);

}
