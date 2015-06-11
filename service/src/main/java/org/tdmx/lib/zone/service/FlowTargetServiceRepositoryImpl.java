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
import org.tdmx.lib.zone.dao.FlowTargetDao;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetConcurrency;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Transactional CRUD Services for FlowTarget Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class FlowTargetServiceRepositoryImpl implements FlowTargetService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(FlowTargetServiceRepositoryImpl.class);

	private FlowTargetDao flowTargetDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(FlowTarget target) {
		if (target.getId() != null) {
			FlowTarget storedTarget = getFlowTargetDao().loadById(target.getId());
			if (storedTarget != null) {
				getFlowTargetDao().merge(target);
			} else {
				log.warn("Unable to find FlowTarget with id " + target.getId());
			}
		} else {
			getFlowTargetDao().persist(target);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public ModifyOperationStatus modifyConcurrency(AgentCredential agent, Service service, int concurrencyLimit) {
		FlowTarget existingFlowTarget = findByTargetService(agent, service);
		if (existingFlowTarget == null) {
			return ModifyOperationStatus.FLOWTARGET_NOT_FOUND;
		}
		FlowTargetConcurrency ftc = getFlowTargetDao().lock(existingFlowTarget.getConcurrency().getId());
		ftc.setConcurrencyLimit(concurrencyLimit);
		return ModifyOperationStatus.SUCCESS;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void setSession(FlowTarget ft) {
		FlowTarget flowTarget = findByTargetService(ft.getTarget(), ft.getService());
		if (flowTarget == null) {
			createOrUpdate(ft);
			flowTarget = ft;
		} else {
			flowTarget.setPrimary(ft.getPrimary());
			flowTarget.setSecondary(ft.getSecondary());
			flowTarget.setSignatureAlgorithm(ft.getSignatureAlgorithm());
			flowTarget.setSignatureDate(ft.getSignatureDate());
			flowTarget.setSignatureValue(ft.getSignatureValue());
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(FlowTarget target) {
		FlowTarget storedTarget = getFlowTargetDao().loadById(target.getId());
		if (storedTarget != null) {
			getFlowTargetDao().delete(storedTarget);
		} else {
			log.warn("Unable to find FlowTarget to delete with id " + target.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<FlowTarget> search(Zone zone, FlowTargetSearchCriteria criteria) {
		return getFlowTargetDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public FlowTarget findByTargetService(AgentCredential agent, Service service) {
		return getFlowTargetDao().loadByTargetService(agent, service);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public FlowTarget findById(Long id) {
		return getFlowTargetDao().loadById(id);
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

	public FlowTargetDao getFlowTargetDao() {
		return flowTargetDao;
	}

	public void setFlowTargetDao(FlowTargetDao flowTargetDao) {
		this.flowTargetDao = flowTargetDao;
	}

}
