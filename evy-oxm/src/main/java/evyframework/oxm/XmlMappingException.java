package evyframework.oxm;

/**
 * Root of the hierarchy of Object XML Mapping exceptions.
 */
public abstract class XmlMappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct an <code>XmlMappingException</code> with the specified detail message.
	 * @param msg the detail message
	 */
    public XmlMappingException(String msg) {
        super(msg);
    }

	/**
	 * Construct an <code>XmlMappingException</code> with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
    public XmlMappingException(String msg, Throwable cause) {
        super(msg, cause);
    }

	public XmlMappingException() {
		super();
	}

	public XmlMappingException(Throwable cause) {
		super(cause);
	}

}