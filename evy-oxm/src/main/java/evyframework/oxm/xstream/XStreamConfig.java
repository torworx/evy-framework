package evyframework.oxm.xstream;

import com.thoughtworks.xstream.XStream;

import evyframework.oxm.Config;

interface XStreamConfig extends Config<XStream> {
	
	void doConfig(XStream xstream);
	
}
