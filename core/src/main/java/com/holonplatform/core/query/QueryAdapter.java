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
package com.holonplatform.core.query;

import java.util.stream.Stream;

import com.holonplatform.core.exceptions.DataAccessException;

/**
 * Adapter to perform a <em>query</em> execution using a {@link QueryOperation}.
 * 
 * @since 5.0.0
 * 
 * @param <C> Query configuration type
 */
public interface QueryAdapter<C extends QueryConfiguration> {

	/**
	 * Execute a query using provided {@link QueryOperation} and return query results as a {@link Stream}.
	 * @param <R> Query results type
	 * @param queryOperation Query operation (not null)
	 * @return Query results stream. The stream elements type must match the query projection type
	 * @throws DataAccessException If a query execution error occurred
	 */
	<R> Stream<R> stream(QueryOperation<C, R> queryOperation) throws DataAccessException;

}
