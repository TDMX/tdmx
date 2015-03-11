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
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.dao.AgentCredentialDao;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;

/**
 * Transactional CRUD Services for AgentCredential Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class AgentCredentialServiceRepositoryImpl implements AgentCredentialService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AgentCredentialServiceRepositoryImpl.class);

	private AgentCredentialDao agentCredentialDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(AgentCredential agentCredential) {
		if (agentCredential.getId() != null) {
			AgentCredential storedAgentCredential = getAgentCredentialDao().loadById(agentCredential.getId());
			if (storedAgentCredential != null) {
				getAgentCredentialDao().merge(agentCredential);
			} else {
				log.warn("Unable to find AgentCredential with id " + agentCredential.getId());
			}
		} else {
			getAgentCredentialDao().persist(agentCredential);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(AgentCredential agentCredential) {
		AgentCredential storedAgentCredential = getAgentCredentialDao().loadById(agentCredential.getId());
		if (storedAgentCredential != null) {
			getAgentCredentialDao().delete(storedAgentCredential);
		} else {
			log.warn("Unable to find AgentCredential to delete with fingerprint " + agentCredential.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public AgentCredential findById(Long id) {
		return getAgentCredentialDao().loadById(id);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<AgentCredential> search(ZoneReference zone, AgentCredentialSearchCriteria criteria) {
		return getAgentCredentialDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public AgentCredential findByFingerprint(ZoneReference zone, String fingerprint) {
		if (!StringUtils.hasText(fingerprint)) {
			throw new IllegalArgumentException("missing fingerprint");
		}
		AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		sc.setFingerprint(fingerprint);
		List<AgentCredential> credentials = getAgentCredentialDao().search(zone, sc);

		return credentials.isEmpty() ? null : credentials.get(0);
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

	public AgentCredentialDao getAgentCredentialDao() {
		return agentCredentialDao;
	}

	public void setAgentCredentialDao(AgentCredentialDao agentCredentialDao) {
		this.agentCredentialDao = agentCredentialDao;
	}

}
