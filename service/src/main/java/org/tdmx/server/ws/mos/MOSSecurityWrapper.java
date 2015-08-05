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
package org.tdmx.server.ws.mos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.mos.GetAddress;
import org.tdmx.core.api.v01.mos.GetAddressResponse;
import org.tdmx.core.api.v01.mos.GetChannel;
import org.tdmx.core.api.v01.mos.GetChannelResponse;
import org.tdmx.core.api.v01.mos.ListChannel;
import org.tdmx.core.api.v01.mos.ListChannelResponse;
import org.tdmx.core.api.v01.mos.Receipt;
import org.tdmx.core.api.v01.mos.ReceiptResponse;
import org.tdmx.core.api.v01.mos.Submit;
import org.tdmx.core.api.v01.mos.SubmitResponse;
import org.tdmx.core.api.v01.mos.Upload;
import org.tdmx.core.api.v01.mos.UploadResponse;
import org.tdmx.core.api.v01.mos.ws.MOS;
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

public class MOSSecurityWrapper implements MOS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MOSSecurityWrapper.class);

	private ServerSecurityManager<MOSServerSession> securityManager;
	private AuthorizedSessionService<MOSServerSession> authorizationService;
	private ThreadLocalPartitionIdProvider partitionIdService;

	private MOS delegate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public CommitResponse commit(Commit parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
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
	public RollbackResponse rollback(Rollback parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
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
	public ForgetResponse forget(Forget parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
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
	public PrepareResponse prepare(Prepare parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
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
	public RecoverResponse recover(Recover parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
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

	@Override
	public GetAddressResponse getAddress(GetAddress parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.getAddress(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SubmitResponse submit(Submit parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.submit(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public UploadResponse upload(Upload parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.upload(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ReceiptResponse receipt(Receipt parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.receipt(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public GetChannelResponse getChannel(GetChannel parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.getChannel(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ListChannelResponse listChannel(ListChannel parameters) {
		MOSServerSession session = securityManager.getSession(parameters.getSessionId());
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

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------
	public ServerSecurityManager<MOSServerSession> getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(ServerSecurityManager<MOSServerSession> securityManager) {
		this.securityManager = securityManager;
	}

	public AuthorizedSessionService<MOSServerSession> getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizedSessionService<MOSServerSession> authorizationService) {
		this.authorizationService = authorizationService;
	}

	public ThreadLocalPartitionIdProvider getPartitionIdService() {
		return partitionIdService;
	}

	public void setPartitionIdService(ThreadLocalPartitionIdProvider partitionIdService) {
		this.partitionIdService = partitionIdService;
	}

	public MOS getDelegate() {
		return delegate;
	}

	public void setDelegate(MOS delegate) {
		this.delegate = delegate;
	}

}
