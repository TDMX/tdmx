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

import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;

/**
 * A DnsResolverList is an ordered list of IP addresses which are used to resolve DNS lookups.
 * 
 * @author Peter
 * 
 */
public class DnsResolverListDO extends AbstractDO {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final DomainObjectField F_NAME = new DomainObjectField("name", DomainObjectType.DnsResolverList);
	public static final DomainObjectField F_ACTIVE = new DomainObjectField("active", DomainObjectType.DnsResolverList);
	public static final DomainObjectField F_HOSTNAMES = new DomainObjectField("hostnames",
			DomainObjectType.DnsResolverList);

	public static final class DnsResolverListSO {
		public static final FieldDescriptor NAME = new FieldDescriptor(DomainObjectType.DnsResolverList, "name",
				FieldType.String);
		public static final FieldDescriptor STATE = new FieldDescriptor(DomainObjectType.DnsResolverList, "state",
				FieldType.Token);
		public static final FieldDescriptor HOSTNAME = new FieldDescriptor(DomainObjectType.DnsResolverList,
				"hostname", FieldType.String);
	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private Boolean active = Boolean.TRUE;
	private String name;
	private List<String> hostnames;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public DnsResolverListDO() {
		super();
	}

	public DnsResolverListDO(DnsResolverListDO original) {
		setId(original.getId());
		setActive(original.isActive());
		setName(original.getName());
		setHostnames(original.getHostnames());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DomainObjectType getType() {
		return DomainObjectType.DnsResolverList;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		DnsResolverListDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE, holder));
		setName(conditionalSet(getName(), o.getName(), F_NAME, holder));
		setHostnames(conditionalSet(getHostnames(), o.getHostnames(), F_HOSTNAMES, holder));
		return holder;
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();

		ValidationUtils.mandatoryField(isActive(), F_ACTIVE.getName(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getName(), F_NAME.getName(), ERROR.MISSING, errors);
		if (getHostnames() != null) {
			for (String hostname : getHostnames()) {
				ValidationUtils.optionalHostnameField(hostname, F_HOSTNAMES.getName(), ERROR.INVALID, errors);
			}
		}
		return errors;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		ctx.sof(this, DnsResolverListSO.NAME, getName());
		ctx.sof(this, DnsResolverListSO.STATE, isActive() ? SearchableObjectField.TOKEN_ON
				: SearchableObjectField.TOKEN_OFF);
		if (getHostnames() != null) {
			for (String hostname : getHostnames()) {
				ctx.sof(this, DnsResolverListSO.HOSTNAME, hostname);
			}
		}
		setSearchFields(ctx.getSearchFields());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private <E extends DomainObject> DnsResolverListDO narrow(E other) {
		return (DnsResolverListDO) other;
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

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
