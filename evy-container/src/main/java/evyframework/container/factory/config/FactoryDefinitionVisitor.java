package evyframework.container.factory.config;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import evyframework.common.Assert;
import evyframework.common.StringValueResolver;

/**
 * Visitor class for traversing {@link FactoryDefinition} objects, in particular
 * the property values and constructor argument values contained in them,
 * resolving factory metadata values.
 *
 * <p>Used by {@link PropertyPlaceholderConfigurer} to parse all String values
 * contained in a FactoryDefinition, resolving any placeholders found.
 *
 */
public class FactoryDefinitionVisitor {
	
	private StringValueResolver valueResolver;
	
	private final ThreadLocal<Set<FactoryDefinition>> currentVisitedFactoryDefinitions = new ThreadLocal<Set<FactoryDefinition>>();

	/**
	 * Create a new FactoryDefinitionVisitor, applying the specified
	 * value resolver to all factory metadata values.
	 * @param valueResolver the StringValueResolver to apply
	 */
	public FactoryDefinitionVisitor(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.valueResolver = valueResolver;
	}

	/**
	 * Create a new FactoryDefinitionVisitor for subclassing.
	 * Subclasses need to override the {@link #resolveStringValue} method.
	 */
	protected FactoryDefinitionVisitor() {
	}

	/**
	 * Traverse the given FactoryDefinition object and the MutablePropertyValues
	 * and ConstructorArgumentValues contained in them.
	 * @param factoryDefinition the FactoryDefinition object to traverse
	 * @see #resolveStringValue(String)
	 */
	public void visit(FactoryDefinition factoryDefinition) {
		currentVisitedFactoryDefinitions.set(new HashSet<FactoryDefinition>());
		visitFactoryDefinition(factoryDefinition);
		currentVisitedFactoryDefinitions.set(null);
	}
	
	protected void visitFactoryDefinition(FactoryDefinition factoryDefinition) {
		if (factoryDefinition != null) {
			Set<FactoryDefinition> visitedFactoryDefinitions = currentVisitedFactoryDefinitions.get();
			if (!visitedFactoryDefinitions.contains(factoryDefinition)) {
				visitedFactoryDefinitions.add(factoryDefinition);
				
				visitIdentifier(factoryDefinition);
				visitIdentifierOwnerClass(factoryDefinition);
				visitFactoryDefinition(factoryDefinition.getIdentifierTargetFactory());
				visitFactoryDefinitions(factoryDefinition.getInstantiationArgFactories());
				visitFactoryDefinitions(factoryDefinition.getInstantiationArgKeyFactories());
				visitPhaseFactories(factoryDefinition);
			}
		}
	}
	
	protected void visitIdentifier(FactoryDefinition factoryDefinition) {
		String identifier = factoryDefinition.getIdentifier();
		if (identifier != null) {
			String resolvedIdentifier = valueResolver.resolveStringValue(identifier);
			if (!identifier.equals(resolvedIdentifier)) {
				factoryDefinition.setIdentifier(resolvedIdentifier);
			}
		}
	}
	
	protected void visitIdentifierOwnerClass(FactoryDefinition factoryDefinition) {
		String indentifierOwnerClass = factoryDefinition.getIdentifierOwnerClass();
		if (indentifierOwnerClass != null) {
			String resolvedValue = valueResolver.resolveStringValue(indentifierOwnerClass);
			if (!indentifierOwnerClass.equals(resolvedValue)) {
				factoryDefinition.setIdentifier(resolvedValue);
			}
		}
	}
	
	protected void visitFactoryDefinitions(List<FactoryDefinition> factoryDefinitions) {
		if (factoryDefinitions != null && !factoryDefinitions.isEmpty()) {
			for (FactoryDefinition definition : factoryDefinitions) {
				visitFactoryDefinition(definition);
			}
		}
	}
	
	protected void visitPhaseFactories(FactoryDefinition factoryDefinition) {
		Map<String, List<FactoryDefinition>> phaseFactories = factoryDefinition.getPhaseFactories();
		if (phaseFactories != null && !phaseFactories.isEmpty()) {
			for (Entry<String, List<FactoryDefinition>> entry : phaseFactories.entrySet()) {
				visitFactoryDefinitions(entry.getValue());
			}
		}
	}
	
}
