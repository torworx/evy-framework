package evyframework.oxm.xstream;

import com.thoughtworks.xstream.converters.ConverterMatcher;

public class FieldConfBuilder<T extends XStreamConfiguration<T>> {
	
	private final ClassConfBuilder<T> classConfBuilder;
	private final FieldConf fieldConf;
	
	public FieldConfBuilder(ClassConfBuilder<T> classConfBuilder, FieldConf fieldConf) {
		super();
		this.classConfBuilder = classConfBuilder;
		this.fieldConf = fieldConf;
	}

	public FieldConfBuilder<T> asAttribute() {
		this.fieldConf.setAsAttribute(true);
		return this;
	}
	
	public FieldConfBuilder<T> converter(ConverterMatcher converter) {
		this.fieldConf.setConverter(converter);
		return this;
	}
	
	public FieldConfBuilder<T> converter(Class<? extends ConverterMatcher> converterClass) {
		this.fieldConf.setConverterClass(converterClass);
		return this;
	}
	
	public ClassConfBuilder<T> endField() {
		return classConfBuilder;
	}
	
}
