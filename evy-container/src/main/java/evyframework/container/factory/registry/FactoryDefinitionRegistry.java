package evyframework.container.factory.registry;

import evyframework.container.factory.config.FactoryDefinition;

public interface FactoryDefinitionRegistry {
	
	void registerFactoryDefinitions(FactoryDefinitionRegistry registry);
	
	void registerFactoryDefinition(String factoryName, FactoryDefinition factoryDefinition);
	
	void removeFactoryDefinition(String factoryName);
	
	void clearFactoryDefinitions();
	
	FactoryDefinition getFactoryDefinition(String factoryName);
	
	boolean containsFactoryDefinition(String factoryName);
	
	String[] getFactoryDefinitionNames();
	
	int getFactoryDefinitionCount();

}
