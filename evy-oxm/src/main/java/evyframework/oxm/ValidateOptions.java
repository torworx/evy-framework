package evyframework.oxm;

import java.util.Collection;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ValidateOptions {
	
	private Collection errorList;
	
	public void addError(Object errorObject) {
		if (errorList != null) {
			errorList.add(errorObject);
		}
	}
	
	public void setErrorList(Collection errorList) {
		this.errorList = errorList;
	}

}
