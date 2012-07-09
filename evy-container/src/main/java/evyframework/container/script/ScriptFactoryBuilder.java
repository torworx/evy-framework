/*
    Copyright 2007-2010 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package evyframework.container.script;

import evyframework.common.io.ByteArrayResource;
import evyframework.common.io.Resource;
import evyframework.container.MutableContainer;
import evyframework.container.context.support.DefaultBeanContext;

/**
 * A ScriptFactoryBuilder is capable of parsing Evy Container Script into
 * factories and add them to an Container instance.
 */
public class ScriptFactoryBuilder {

	private final DefaultBeanContext beanContext;
	private final ScriptFactoryDefinitionParser parser;
	
	/**
	 * Creates a new ScriptFactoryBuilder that adds its factories to the given
	 * container.
	 * 
	 * @param container
	 *            The container the ScriptFactoryBuilder is to add factories to.
	 */
	public ScriptFactoryBuilder(MutableContainer container) {
		beanContext = new DefaultBeanContext(container);
		parser = new ScriptFactoryDefinitionParser(beanContext.getRegistry());
	}

	/**
	 * Parses the given script and adds the corresponding factory to the
	 * container. Note: The script should only define a single factory.
	 * 
	 * @param factoryScript
	 *            The script defining the factory to add.
	 */
	public void addFactory(String factoryScript) {
		parser.loadFactoryDefinitions(new ByteArrayResource(factoryScript.getBytes()));
		beanContext.publish();
	}

	/**
	 * Parses the given script string and replaces the corresponding factory. If
	 * no factory exists with the given name, a new factory is created.
	 * 
	 * @param factoryScript
	 *            The script defining the factory to replace.
	 */
	public void replaceFactory(String factoryScript) {
		parser.loadFactoryDefinitions(new ByteArrayResource(factoryScript.getBytes()));
		beanContext.publish(true);
	}

	public void addFactories(String... locations) {
		parser.loadFactoryDefinitions(locations);
		beanContext.publish();
	}

	public void addFactories(Resource... resources) {
		parser.loadFactoryDefinitions(resources);
		beanContext.publish();
	}
	
	public void replaceFactories(String... locations) {
		parser.loadFactoryDefinitions(locations);
		beanContext.publish(true);
	}

	public void replaceFactories(Resource... resources) {
		parser.loadFactoryDefinitions(resources);
		beanContext.publish(true);
	}

}