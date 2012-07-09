package evyframework.container.context.support;

import evyframework.container.Container;
import evyframework.container.context.MutableBeanContext;
import evyframework.container.factory.config.FactoryDefinition;
import evyframework.container.factory.registry.DefaultFactoryDefinitionRegistry;
import evyframework.container.factory.registry.FactoryDefinitionRegistry;

public class AbstractMutableBeanContext extends BeanContextWrapper implements MutableBeanContext {

	private final DefaultFactoryDefinitionRegistry publishedRegistry = new DefaultFactoryDefinitionRegistry();
	private final DefaultFactoryDefinitionRegistry registry = new DefaultFactoryDefinitionRegistry();
	
	public AbstractMutableBeanContext(Container container) {
		super(container);
	}
	
	protected DefaultFactoryDefinitionRegistry getPublishedRegistry() {
		return publishedRegistry;
	}

	@Override
	public FactoryDefinitionRegistry getRegistry() {
		return registry;
	}

	@Override
	public boolean containsFactoryDefinition(String factoryName) {
		return publishedRegistry.containsFactoryDefinition(factoryName);
	}

	@Override
	public int getFactoryDefinitionCount() {
		return publishedRegistry.getFactoryDefinitionCount();
	}

	@Override
	public String[] getFactoryDefinitionNames() {
		return publishedRegistry.getFactoryDefinitionNames();
	}

	@Override
	public FactoryDefinition getFactoryDefinition(String factoryName) {
		return publishedRegistry.getFactoryDefinition(factoryName);
	}

	
}
