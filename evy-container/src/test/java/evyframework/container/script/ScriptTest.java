package evyframework.container.script;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import evyframework.container.DefaultContainer;
import evyframework.container.factory.config.PropertyPlaceholderConfigurer;
import evyframework.container.script.ScriptFactoryBuilder;

public class ScriptTest {
	
	private DefaultContainer container;
	
	@Before
	public void setup() {
		container = new DefaultContainer();
	}
	
	@Test
	public void testPostProcessor() {
		Properties properties = new Properties();
		properties.setProperty("name", "Sky");
		PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
		propertyPlaceholderConfigurer.setProperties(properties);
		
		container.addBeanContextPostProcessor(propertyPlaceholderConfigurer);
		
		ScriptFactoryBuilder builder = new ScriptFactoryBuilder(container);
		builder.addFactory("person = 1 evyframework.container.script.Person().setName(\"${name}\"); ");
		
		Person product = container.getInstance("person");
		assertEquals("Sky", product.getName());
	}
	
	@Test
	public void testPostProcessorWhithConfig() {
		Properties properties = new Properties();
		properties.setProperty("name", "Sky");
		PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
		propertyPlaceholderConfigurer.setProperties(properties);
		
		container.addBeanContextPostProcessor(propertyPlaceholderConfigurer);
		
		ScriptFactoryBuilder builder = new ScriptFactoryBuilder(container);
		builder.addFactory("person = 1 evyframework.container.script.Person().setName(\"${name}\"); " +
				"config {$person.setName(\"Cloud\");}");
		
		Person product = container.getInstance("person");
		assertEquals("Cloud", product.getName());
	}
	
	@Test
	public void testFactoryUpdateFactory() {
		ScriptFactoryBuilder builder = new ScriptFactoryBuilder(container);
		builder.addFactory("person = 1 evyframework.container.script.Person().setName(\"${name}\"); ");
		builder.addFactory("&person config{$person.setName(\"Cloud\");}");
		
		Person product = container.getInstance("person");
		assertEquals("Cloud", product.getName());
	}

}
