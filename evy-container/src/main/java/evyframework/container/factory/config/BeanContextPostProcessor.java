package evyframework.container.factory.config;

import evyframework.container.context.MutableBeanContext;

public interface BeanContextPostProcessor {
	
	void postProcessBeanContext(MutableBeanContext beanContext);

}
