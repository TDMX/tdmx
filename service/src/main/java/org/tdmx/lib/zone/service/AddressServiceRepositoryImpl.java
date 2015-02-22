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

package org.tdmx.lib.zone.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.dao.AddressDao;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;

/**
 * Transactional CRUD Services for Address Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class AddressServiceRepositoryImpl implements AddressService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AddressServiceRepositoryImpl.class);

	private AddressDao addressDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(Address address) {
		Address storedAddress = getAddressDao().loadById(address.getId());
		if (storedAddress == null) {
			getAddressDao().persist(address);
		} else {
			getAddressDao().merge(address);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Address address) {
		Address storedAddress = getAddressDao().loadById(address.getId());
		if (storedAddress != null) {
			getAddressDao().delete(storedAddress);
		} else {
			log.warn("Unable to find Address to delete with addressName " + address.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<Address> search(ZoneReference zone, AddressSearchCriteria criteria) {
		return getAddressDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Address findByName(ZoneReference zone, String domainName, String localName) {
		AddressSearchCriteria sc = new AddressSearchCriteria(new PageSpecifier(0, 1));
		sc.setDomainName(domainName);
		sc.setLocalName(localName);
		List<Address> addresses = getAddressDao().search(zone, sc);

		return addresses.isEmpty() ? null : addresses.get(0);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Address findById(Long id) {
		return getAddressDao().loadById(id);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AddressDao getAddressDao() {
		return addressDao;
	}

	public void setAddressDao(AddressDao addressDao) {
		this.addressDao = addressDao;
	}

}
