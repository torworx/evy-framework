package evyframework.common.io;

public class FileAsDefaultResourceLoader extends DefaultResourceLoader {

	@Override
	protected Resource getResourceByPath(String path) {
		return new FileSystemResource(path);
	}

}
