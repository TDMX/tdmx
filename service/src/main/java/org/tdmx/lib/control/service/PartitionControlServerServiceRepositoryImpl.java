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

package org.tdmx.lib.control.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.PartitionControlServerDao;
import org.tdmx.lib.control.domain.PartitionControlServer;

/**
 * A transactional service managing the PartitionControlServer information.
 * 
 * @author Peter Klauser
 * 
 */
public class PartitionControlServerServiceRepositoryImpl implements PartitionControlServerService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(PartitionControlServerServiceRepositoryImpl.class);

	private PartitionControlServerDao partitionControlServerDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(PartitionControlServer pcsServer) {
		if (pcsServer.getId() != null) {
			PartitionControlServer storedPcsServer = getPartitionControlServerDao().loadById(pcsServer.getId());
			if (storedPcsServer != null) {
				getPartitionControlServerDao().merge(pcsServer);
			} else {
				log.warn("Unable to find PartitionControlServer with id " + pcsServer.getId());
			}
		} else {
			getPartitionControlServerDao().persist(pcsServer);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(PartitionControlServer pcsServer) {
		PartitionControlServer storedPcsServer = getPartitionControlServerDao().loadById(pcsServer.getId());
		if (storedPcsServer != null) {
			getPartitionControlServerDao().delete(storedPcsServer);
		} else {
			log.warn("Unable to find PartitionControlServer to delete with id " + pcsServer.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public PartitionControlServer findByIpEndpoint(String ipAddress, int port) {
		return getPartitionControlServerDao().loadByIpEndpoint(ipAddress, port);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<PartitionControlServer> findBySegment(String segment) {
		return getPartitionControlServerDao().loadBySegment(segment);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<PartitionControlServer> findAll() {
		return getPartitionControlServerDao().loadAll();
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

	public PartitionControlServerDao getPartitionControlServerDao() {
		return partitionControlServerDao;
	}

	public void setPartitionControlServerDao(PartitionControlServerDao partitionControlServerDao) {
		this.partitionControlServerDao = partitionControlServerDao;
	}

}
