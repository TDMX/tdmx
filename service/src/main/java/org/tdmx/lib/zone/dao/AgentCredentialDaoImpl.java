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
package org.tdmx.lib.zone.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialID;
import org.tdmx.lib.zone.domain.AgentCredentialType;

public class AgentCredentialDaoImpl implements AgentCredentialDao {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@PersistenceContext(unitName = "ZoneDB")
	private EntityManager em;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void persist(AgentCredential value) {
		em.persist(value);
	}

	@Override
	public void delete(AgentCredential value) {
		em.remove(value);
	}

	@Override
	public void lock(AgentCredential value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public AgentCredential merge(AgentCredential value) {
		return em.merge(value);
	}

	@Override
	public AgentCredential loadById(AgentCredentialID id) {
		Query query = em.createQuery("from AgentCredential as ac where ac.id = :id");
		query.setParameter("id", id);
		try {
			return (AgentCredential) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AgentCredential> loadByZoneApex(String zoneApex) {
		Query query = em.createQuery("from AgentCredential as ac where ac.id.zoneApex = :zoneApex");
		query.setParameter("zoneApex", zoneApex);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AgentCredential> loadByZoneDomain(String zoneApex, String domainName) {
		Query query = em
				.createQuery("from AgentCredential as ac where ac.id.zoneApex = :zoneApex and ac.domainName = :domainName");
		query.setParameter("zoneApex", zoneApex);
		query.setParameter("domainName", domainName);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AgentCredential> loadByZoneDomainAndType(String zoneApex, String domainName, AgentCredentialType type) {
		Query query = em
				.createQuery("from AgentCredential as ac where ac.id.zoneApex = :zoneApex and ac.credentialType = :type and ac.domainName = :domainName");
		query.setParameter("zoneApex", zoneApex);
		query.setParameter("domainName", domainName);
		query.setParameter("type", type);
		return query.getResultList();
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

}