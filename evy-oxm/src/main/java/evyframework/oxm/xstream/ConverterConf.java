package evyframework.oxm.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.converters.SingleValueConverter;

import evyframework.oxm.Validatable;
import evyframework.oxm.ValidateOptions;

public class ConverterConf implements XStreamConfig, Validatable {
	
	private ConverterMatcher converter;
	
	private Integer priority;

	public ConverterConf(ConverterMatcher converter) {
		this(converter, null);
	}

	public ConverterConf(ConverterMatcher converter, Integer priority) {
		super();
		this.converter = converter;
		this.priority = priority;
	}

	@Override
	public void validate(ValidateOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doConfig(XStream xstream) {
		Object[] dependencies = new Object[] {xstream, xstream.getMapper()};
		Object conv = ConverterResolver.resolve(converter, Converter.class, dependencies);
		if (conv != null) {
			if (getPriority() == null) {
				xstream.registerConverter((Converter) conv);
			} else {
				xstream.registerConverter((Converter) conv, priority);
			}
		}
		conv = ConverterResolver.resolve(converter, SingleValueConverter.class, dependencies);
		if (conv != null) {
			if (getPriority() == null) {
				xstream.registerConverter((SingleValueConverter) conv);
			} else {
				xstream.registerConverter((SingleValueConverter) conv, getPriority());
			}
		}
	}

	public ConverterMatcher getConverter() {
		return converter;
	}

	public void setConverter(ConverterMatcher converter) {
		this.converter = converter;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}
