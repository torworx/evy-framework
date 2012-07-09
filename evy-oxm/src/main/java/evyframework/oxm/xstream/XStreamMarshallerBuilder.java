package evyframework.oxm.xstream;

public class XStreamMarshallerBuilder extends XStreamConfiguration<XStreamMarshallerBuilder> {
	
	private boolean validating = true;
	
	public boolean isValidating() {
		return validating;
	}

	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	@Override
	public void validate() {
		if (isValidating()) {
			super.validate();
		}
	}

	public XStreamMarshaller build() {
		validate();
		return new XStreamMarshaller(this);
	}
}
