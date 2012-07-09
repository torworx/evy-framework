package evyframework.container.factory.impl.convert;

import java.io.File;

import evyframework.common.io.ResourceLoader;
import evyframework.common.io.support.ResourcePatternUtils;
import evyframework.container.factory.LocalFactory;
import evyframework.container.factory.impl.LocalFactoryBase;

public class FileFactory extends LocalFactoryBase implements LocalFactory {
	
	private final ResourceLoader resourceLoader = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();

	protected LocalFactory sourceFactory = null;

	public FileFactory(LocalFactory sourceFactory) {
		this.sourceFactory = sourceFactory;
	}

	public Class<?> getReturnType() {
		return File.class;
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		try {
			return resourceLoader.getResource(this.sourceFactory.instance(parameters, localProducts).toString()).getFile();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}