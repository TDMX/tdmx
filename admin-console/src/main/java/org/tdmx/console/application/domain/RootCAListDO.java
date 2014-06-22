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

import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;
import org.tdmx.console.application.util.ValueObjectUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;

/**
 * A RootCAList is consists of a list of Trusted or Distrusted TrustStoreEntries.
 * 
 * @author Peter
 * 
 */
public class RootCAListDO extends AbstractDO {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final DomainObjectField F_ACTIVE = new DomainObjectField("active", DomainObjectType.RootCAList);
	public static final DomainObjectField F_NAME = new DomainObjectField("name", DomainObjectType.RootCAList);
	public static final DomainObjectField F_TYPE = new DomainObjectField("type", DomainObjectType.RootCAList);
	public static final DomainObjectField F_ENTRIES = new DomainObjectField("entries", DomainObjectType.RootCAList);

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private Boolean active = Boolean.TRUE;
	private String name;
	private RootCAListType listType;
	private List<TrustStoreEntryVO> entries;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public RootCAListDO() {
		super();
	}

	public RootCAListDO(RootCAListDO original) {
		setId(original.getId());
		setActive(original.isActive());
		setName(original.getName());
		setEntries(ValueObjectUtils.cloneList(original.getEntries()));
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DomainObjectType getType() {
		return DomainObjectType.RootCAList;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		RootCAListDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE, holder));
		setName(conditionalSet(getName(), o.getName(), F_NAME, holder));
		setEntries(conditionalSet(getEntries(), o.getEntries(), F_ENTRIES, holder));
		return holder;
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();

		ValidationUtils.mandatoryField(isActive(), F_ACTIVE.getName(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getName(), F_NAME.getName(), ERROR.MISSING, errors);
		return errors;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		// TODO
		setSearchFields(ctx.getSearchFields());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private <E extends DomainObject> RootCAListDO narrow(E other) {
		return (RootCAListDO) other;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public RootCAListType getListType() {
		return listType;
	}

	public void setListType(RootCAListType listType) {
		this.listType = listType;
	}

	public List<TrustStoreEntryVO> getEntries() {
		return entries;
	}

	public void setEntries(List<TrustStoreEntryVO> entries) {
		this.entries = entries;
	}

}
