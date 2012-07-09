package evyframework.oxm.xstream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.XStream;

public class ClassConfBuilder<T extends XStreamConfiguration<T>> {
	
	private final T configurator;

	private final ClassConf classConf;

	private final List<XStreamConfig> configs;

	public ClassConfBuilder(T configurator, ClassConf classConf) {
		this.configurator = configurator;
		this.configs = configurator.configs;
		this.classConf = classConf;
	}

	public FieldConfBuilder<T> forField(String fieldName) {
		FieldConf fieldConf = new FieldConf(classConf.getType(), fieldName);
		configs.add(fieldConf);
		return new FieldConfBuilder<T>(this, fieldConf);
	}

	public FieldConfBuilder<T> aliasField(String fieldName, String alias) {
		FieldConf fieldConf = new FieldConf(classConf.getType(), fieldName, alias);
		configs.add(fieldConf);
		return new FieldConfBuilder<T>(this, fieldConf);
	}
	
	/**
	 * Alias fields fieldName/alias map
	 * @param aliases
	 * @see XStream#aliasField(String, Class, String) 
	 */
	public ClassConfBuilder<T> aliasFields(Map<String, String> aliases) {
		for (Entry<String, String> entry : aliases.entrySet()) {
			configs.add(new FieldConf(classConf.getType(), entry.getKey(), entry.getValue()));
		}
		return this;
	}
	
	public ClassConfBuilder<T> omitFields(final Collection<String> fieldNames) {
		return omitFields(fieldNames.toArray(new String[]{}));
	}

	public ClassConfBuilder<T> omitFields(final String... fieldNames) {
		if (fieldNames.length > 0) {
			configs.add(new XStreamConfig() {
				
				@Override
				public void doConfig(XStream xstream) {
					for (String name : fieldNames) {
						xstream.omitField(classConf.getType(), name);
					}
				}
			});
		}
		return this;
	}

	public ClassConfBuilder<T> implicitCollections(String... fieldNames) {
		for (String name : fieldNames) {
			configs.add(new ImplicitCollectionFieldConf(classConf.getType(), name));
		}
		return this;
	}

	public ClassConfBuilder<T> implicitCollection(String fieldName, String itemFieldName, Class<?> itemType) {
		ImplicitCollectionFieldConf conf = new ImplicitCollectionFieldConf(classConf.getType(), fieldName, itemFieldName, itemType);
		configs.add(conf);
		return this;
	}
	
	public ClassConfBuilder<T> implicitCollection(String fieldName, Class<?> itemType) {
		ImplicitCollectionFieldConf conf = new ImplicitCollectionFieldConf(classConf.getType(), fieldName, itemType);
		configs.add(conf);
		return this;
	}
	
	public T endClass() {
		return configurator;
	}
}
