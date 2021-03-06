/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.core.internal.beans;

import java.util.function.Consumer;

import com.holonplatform.core.beans.StringBeanProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.StringProperty;

/**
 * Default {@link StringBeanProperty} implementation.
 *
 * @since 5.1.0
 */
public class DefaultStringBeanProperty extends AbstractBeanProperty<String> implements StringBeanProperty {

	private static final long serialVersionUID = 8247229897767365467L;

	/**
	 * Constructor.
	 * @param name Property name (not null)
	 */
	public DefaultStringBeanProperty(String name) {
		super(name, String.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.property.StringProperty#clone(java.util.function.Consumer)
	 */
	@Override
	public StringProperty clone(Consumer<PathProperty.Builder<String, PathProperty<String>, ?>> builder) {
		return clonePathProperty(new DefaultStringBeanProperty(getName()), builder);
	}

}
