package evyframework.oxm.xstream;

import com.thoughtworks.xstream.XStream;

public class ImplicitCollectionFieldConf extends AbstractFieldConf {

	private String itemFieldName;

	private Class<?> itemType;

	public ImplicitCollectionFieldConf(Class<?> definedIn, String fieldName) {
		this(definedIn, fieldName, null);
	}

	public ImplicitCollectionFieldConf(Class<?> definedIn, String fieldName, Class<?> itemType) {
		this(definedIn, fieldName, null, itemType);
	}

	public ImplicitCollectionFieldConf(Class<?> definedIn, String fieldName, String itemFieldName, Class<?> itemType) {
		super(definedIn, fieldName);
		this.itemFieldName = itemFieldName;
		this.itemType = itemType;
	}

	@Override
	public void doConfig(XStream xstream) {
		xstream.addImplicitCollection(getDefinedIn(), getFieldName(), getItemFieldName(), getItemType());
	}

	public String getItemFieldName() {
		return itemFieldName;
	}

	public void setItemFieldName(String itemFieldName) {
		this.itemFieldName = itemFieldName;
	}

	public Class<?> getItemType() {
		return itemType;
	}

	public void setItemType(Class<?> itemType) {
		this.itemType = itemType;
	}

}
