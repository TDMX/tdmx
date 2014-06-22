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
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.FieldValidationException;

/**
 * An abstract base class for DomainObjects.
 * 
 * @author Peter
 * 
 */
public abstract class AbstractDO implements DomainObject {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final AtomicLong ID = new AtomicLong(System.currentTimeMillis() * 1000);

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private String id;
	private List<SearchableObjectField> searchFields = NO_SEARCH_FIELDS;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public AbstractDO() {
		id = getNextObjectId();
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractDO other = (AbstractDO) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + id;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		return errors;
	}

	@Override
	public void check() throws FieldValidationException {
		List<FieldError> errors = validate();
		if (errors != null && !errors.isEmpty()) {
			throw new FieldValidationException(this, errors);
		}
	}

	@Override
	public List<SearchableObjectField> getSearchFields() {
		return searchFields;
	}

	public void setSearchFields(List<SearchableObjectField> searchFields) {
		this.searchFields = searchFields;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	/**
	 * Conditionally change the output if the other is different from the current.
	 * 
	 * @param current
	 * @param other
	 * @param changeList
	 * @return current if no changes, other if changes and logging the change in the list
	 */
	protected <E extends Object> E conditionalSet(E current, E other, DomainObjectField field,
			DomainObjectFieldChanges holder) {
		if (current == null) {
			if (other != null) {
				holder.field(field);
				return other;
			}
		} else if (current instanceof Object[] && other instanceof Object[]) {
			Object[] cList = (Object[]) current;
			Object[] oList = (Object[]) other;
			if (cList.length != oList.length) {
				holder.field(field);
				return other;
			}
			for (int i = 0; i < cList.length; i++) {
				if (!cList[i].equals(oList[i])) {
					holder.field(field);
					return other;
				}
			}
		} else if (other == null || !other.equals(current)) {
			holder.field(field);
			return other;
		}
		return current;
	}

	public static String getNextObjectId() {
		return "" + ID.getAndIncrement();
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
