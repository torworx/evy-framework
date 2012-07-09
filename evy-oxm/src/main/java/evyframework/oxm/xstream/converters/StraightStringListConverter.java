package evyframework.oxm.xstream.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class StraightStringListConverter extends AbstractCollectionConverter {
	
	private static final Splitter STRING_SPLITTER = Splitter.on(CharMatcher.anyOf(",;\t\n"));

	public StraightStringListConverter(Mapper mapper) {
		super(mapper);
	}

    @SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
//        return type.equals(ArrayList.class)
//                || type.equals(HashSet.class)
//                || type.equals(LinkedList.class)
//                || type.equals(Vector.class)
//                || (JVM.is14() && type.getName().equals("java.util.LinkedHashSet"));
    	return Collection.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        String value = "";
    	Collection<Object> collection = (Collection<Object>) source;
        for (Iterator<Object> iterator = collection.iterator(); iterator.hasNext();) {
            Object item = iterator.next();
            value += item.toString();
            if (iterator.hasNext()) {
            	value += ",";
            }
        }
        writer.setValue(value);
    }

    @SuppressWarnings("unchecked")
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    	Collection<Object> collection = null;
    	try {
    		collection = (Collection<Object>) createCollection(context.getRequiredType());
		} catch (Exception e) {
			collection = new ArrayList<Object>();
		}
        populateCollection(reader, context, collection);
        return collection;
    }

    protected void populateCollection(HierarchicalStreamReader reader, UnmarshallingContext context, Collection<Object> collection) {
        populateCollection(reader, context, collection, collection);
    }

    protected void populateCollection(HierarchicalStreamReader reader, UnmarshallingContext context, Collection<Object> collection, Collection<Object> target) {
    	String value = reader.getValue();
    	Iterator<String> values = STRING_SPLITTER.split(value).iterator();
    	while (values.hasNext()) {
			target.add(values.next());
		}
    }

}
