package evyframework.container.context.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import evyframework.container.Container;
import evyframework.container.ContainerException;
import evyframework.container.MutableContainer;
import evyframework.container.context.MutableBeanContext;
import evyframework.container.factory.FactoryDefinitionStoreException;
import evyframework.container.factory.GlobalFactory;
import evyframework.container.factory.LocalFactory;
import evyframework.container.factory.config.BeanContextPostProcessor;
import evyframework.container.factory.config.FactoryDefinition;
import evyframework.container.factory.impl.CollectionFactory;
import evyframework.container.factory.impl.FactoryBuilder;
import evyframework.container.factory.impl.FactoryFactory;
import evyframework.container.factory.impl.FactoryUtil;
import evyframework.container.factory.impl.GlobalFactoryBase;
import evyframework.container.factory.impl.GlobalFlyweightFactory;
import evyframework.container.factory.impl.GlobalNewInstanceFactory;
import evyframework.container.factory.impl.GlobalSingletonFactory;
import evyframework.container.factory.impl.GlobalThreadSingletonFactory;
import evyframework.container.factory.impl.InputAdaptingFactory;
import evyframework.container.factory.impl.InputConsumerFactory;
import evyframework.container.factory.impl.LocalFlyweightFactory;
import evyframework.container.factory.impl.LocalProductConsumerFactory;
import evyframework.container.factory.impl.LocalProductProducerFactory;
import evyframework.container.factory.impl.LocalSingletonFactory;
import evyframework.container.factory.impl.LocalThreadSingletonFactory;
import evyframework.container.factory.impl.LocalizedResourceFactory;
import evyframework.container.factory.impl.MapFactory;
import evyframework.container.factory.impl.ValueFactory;
import evyframework.container.factory.support.ParserException;

public class DefaultBeanContext extends AbstractMutableBeanContext {
	
	protected final FactoryBuilder builder = new FactoryBuilder();
	
	private final Object unresolvedRegistryMonitor = new Object();

	public DefaultBeanContext(MutableContainer container) {
		super(container);
	}

	@Override
	public MutableContainer getContainer() {
		return (MutableContainer) super.getContainer();
	}
	
	public void publish() {
		publish(false);
	}

	public void publish(boolean overrideExists) {
		synchronized (unresolvedRegistryMonitor) {
			
			invokeContainerPostProcessors(this);
	
			String[] factoryNames = getRegistry().getFactoryDefinitionNames();
			for (String factoryName : factoryNames) {
				FactoryDefinition definition = getRegistry().getFactoryDefinition(factoryName);
				FactoryDefinition declaredDefinition = definition;
				// is factory update factory
				if (definition.isFactoryUpdateFactory()) {
					factoryName = definition.getIdentifier();
					declaredDefinition = null;
					if (containsFactoryDefinition(factoryName)) {
						declaredDefinition = getFactoryDefinition(factoryName);
					} else if (getRegistry().containsFactoryDefinition(factoryName)) {
						declaredDefinition = getRegistry().getFactoryDefinition(factoryName);
					}
					if (declaredDefinition == null) {
						throw new FactoryDefinitionStoreException(definition.getResourceDescription(), factoryName,
								"Cannot update factory definition [" + factoryName + "] for factory '" + definition
										+ "': There is no declared factory definition bound.");
					}
					updateFactoryDefinition(declaredDefinition, definition);
					declaredDefinition.init();
				}
				getRegistry().removeFactoryDefinition(definition.getName());
				getPublishedRegistry().registerFactoryDefinition(factoryName, declaredDefinition);
				GlobalFactory<?> factory = buildGlobalFactory(getContainer(), declaredDefinition);
				if (overrideExists || definition.isFactoryUpdateFactory()) {
					getContainer().replaceFactory(factoryName, factory);
				} else {
					getContainer().addFactory(factoryName, factory);
				}
			}
		}
	}

	protected void invokeContainerPostProcessors(MutableBeanContext beanContext) {
		invokeContainerPostProcessors(getContainer().getBeanContextPostProcessors(), beanContext);

		String[] preProcessorNames = getInstanceNamesForType(BeanContextPostProcessor.class);
		if (preProcessorNames != null && preProcessorNames.length > 0) {
			List<BeanContextPostProcessor> nonOrderedPreProcessors = new ArrayList<BeanContextPostProcessor>();
			for (String name : preProcessorNames) {
				nonOrderedPreProcessors.add(getInstance(name, BeanContextPostProcessor.class));
			}
			invokeContainerPostProcessors(nonOrderedPreProcessors, beanContext);
		}
	}

	protected void invokeContainerPostProcessors(Collection<BeanContextPostProcessor> portProcessors,
			MutableBeanContext beanContext) {
		for (BeanContextPostProcessor postProcessor : portProcessors) {
			postProcessor.postProcessBeanContext(beanContext);
		}
	}

	protected void updateFactoryDefinition(FactoryDefinition declaredDefinition, FactoryDefinition newDefinition) {
		// merge phases
		if (newDefinition.getPhaseFactories() != null && newDefinition.getPhaseFactories().size() > 0) {
			if (declaredDefinition.getPhaseFactories() == null) {
				declaredDefinition.setPhaseFactories(new HashMap<String, List<FactoryDefinition>>());
			}
			for (String phase : newDefinition.getPhaseFactories().keySet()) {
				List<FactoryDefinition> originPhaseFactories = declaredDefinition.getPhaseFactories().get(phase);
				List<FactoryDefinition> newPhaseFactories = newDefinition.getPhaseFactories().get(phase);

				if (originPhaseFactories == null) {
					declaredDefinition.getPhaseFactories().put(phase, newPhaseFactories);
				} else {
					originPhaseFactories.addAll(newPhaseFactories);
				}
			}
		}
	}

	protected GlobalFactory<?> buildGlobalFactory(Container container, FactoryDefinition definition) {
		GlobalFactoryBase globalFactory = null;

		LocalFactory instantiationFactory = buildLocalFactoryRecursively(container, definition);
		definition.setLocalProductType(definition.getName(), instantiationFactory.getReturnType());

		if (definition.isNewInstance() || definition.isLocalizedMap()) {
			globalFactory = new GlobalNewInstanceFactory();
		} else if (definition.isSingleton()) {
			globalFactory = new GlobalSingletonFactory();
		} else if (definition.isThreadSingleton()) {
			globalFactory = new GlobalThreadSingletonFactory();
		} else if (definition.isFlyweight()) {
			globalFactory = new GlobalFlyweightFactory();
		}

		int namedLocalProductCount = definition.getNamedLocalProductCount();

		globalFactory.setLocalProductCount(namedLocalProductCount);

		/*
		 * todo optimize this... so far all global factories have at least 1
		 * named local product (the returned product)... but far from all global
		 * factories actually reference it from life cycle phases.
		 */
		if (definition.getNamedLocalProductCount() > 0) {
			instantiationFactory = new LocalProductProducerFactory(instantiationFactory, 0);
		}

		globalFactory.setLocalInstantiationFactory(instantiationFactory);

		if (definition.getPhaseFactories() != null && definition.getPhaseFactories().size() > 0) {
			for (String phase : definition.getPhaseFactories().keySet()) {
				List<FactoryDefinition> phaseFactories = definition.getPhaseFactories().get(phase);
				globalFactory.setPhase(phase, buildLocalFactories(container, phaseFactories));
			}
		}
		return globalFactory;
	}

	protected List<LocalFactory> buildLocalFactories(Container container, List<FactoryDefinition> factoryDefinitions) {
		List<LocalFactory> factories = new ArrayList<LocalFactory>();
		if (factoryDefinitions != null) {
			for (FactoryDefinition definition : factoryDefinitions) {
				factories.add(buildLocalFactoryRecursively(container, definition));
			}
		}
		return factories;
	}

	protected LocalFactory buildLocalFactoryRecursively(Container container, FactoryDefinition definition) {
		LocalFactory factory = null;

		try {
			List<LocalFactory> argumentFactories = buildLocalFactories(container,
					definition.getInstantiationArgFactories());

			// todo get this from castings in factory definition. Casting will
			// force a specific return type.
			Class<?>[] forcedArgumentTypes = getForcedArgumentTypes(argumentFactories, definition);

			if (definition.isConstructorFactory()) { // constructor factory
				factory = builder.createConstructorFactory(definition.getIdentifierOwnerClass(), argumentFactories,
						forcedArgumentTypes);
			} else if (definition.isStaticMethodFactory()) {
				// method invocation factory
				factory = builder.createStaticMethodFactory(definition.getIdentifier(),
						definition.getIdentifierOwnerClass(), argumentFactories, forcedArgumentTypes);
			} else if (definition.isInstanceMethodFactory()) {
				LocalFactory methodInvocationTargetFactory = buildLocalFactoryRecursively(container,
						definition.getIdentifierTargetFactory());
				factory = builder.createInstanceMethodFactory(definition.getIdentifier(),
						methodInvocationTargetFactory, argumentFactories, forcedArgumentTypes);
			} else if (definition.isInstanceFieldFactory()) {
				LocalFactory fieldTargetFactory = buildLocalFactoryRecursively(container,
						definition.getIdentifierTargetFactory());
				factory = builder.createFieldFactory(definition.getIdentifier(), fieldTargetFactory);
			} else if (definition.isStaticFieldFactory()) {
				factory = builder.createFieldFactory(definition.getIdentifier(), definition.getIdentifierOwnerClass());
			} else if (definition.isInstanceFieldAssignmentFactory()) {
				LocalFactory assignmentTargetFactory = buildLocalFactoryRecursively(container,
						definition.getIdentifierTargetFactory());
				factory = builder.createFieldAssignmentFactory(definition.getIdentifier(), assignmentTargetFactory,
						argumentFactories.get(0));
			} else if (definition.isStaticFieldAssignmentFactory()) {
				factory = builder.createFieldAssignmentFactory(definition.getIdentifier(),
						definition.getIdentifierOwnerClass(), argumentFactories.get(0));
			} else if (definition.isFactoryCallFactory()) { // existing factory
															// reference
				if (container.getFactory(definition.getIdentifier()) == null)
					throw new ParserException("ScriptFactoryBuilder", "UNKNOWN_FACTORY",
							"Error in factory definition [" + definition.getRoot().getName() + "]: Unknown Factory: "
									+ definition.getIdentifier());
				factory = new InputAdaptingFactory(container.getFactory(definition.getIdentifier()), argumentFactories);
			} else if (definition.isFactoryFactory()) {
				factory = new FactoryFactory(container, definition.getIdentifier());
			} else if (definition.isCollectionFactory()) {
				factory = new CollectionFactory(argumentFactories);
			} else if (definition.isMapFactory()) {
				List<LocalFactory> keyFactories = buildLocalFactories(container,
						definition.getInstantiationArgKeyFactories());
				factory = new MapFactory(keyFactories, argumentFactories);

				if (definition.isLocalizedMap()) {
					((MapFactory) factory).setFactoryMap(true);
					GlobalFactory<?> localeFactory = container.getFactory("locale");
					if (localeFactory == null) {
						new ParserException(
								"ScriptFactoryBuilder",
								"NO_LOCALE_FACTORY_FOUND",
								"Error in factory definition "
										+ definition.getRoot().getName()
										+ ": No 'locale' factory found. "
										+ "A 'locale' factory must be present in order to use localized resource factories");
					}
					factory = new LocalizedResourceFactory(factory, localeFactory);
				}
			} else if (definition.isValueFactory()) { // value factory
				if (isString(definition.getIdentifier()))
					factory = new ValueFactory(definition.getIdentifier().substring(1,
							definition.getIdentifier().length() - 1));
				else if ("null".equals(definition.getIdentifier())) {
					factory = new ValueFactory(null);
				} else
					factory = new ValueFactory(definition.getIdentifier());
			} else if (definition.isInputParameterFactory()) { // input
																// consuming
																// factory
				factory = new InputConsumerFactory(Integer.parseInt(definition.getIdentifier()));
			} else if (definition.isLocalProductFactory()) {
				factory = new LocalProductConsumerFactory(definition.getLocalProductType(),
						definition.getLocalProductIndex());
			}

			// only local factories with a name (named local factories) can be
			// something else than "new instance"
			// factories.
			if (definition.isNamedLocalFactory()) {
				if (definition.isSingleton()) {
					factory = new LocalSingletonFactory(factory);
				} else if (definition.isThreadSingleton()) {
					factory = new LocalThreadSingletonFactory(factory);
				} else if (definition.isFlyweight()) {
					factory = new LocalFlyweightFactory(factory);
				}

				factory = new LocalProductProducerFactory(factory, definition.getLocalProductIndex());
				definition.setLocalProductType(definition.getName(), factory.getReturnType());
			}

			return factory;
		} catch (ContainerException e) {
			if (e.getCode().indexOf("ScriptFactoryBuilder") == -1) {
				e.addInfo("ScriptFactoryBuilder", "ERROR_CREATING_FACTORY", "Error in factory definition "
						+ definition.getRoot().getName());
			}
			throw e;
		}
	}

	private Class<?>[] getForcedArgumentTypes(List<LocalFactory> arguments, FactoryDefinition definition) {
		Class<?>[] forcedArgumentTypes = new Class[arguments.size()];
		if (definition.getInstantiationArgFactories() != null) {
			for (int i = 0; i < forcedArgumentTypes.length; i++) {
				String forcedReturnType = definition.getInstantiationArgFactories().get(i).getForcedReturnType();
				if (forcedReturnType != null) {
					if (forcedReturnType.endsWith("[]")) {
						Class<?> componentType = FactoryUtil.getClassForName(forcedReturnType.substring(0,
								forcedReturnType.length() - 2).trim());
						if (componentType == null) {
							throw new ParserException("ScriptFactoryBuilder", "INVALID_PARAMETER_CAST",
									"Error in factory definition " + definition.getRoot().getName()
											+ ": Invalid parameter casting - class not found: "
											+ definition.getInstantiationArgFactories().get(i).getForcedReturnType());

						}
						forcedArgumentTypes[i] = Array.newInstance(componentType, 0).getClass();

					} else {
						forcedArgumentTypes[i] = FactoryUtil.getClassForName(forcedReturnType);
						if (forcedArgumentTypes[i] == null) {
							throw new ParserException("ScriptFactoryBuilder", "INVALID_PARAMETER_CAST",
									"Error in factory definition " + definition.getRoot().getName()
											+ ": Invalid parameter casting - class not found: "
											+ definition.getInstantiationArgFactories().get(i).getForcedReturnType());

						}
					}
				}
			}
		}
		return forcedArgumentTypes;
	}

	protected boolean isString(String value) {
		return value.startsWith("\"") || value.startsWith("'");
	}

}
