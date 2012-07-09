package evyframework.oxm.xstream;

import com.thoughtworks.xstream.core.util.DependencyInjectionFactory;

public class ConverterResolver {

	@SuppressWarnings("unchecked")
	public static <T> T resolve(Object target, Class<T> clsOfT, Object[] dependencies) {
		if ((target == null) || (clsOfT == null)) {
			return null;
		}
		if (clsOfT.isAssignableFrom(target.getClass())) {
			return (T) target;
		}
		Class<?> targetClass = target instanceof Class ? (Class<?>) target : null;
		if ((targetClass == null) && (target instanceof String)) {
			try {
				targetClass = Class.forName((String) target);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if ((targetClass != null) && (clsOfT.isAssignableFrom(targetClass))) {
			try {
				return (T) DependencyInjectionFactory.newInstance(targetClass, dependencies);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
