package evyframework.oxm.xstream;

import com.thoughtworks.xstream.XStream;

import evyframework.oxm.Validatable;
import evyframework.oxm.ValidateOptions;

public class ClassConf implements XStreamConfig, Validatable {
	
	private Class<?> type;
	
	private String alias;

	public ClassConf(Class<?> type) {
		this(type, null);
	}

	public ClassConf(Class<?> type, String alias) {
		super();
		this.type = type;
		this.alias = alias;
	}

	@Override
	public void validate(ValidateOptions options) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void doConfig(XStream xstream) {
		if (alias != null) {
			xstream.alias(getAlias(), getType());
		}
	}


	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
