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
package org.tdmx.lib.control.dao;

import java.util.List;

import javax.persistence.LockModeType;

import org.tdmx.lib.control.domain.ControlJobEntry;
import org.tdmx.lib.control.domain.ControlJobEntrySearchCriteria;

/**
 * DAO for JobEntry entity.
 * 
 * @author Peter
 * 
 */
public interface ControlJobEntryDao {

	public void persist(ControlJobEntry value);

	public void delete(ControlJobEntry value);

	public void lock(ControlJobEntry value);

	public ControlJobEntry merge(ControlJobEntry value);

	public ControlJobEntry loadById(String id);

	public List<ControlJobEntry> fetch(ControlJobEntrySearchCriteria criteria, LockModeType lockMode);
}
