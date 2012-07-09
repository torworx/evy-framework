package evyframework.oxm.xstream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.ClassUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterMatcher;

import evyframework.oxm.ConfigError;
import evyframework.oxm.ConfigValidationError;
import evyframework.oxm.Configuration;
import evyframework.oxm.Validatable;
import evyframework.oxm.ValidateOptions;
import evyframework.oxm.ValidationFailureException;

@SuppressWarnings("unchecked")
public class XStreamConfiguration<T extends XStreamConfiguration<T>> implements Configuration, XStreamConfig {

	protected final List<XStreamConfig> configs = Lists.newArrayList();
	
	private ClassLoader classLoader;
	
	private ValidateOptions validateOptions;

	public ClassLoader getClassLoader() {
		return classLoader != null ? classLoader : XStreamConfiguration.class.getClassLoader();
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ValidateOptions getValidateOptions() {
		return validateOptions;
	}

	public void setValidateOptions(ValidateOptions validateOptions) {
		this.validateOptions = validateOptions;
	}

	protected void validate(ValidateOptions options) {
		for (XStreamConfig config : configs) {
			if (config instanceof Validatable) {
				((Validatable) config).validate(options);
			}
		}
	}
	
	public void validate() {
		ValidateOptions options = validateOptions != null ? validateOptions : new ValidateOptions();
		Collection<?> errorsList = Lists.newArrayList();
		options.setErrorList(errorsList);
		validate(options);
		if (!errorsList.isEmpty()) {
			StringBuilder builder = new StringBuilder("Config validation failure :");
			for (Object anErrorsList : errorsList) {
				ConfigError error = (ConfigError) anErrorsList;
				if (error instanceof ConfigValidationError) {
					builder.append(error.toString());
				}
			}
			throw new ValidationFailureException(builder.toString(), null, errorsList);
		}
	}

	@Override
	public void doConfig(XStream xstream) {
		for (XStreamConfig config : configs) {
			config.doConfig(xstream);
		}
	}
	
	public T apply(Collection<XStreamMapper> mappers) {
		apply(mappers.toArray(new XStreamMapper[]{}));
		return (T) this;
	}
	
	public T apply(XStreamMapper... mappers) {
		for (XStreamMapper mapper : mappers) {
			mapper.configure(this);
		}
		return (T) this;
	}

	public ClassConfBuilder<T> forClass(Class<?> type) {
		ClassConf conf = new ClassConf(type);
		configs.add(conf);
		return new ClassConfBuilder<T>((T) this, conf);
	}

	public ClassConfBuilder<T> aliasClass(Class<?> type, String alias) {
		ClassConf conf = new ClassConf(type, alias);
		configs.add(conf);
		return new ClassConfBuilder<T>((T) this, conf);
	}
	
	public T aliasClasses(Map<?, String> aliases) {
		Map<Class<?>, String> classMap = toClassMap(aliases);
		for (Entry<Class<?>, String> entry : classMap.entrySet()) {
			configs.add(new ClassConf(entry.getKey(), entry.getValue()));
		}
		return (T) this;
	}
	
	
	public T aliasType(final Class<?> type, final String alias) {
		configs.add(new XStreamConfig() {
			
			@Override
			public void doConfig(XStream xstream) {
				xstream.aliasType(alias, type);
			}
		});
		return (T) this;
	}
	
	public T aliasTypes(Map<?, String> aliases) {
		Map<Class<?>, String> classMap = toClassMap(aliases);
		for (Entry<Class<?>, String> entry : classMap.entrySet()) {
			aliasType(entry.getKey(), entry.getValue());
		}
		return (T) this;
	}
	
	private Map<Class<?>, String> toClassMap(Map<?, String> map) {
		Map<Class<?>, String> result = Maps.newLinkedHashMap();

		for (Map.Entry<?, String> entry : map.entrySet()) {
			Object key = entry.getKey();
			String value = entry.getValue();
			Class<?> type;
			if (key instanceof Class) {
				type = (Class<?>) key;
			}
			else if (key instanceof String) {
				String s = (String) key;
				try {
					type = ClassUtils.forName(s, getClassLoader());
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException(e);
				} catch (LinkageError e) {
					throw new IllegalArgumentException(e);
				}
			}
			else {
				throw new IllegalArgumentException("Unknown value [" + key + "], expected String or Class");
			}
			result.put(type, value);
		}
		return result;
	}
	public T registerConverters(Collection<ConverterMatcher> converters) {
		return registerConverters(converters.toArray(new ConverterMatcher[]{}));
	}

	public T registerConverters(ConverterMatcher... converters) {
		for (ConverterMatcher converter : converters) {
			configs.add(new ConverterConf(converter));
		}
		return (T) this;
	}

	public T registerConverter(ConverterMatcher converter, int priority) {
		configs.add(new ConverterConf(converter, priority));
		return (T) this;
	}
	
	public T asAttributeForTypes(final Collection<Class<?>> types) {
		return asAttributeForTypes(types.toArray(new Class<?>[]{}));
	}
	
	public T asAttributeForTypes(final Class<?>... types) {
		configs.add(new XStreamConfig() {
			
			@Override
			public void doConfig(XStream xstream) {
				for (Class<?> type : types) {
					xstream.useAttributeFor(type);
				}
			}
		});
		return (T) this;
	}
	
	public T asAttribute(final String fieldName, final Class<?> type) {
		configs.add(new XStreamConfig() {
			
			@Override
			public void doConfig(XStream xstream) {
				xstream.useAttributeFor(fieldName, type);
			}
		});
		
		return (T) this;
	}
	
	public T annotatedClasses(final Collection<Class<?>> annotatedClasses) {
		return annotatedClasses(annotatedClasses.toArray(new Class<?>[]{}));
	}
	
	public T annotatedClasses(final Class<?>... annotatedClasses) {
		configs.add(new XStreamConfig() {
			
			@Override
			public void doConfig(XStream xstream) {
				xstream.processAnnotations(annotatedClasses);
			}
		});
		return (T) this;
	}
	
	public T autodetectAnnotations(final boolean autodetectAnnotations) {
		configs.add(new XStreamConfig() {
			
			@Override
			public void doConfig(XStream xstream) {
				xstream.autodetectAnnotations(autodetectAnnotations);
			}
		});
		return (T) this;
	}
	
}
