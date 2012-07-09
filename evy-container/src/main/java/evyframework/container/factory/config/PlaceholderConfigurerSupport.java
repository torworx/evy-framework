package evyframework.container.factory.config;

import evyframework.common.StringValueResolver;
import evyframework.container.factory.FactoryDefinitionStoreException;
import evyframework.container.factory.registry.FactoryDefinitionRegistry;

public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer {

	/** Default placeholder prefix: {@value} */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	/** Default placeholder suffix: {@value} */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	/** Default value separator: {@value} */
	public static final String DEFAULT_VALUE_SEPARATOR = ":";

	/** Defaults to {@value #DEFAULT_PLACEHOLDER_PREFIX} */
	protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	/** Defaults to {@value #DEFAULT_PLACEHOLDER_SUFFIX} */
	protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	/** Defaults to {@value #DEFAULT_VALUE_SEPARATOR} */
	protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;

	protected boolean ignoreUnresolvablePlaceholders = false;

	protected String nullValue;

	/**
	 * Set the prefix that a placeholder string starts with. The default is
	 * {@value #DEFAULT_PLACEHOLDER_PREFIX}.
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * Set the suffix that a placeholder string ends with. The default is
	 * {@value #DEFAULT_PLACEHOLDER_SUFFIX}.
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * Specify the separating character between the placeholder variable and the
	 * associated default value, or {@code null} if no such special character
	 * should be processed as a value separator. The default is
	 * {@value #DEFAULT_VALUE_SEPARATOR}.
	 */
	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	/**
	 * Set a value that should be treated as {@code null} when resolved as a
	 * placeholder value: e.g. "" (empty String) or "null".
	 * <p>
	 * Note that this will only apply to full property values, not to parts of
	 * concatenated values.
	 * <p>
	 * By default, no such null value is defined. This means that there is no
	 * way to express {@code null} as a property value unless you explicitly map
	 * a corresponding value here.
	 */
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	/**
	 * Set whether to ignore unresolvable placeholders.
	 * <p>
	 * Default is "false": An exception will be thrown if a placeholder fails to
	 * resolve. Switch this flag to "true" in order to preserve the placeholder
	 * String as-is in such a case, leaving it up to other placeholder
	 * configurers to resolve it.
	 */
	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}

	protected void doProcessProperties(FactoryDefinitionRegistry registry, StringValueResolver valueResolver) {
		FactoryDefinitionVisitor visitor = new FactoryDefinitionVisitor(valueResolver);

		String[] factoryNames = registry.getFactoryDefinitionNames();
		for (String curName : factoryNames) {
			// Check that we're not parsing our own factory definition,
			// to avoid failing on unresolvable placeholders in properties file locations.
//			if (!(curName.equals(this.factoryName) && factoryRegistryToProcess.equals(this.factoryRegistry))) {
				FactoryDefinition fd = registry.getFactoryDefinition(curName);
				try {
					visitor.visit(fd);
				}
				catch (Exception ex) {
					throw new FactoryDefinitionStoreException(fd.getResourceDescription(), curName, ex.getMessage());
				}
//			}
		}
	}
	
}
