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
package org.tdmx.server.ws.mds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.mds.Acknowledge;
import org.tdmx.core.api.v01.mds.AcknowledgeResponse;
import org.tdmx.core.api.v01.mds.Download;
import org.tdmx.core.api.v01.mds.DownloadResponse;
import org.tdmx.core.api.v01.mds.GetDestinationSession;
import org.tdmx.core.api.v01.mds.GetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ListChannel;
import org.tdmx.core.api.v01.mds.ListChannelResponse;
import org.tdmx.core.api.v01.mds.Receive;
import org.tdmx.core.api.v01.mds.ReceiveResponse;
import org.tdmx.core.api.v01.mds.SetDestinationSession;
import org.tdmx.core.api.v01.mds.SetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.tx.Commit;
import org.tdmx.core.api.v01.tx.CommitResponse;
import org.tdmx.core.api.v01.tx.Forget;
import org.tdmx.core.api.v01.tx.ForgetResponse;
import org.tdmx.core.api.v01.tx.Prepare;
import org.tdmx.core.api.v01.tx.PrepareResponse;
import org.tdmx.core.api.v01.tx.Recover;
import org.tdmx.core.api.v01.tx.RecoverResponse;
import org.tdmx.core.api.v01.tx.Rollback;
import org.tdmx.core.api.v01.tx.RollbackResponse;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.server.ws.security.ServerSecurityManager;
import org.tdmx.server.ws.security.service.AuthorizedSessionService;

public class MDSSecurityWrapper implements MDS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MDSSecurityWrapper.class);

	private ServerSecurityManager<MDSServerSession> securityManager;
	private AuthorizedSessionService<MDSServerSession> authorizationService;
	private ThreadLocalPartitionIdProvider partitionIdService;

	private MDS delegate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public GetDestinationSessionResponse getDestinationSession(GetDestinationSession parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.getDestinationSession(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SetDestinationSessionResponse setDestinationSession(SetDestinationSession parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.setDestinationSession(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DownloadResponse download(Download parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.download(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ReceiveResponse receive(Receive parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.receive(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public AcknowledgeResponse acknowledge(Acknowledge parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.acknowledge(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ListChannelResponse listChannel(ListChannel parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.listChannel(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.prepare(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.forget(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.rollback(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.commit(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		MDSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.recover(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
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

	public ServerSecurityManager<MDSServerSession> getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(ServerSecurityManager<MDSServerSession> securityManager) {
		this.securityManager = securityManager;
	}

	public AuthorizedSessionService<MDSServerSession> getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizedSessionService<MDSServerSession> authorizationService) {
		this.authorizationService = authorizationService;
	}

	public ThreadLocalPartitionIdProvider getPartitionIdService() {
		return partitionIdService;
	}

	public void setPartitionIdService(ThreadLocalPartitionIdProvider partitionIdService) {
		this.partitionIdService = partitionIdService;
	}

	public MDS getDelegate() {
		return delegate;
	}

	public void setDelegate(MDS delegate) {
		this.delegate = delegate;
	}

}
