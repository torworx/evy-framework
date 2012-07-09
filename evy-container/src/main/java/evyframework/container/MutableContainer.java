package evyframework.container;

import java.util.List;

import evyframework.container.factory.config.BeanContextPostProcessor;

public interface MutableContainer extends Container {
	
	void addBeanContextPostProcessor(BeanContextPostProcessor beanContextPostProcessor);
	
	List<BeanContextPostProcessor> getBeanContextPostProcessors();
}
