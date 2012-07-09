package evyframework.oxm.xstream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

import evyframework.oxm.Marshaller;
import evyframework.oxm.MarshallingFailureException;
import evyframework.oxm.UncategorizedMappingException;
import evyframework.oxm.UnmarshallingFailureException;
import evyframework.oxm.XmlMappingException;

public class XStreamMarshaller implements Marshaller {
	
	private static final Logger logger = LoggerFactory.getLogger(XStreamMarshaller.class);

	/**
	 * The default encoding used for stream access: UTF-8.
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";
	
	private static final HierarchicalStreamDriver DEFAULT_DRIVER = new CompactXppDriver();

	private XStream xstream;

	private HierarchicalStreamDriver streamDriver;

	private String encoding = DEFAULT_ENCODING;

	private Class<?>[] supportedClasses;

	public XStreamMarshaller(XStream xstream) {
		this.xstream = xstream;
	}

	public XStreamMarshaller(XStreamConfig config) {
		this(new XStream());
		config.doConfig(xstream);
	}

	/**
	 * Returns the XStream instance used by this marshaller.
	 */
	public XStream getXStream() {
		return this.xstream;
	}

	/**
	 * Set the XStream mode.
	 * 
	 * @see XStream#XPATH_REFERENCES
	 * @see XStream#ID_REFERENCES
	 * @see XStream#NO_REFERENCES
	 */
	public void setMode(int mode) {
		this.getXStream().setMode(mode);
	}
	
	public HierarchicalStreamDriver getStreamDriver() {
		return streamDriver != null ? streamDriver : DEFAULT_DRIVER;
	}

	/**
	 * Set the XStream hierarchical stream driver to be used with stream readers
	 * and writers.
	 */
	public void setStreamDriver(HierarchicalStreamDriver streamDriver) {
		this.streamDriver = streamDriver;
	}

	/**
	 * Set the encoding to be used for stream access.
	 * 
	 * @see #DEFAULT_ENCODING
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set the classes supported by this marshaller.
	 * <p>
	 * If this property is empty (the default), all classes are supported.
	 * 
	 * @see #supports(Class)
	 */
	public void setSupportedClasses(Class<?>[] supportedClasses) {
		this.supportedClasses = supportedClasses;
	}
	
	public boolean supports(Class<?> clazz) {
		if (ObjectUtils.isEmpty(this.supportedClasses)) {
			return true;
		}
		else {
			for (Class<?> supportedClass : this.supportedClasses) {
				if (supportedClass.isAssignableFrom(clazz)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public String marshal(Object obj) {
		Writer writer = new StringWriter();
		marshalWriter(obj, writer);
		return writer.toString();
	}

	public void marshal(Object obj, Writer out) {
		marshalWriter(obj, out);
	}

	public void marshal(Object obj, OutputStream out) {
		try {
			marshalWriter(obj, new OutputStreamWriter(out, this.encoding));
		} catch (UnsupportedEncodingException e) {
			throw new MarshallingFailureException(e.getMessage(), e);
		}
	}

	protected void marshalWriter(Object graph, Writer writer) {
		if (this.streamDriver != null) {
			marshal(graph, this.streamDriver.createWriter(writer));
		} else {
			marshal(graph, new CompactWriter(writer));
		}
	}

	/**
	 * Marshals the given graph to the given XStream HierarchicalStreamWriter.
	 * Converts exceptions using {@link #convertXStreamException}.
	 */
	private void marshal(Object graph, HierarchicalStreamWriter streamWriter) {
		try {
			getXStream().marshal(graph, streamWriter);
		} catch (Exception ex) {
			throw convertXStreamException(ex, true);
		} finally {
			try {
				streamWriter.flush();
			} catch (Exception ex) {
				logger.debug("Could not flush HierarchicalStreamWriter", ex);
			}
		}
	}

	public Object unmarshal(String source) {
		return unmarshalReader(new StringReader(source));
	}

	public Object unmarshal(Reader source) {
		return unmarshalReader(source);
	}

	public Object unmarshal(InputStream source) {
		try {
			return unmarshalReader(new InputStreamReader(source, this.encoding));
		} catch (UnsupportedEncodingException e) {
			throw new MarshallingFailureException(e.getMessage(), e);
		}
	}

	protected Object unmarshalReader(Reader reader) {
		return unmarshal(getStreamDriver().createReader(reader));
	}

	private Object unmarshal(HierarchicalStreamReader streamReader) {
		try {
			return this.getXStream().unmarshal(streamReader);
		} catch (Exception ex) {
			throw convertXStreamException(ex, false);
		}
	}
	
	protected Object unmarshalReader(Reader reader, Object root) {
		return unmarshal(getStreamDriver().createReader(reader), root);
	}

	public Object unmarshal(String source, Object root) {
		return unmarshalReader(new StringReader(source), root);
	}

	public Object unmarshal(Reader source, Object root) {
		return unmarshalReader(source, root);
	}

	public Object unmarshal(InputStream source, Object root) {
		try {
			return unmarshalReader(new InputStreamReader(source, this.encoding), root);
		} catch (UnsupportedEncodingException e) {
			throw new MarshallingFailureException(e.getMessage(), e);
		}
	}
	
	private Object unmarshal(HierarchicalStreamReader streamReader, Object root) {
		try {
			return this.getXStream().unmarshal(streamReader, root);
		} catch (Exception ex) {
			throw convertXStreamException(ex, false);
		}
	}

	/**
	 * Convert the given XStream exception to an appropriate exception from the
	 * <code>org.springframework.oxm</code> hierarchy.
	 * <p>
	 * A boolean flag is used to indicate whether this exception occurs during
	 * marshalling or unmarshalling, since XStream itself does not make this
	 * distinction in its exception hierarchy.
	 * 
	 * @param ex
	 *            XStream exception that occured
	 * @param marshalling
	 *            indicates whether the exception occurs during marshalling (
	 *            <code>true</code>), or unmarshalling (<code>false</code>)
	 * @return the corresponding <code>XmlMappingException</code>
	 */
	protected XmlMappingException convertXStreamException(Exception ex, boolean marshalling) {
		if (ex instanceof StreamException || ex instanceof CannotResolveClassException
				|| ex instanceof ConversionException) {
			if (marshalling) {
				return new MarshallingFailureException("XStream marshalling exception", ex);
			} else {
				return new UnmarshallingFailureException("XStream unmarshalling exception", ex);
			}
		} else {
			// fallback
			return new UncategorizedMappingException("Unknown XStream exception", ex);
		}
	}
	
	private static class CompactXppDriver extends XppDriver {

		@Override
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new CompactWriter(out);
		}
		
	}

}
