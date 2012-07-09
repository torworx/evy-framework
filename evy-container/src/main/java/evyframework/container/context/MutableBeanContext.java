package evyframework.container.context;

import evyframework.container.factory.config.FactoryDefinition;
import evyframework.container.factory.registry.FactoryDefinitionRegistry;

public interface MutableBeanContext extends ListableBeanContext {

	FactoryDefinitionRegistry getRegistry();
	
	boolean containsFactoryDefinition(String factoryName);
	
	int getFactoryDefinitionCount();
	
	String[] getFactoryDefinitionNames();
	
	FactoryDefinition getFactoryDefinition(String factoryName);
}
