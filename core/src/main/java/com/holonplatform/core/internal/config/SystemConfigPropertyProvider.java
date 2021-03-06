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
package com.holonplatform.core.internal.config;

import java.util.Enumeration;
import java.util.stream.Stream;

import com.holonplatform.core.config.ConfigPropertyProvider;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * A {@link ConfigPropertyProvider} using {@link System} properties.
 *
 * @since 5.0.0
 */
public class SystemConfigPropertyProvider implements ConfigPropertyProvider {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.config.ConfigPropertyProvider#containsProperty(java.lang.String)
	 */
	@Override
	public boolean containsProperty(String key) {
		return System.getProperty(key) != null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.config.ConfigPropertyProvider#getProperty(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T getProperty(String key, Class<T> targetType) throws IllegalArgumentException {
		ObjectUtils.argumentNotNull(key, "Property name must be not null");
		ObjectUtils.argumentNotNull(targetType, "Property type must be not null");

		String value = System.getProperty(key);
		if (value != null) {
			return ConversionUtils.convertStringValue(value, targetType);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.config.ConfigPropertyProvider#getPropertyNames()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Stream<String> getPropertyNames() throws UnsupportedOperationException {
		return ConversionUtils.enumerationAsStream((Enumeration<String>) System.getProperties().propertyNames());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SystemConfigPropertyProvider";
	}

}
