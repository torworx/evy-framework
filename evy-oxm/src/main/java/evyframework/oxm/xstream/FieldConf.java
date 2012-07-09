package evyframework.oxm.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.converters.SingleValueConverter;

public class FieldConf extends AbstractFieldConf {

	private String alias;

	private boolean asAttribute;

	private ConverterMatcher converter;

	private Class<? extends ConverterMatcher> converterClass;

	public FieldConf(Class<?> definedIn, String fieldName) {
		super(definedIn, fieldName);
	}

	public FieldConf(Class<?> definedIn, String fieldName, String alias) {
		super(definedIn, fieldName);
		this.alias = alias;
	}

	@Override
	public void doConfig(XStream xstream) {
		if (alias != null) {
			xstream.aliasField(alias, getDefinedIn(), getFieldName());

		}
		if (asAttribute) {
			xstream.useAttributeFor(getDefinedIn(), getFieldName());
		}

		Object conv = converter != null ? converter : converterClass;

		if (conv != null) {
			Object[] dependencies = new Object[] { xstream, xstream.getMapper() };
			Object converter = ConverterResolver.resolve(conv, Converter.class, dependencies);
			if (converter != null) {
				xstream.registerLocalConverter(getDefinedIn(), getFieldName(), (Converter) converter);
			}
			converter = ConverterResolver.resolve(conv, SingleValueConverter.class, dependencies);
			if (converter != null) {
				xstream.registerLocalConverter(getDefinedIn(), getFieldName(), (SingleValueConverter) converter);
			}
		}
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isAsAttribute() {
		return asAttribute;
	}

	public void setAsAttribute(boolean asAttribute) {
		this.asAttribute = asAttribute;
	}

	public ConverterMatcher getConverter() {
		return converter;
	}

	public void setConverter(ConverterMatcher converter) {
		this.converter = converter;
	}

	public Class<? extends ConverterMatcher> getConverterClass() {
		return converterClass;
	}

	public void setConverterClass(Class<? extends ConverterMatcher> converterClass) {
		this.converterClass = converterClass;
	}

}
