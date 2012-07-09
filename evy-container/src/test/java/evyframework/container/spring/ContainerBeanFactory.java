package evyframework.container.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import evyframework.container.MutableContainer;
import evyframework.container.factory.impl.FactoryUtil;
import evyframework.container.factory.impl.GlobalNewInstanceFactory;
import evyframework.container.factory.impl.GlobalSingletonFactory;

public class ContainerBeanFactory implements BeanFactory {
	
	private final MutableContainer container;

	public ContainerBeanFactory(MutableContainer container) {
		this.container = container;
	}

	@Override
	public Object getBean(String name) throws BeansException {
		return container.getInstance(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return container.getInstance(name, requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return container.getInstance(requiredType);
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		return container.getInstance(name, args);
	}

	@Override
	public boolean containsBean(String name) {
		return container.getFactory(name) != null;
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return FactoryUtil.getRealFactory(container, name) instanceof GlobalSingletonFactory;
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		return FactoryUtil.getRealFactory(container, name) instanceof GlobalNewInstanceFactory;
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		return targetType.isAssignableFrom(container.getFactory(name).getReturnType());
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return container.getFactory(name).getReturnType();
	}

	@Override
	public String[] getAliases(String name) {
		return null;
	}

}
