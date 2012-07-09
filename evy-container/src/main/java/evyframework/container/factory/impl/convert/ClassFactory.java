package evyframework.container.factory.impl.convert;

import evyframework.common.ClassUtils;
import evyframework.container.factory.LocalFactory;
import evyframework.container.factory.impl.LocalFactoryBase;

public class ClassFactory extends LocalFactoryBase implements LocalFactory {

	protected LocalFactory sourceFactory = null;

	public ClassFactory(LocalFactory sourceFactory) {
		this.sourceFactory = sourceFactory;
	}

	public Class<?> getReturnType() {
		return Class.class;
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		try {
			return ClassUtils.forName(this.sourceFactory.instance(parameters, localProducts).toString(),
					ClassUtils.getDefaultClassLoader());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}