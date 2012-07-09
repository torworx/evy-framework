package evyframework.oxm.xstream;

import evyframework.common.ReflectionUtils;
import evyframework.oxm.ConfigValidationError;
import evyframework.oxm.Validatable;
import evyframework.oxm.ValidateOptions;

public abstract class AbstractFieldConf implements XStreamConfig, Validatable {
	
	private Class<?> definedIn;
	
	private String fieldName;

	public AbstractFieldConf(Class<?> definedIn, String fieldName) {
		super();
		this.definedIn = definedIn;
		this.fieldName = fieldName;
	}

	@Override
	public void validate(ValidateOptions options) {
		if (ReflectionUtils.findField(definedIn, fieldName) == null) {
			options.addError(new ConfigValidationError(String.format("Field '%s' not exist in %s", fieldName, definedIn.getName())));
		}
		
	}

	public Class<?> getDefinedIn() {
		return definedIn;
	}

	public void setDefinedIn(Class<?> definedIn) {
		this.definedIn = definedIn;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	

}
