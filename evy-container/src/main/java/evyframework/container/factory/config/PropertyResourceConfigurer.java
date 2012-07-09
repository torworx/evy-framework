package evyframework.container.factory.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import evyframework.common.ObjectUtils;
import evyframework.common.io.support.PropertiesLoaderSupport;
import evyframework.container.context.MutableBeanContext;
import evyframework.container.factory.FactoryInitializationException;

/**
 * Allows for configuration of individual bean property values from a property
 * resource, i.e. a properties file. Useful for custom config files targeted at
 * system administrators that override bean properties configured in the
 * application context.
 * 
 * <p>
 * Two concrete implementations are provided in the distribution:
 * <ul>
 * <li>{@link PropertyOverrideConfigurer} for "beanName.property=value" style
 * overriding (<i>pushing</i> values from a properties file into bean
 * definitions)
 * <li>{@link PropertyPlaceholderConfigurer} for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * </ul>
 * 
 * <p>
 * Property values can be converted after reading them in, through overriding
 * the {@link #convertPropertyValue} method. For example, encrypted values can
 * be detected and decrypted accordingly before processing them.
 * 
 */
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport implements BeanContextPostProcessor {

	@Override
	public void postProcessBeanContext(MutableBeanContext beanContext) {
		try {
			Properties mergedProps = mergeProperties();

			// Convert the merged properties, if necessary.
			convertProperties(mergedProps);

			// Let the subclass process the properties.
			processProperties(beanContext, mergedProps);
		} catch (IOException ex) {
			throw new FactoryInitializationException("PropertyResourceConfigurer", "PROCESS_PROPERTY",
					"Could not load properties", ex);
		}
	}

	/**
	 * Convert the given merged properties, converting property values if
	 * necessary. The result will then be processed.
	 * <p>
	 * The default implementation will invoke {@link #convertPropertyValue} for
	 * each property value, replacing the original with the converted value.
	 * 
	 * @param props
	 *            the Properties to convert
	 * @see #processProperties
	 */
	protected void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertProperty(propertyName, propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	/**
	 * Convert the given property from the properties source to the value which
	 * should be applied.
	 * <p>
	 * The default implementation calls {@link #convertPropertyValue(String)}.
	 * 
	 * @param propertyName
	 *            the name of the property that the value is defined for
	 * @param propertyValue
	 *            the original value from the properties source
	 * @return the converted value, to be used for processing
	 * @see #convertPropertyValue(String)
	 */
	protected String convertProperty(String propertyName, String propertyValue) {
		return convertPropertyValue(propertyValue);
	}

	/**
	 * Convert the given property value from the properties source to the value
	 * which should be applied.
	 * <p>
	 * The default implementation simply returns the original value. Can be
	 * overridden in subclasses, for example to detect encrypted values and
	 * decrypt them accordingly.
	 * 
	 * @param originalValue
	 *            the original value from the properties source (properties file
	 *            or local "properties")
	 * @return the converted value, to be used for processing
	 * @see #setProperties
	 * @see #setLocations
	 * @see #setLocation
	 * @see #convertProperty(String, String)
	 */
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}

	protected abstract void processProperties(MutableBeanContext beanContext, Properties props);

}