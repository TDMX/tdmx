/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.FieldValidationException;

/**
 * A domain object.
 * 
 * @author Peter
 * 
 */
public interface DomainObject {

	public static final List<SearchableObjectField> NO_SEARCH_FIELDS = Collections
			.unmodifiableList(new ArrayList<SearchableObjectField>());

	public String getId();

	public DomainObjectType getType();

	/**
	 * Merge another DomainObject of the same type and declare in the result which fields' values have changed.
	 * 
	 * @param other
	 * @return list of fields who's values have changed in the merge.
	 */
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other);

	public List<FieldError> validate();

	public void check() throws FieldValidationException;

	public void updateSearchFields(ObjectRegistry registry);

	public List<SearchableObjectField> getSearchFields();
}
