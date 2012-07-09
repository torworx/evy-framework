package evyframework.oxm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface Marshaller {
	
	String marshal(Object target);
	
	void marshal(Object obj, Writer out);
	
	void marshal(Object obj, OutputStream out);
	
	Object unmarshal(String source);
	
	Object unmarshal(Reader source);
	
	Object unmarshal(InputStream source);
	
	Object unmarshal(String source, Object root);
	
	Object unmarshal(Reader source, Object root);
	
	Object unmarshal(InputStream source, Object root);
}
