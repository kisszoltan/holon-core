/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.core.internal.datastore.bulk;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.internal.datastore.operation.common.AbstractBulkDeleteOperation;

/**
 * Abstract {@link BulkDelete} implementation.
 *
 * @since 5.2.0
 */
public abstract class AbstractBulkDelete extends AbstractBulkDeleteOperation<OperationResult, BulkDelete>
		implements BulkDelete {

	private static final long serialVersionUID = 4417346841028953203L;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.operation.AbstractDatastoreOperation#getActualOperation()
	 */
	@Override
	protected BulkDelete getActualOperation() {
		return this;
	}

}
