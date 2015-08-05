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
package org.tdmx.server.ws.zas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.report.Incident;
import org.tdmx.core.api.v01.report.IncidentResponse;
import org.tdmx.core.api.v01.report.Report;
import org.tdmx.core.api.v01.report.ReportResponse;
import org.tdmx.core.api.v01.zas.CreateAddress;
import org.tdmx.core.api.v01.zas.CreateAddressResponse;
import org.tdmx.core.api.v01.zas.CreateAdministrator;
import org.tdmx.core.api.v01.zas.CreateAdministratorResponse;
import org.tdmx.core.api.v01.zas.CreateDomain;
import org.tdmx.core.api.v01.zas.CreateDomainResponse;
import org.tdmx.core.api.v01.zas.CreateIpZone;
import org.tdmx.core.api.v01.zas.CreateIpZoneResponse;
import org.tdmx.core.api.v01.zas.CreateService;
import org.tdmx.core.api.v01.zas.CreateServiceResponse;
import org.tdmx.core.api.v01.zas.CreateUser;
import org.tdmx.core.api.v01.zas.CreateUserResponse;
import org.tdmx.core.api.v01.zas.DeleteAddress;
import org.tdmx.core.api.v01.zas.DeleteAddressResponse;
import org.tdmx.core.api.v01.zas.DeleteAdministrator;
import org.tdmx.core.api.v01.zas.DeleteAdministratorResponse;
import org.tdmx.core.api.v01.zas.DeleteChannelAuthorization;
import org.tdmx.core.api.v01.zas.DeleteChannelAuthorizationResponse;
import org.tdmx.core.api.v01.zas.DeleteDomain;
import org.tdmx.core.api.v01.zas.DeleteDomainResponse;
import org.tdmx.core.api.v01.zas.DeleteIpZone;
import org.tdmx.core.api.v01.zas.DeleteIpZoneResponse;
import org.tdmx.core.api.v01.zas.DeleteService;
import org.tdmx.core.api.v01.zas.DeleteServiceResponse;
import org.tdmx.core.api.v01.zas.DeleteUser;
import org.tdmx.core.api.v01.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.zas.DownloadChunk;
import org.tdmx.core.api.v01.zas.DownloadChunkResponse;
import org.tdmx.core.api.v01.zas.ListChannelMessage;
import org.tdmx.core.api.v01.zas.ListChannelMessageResponse;
import org.tdmx.core.api.v01.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.zas.ModifyAdministratorResponse;
import org.tdmx.core.api.v01.zas.ModifyIpZone;
import org.tdmx.core.api.v01.zas.ModifyIpZoneResponse;
import org.tdmx.core.api.v01.zas.ModifyUser;
import org.tdmx.core.api.v01.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.zas.ReceiptMessage;
import org.tdmx.core.api.v01.zas.ReceiptMessageResponse;
import org.tdmx.core.api.v01.zas.ReceiveMessage;
import org.tdmx.core.api.v01.zas.ReceiveMessageResponse;
import org.tdmx.core.api.v01.zas.SearchAddress;
import org.tdmx.core.api.v01.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.zas.SearchAdministrator;
import org.tdmx.core.api.v01.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.zas.SearchChannel;
import org.tdmx.core.api.v01.zas.SearchChannelResponse;
import org.tdmx.core.api.v01.zas.SearchDestination;
import org.tdmx.core.api.v01.zas.SearchDestinationResponse;
import org.tdmx.core.api.v01.zas.SearchDomain;
import org.tdmx.core.api.v01.zas.SearchDomainResponse;
import org.tdmx.core.api.v01.zas.SearchIpZone;
import org.tdmx.core.api.v01.zas.SearchIpZoneResponse;
import org.tdmx.core.api.v01.zas.SearchService;
import org.tdmx.core.api.v01.zas.SearchServiceResponse;
import org.tdmx.core.api.v01.zas.SearchUser;
import org.tdmx.core.api.v01.zas.SearchUserResponse;
import org.tdmx.core.api.v01.zas.SetChannelAuthorization;
import org.tdmx.core.api.v01.zas.SetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.zas.SubmitMessage;
import org.tdmx.core.api.v01.zas.SubmitMessageResponse;
import org.tdmx.core.api.v01.zas.UploadChunk;
import org.tdmx.core.api.v01.zas.UploadChunkResponse;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.server.ws.security.ServerSecurityManager;
import org.tdmx.server.ws.security.service.AuthorizedSessionService;

public class ZASSecurityWrapper implements ZAS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZASSecurityWrapper.class);

	private ServerSecurityManager<ZASServerSession> securityManager;
	private AuthorizedSessionService<ZASServerSession> authorizationService;
	private ThreadLocalPartitionIdProvider partitionIdService;

	private ZAS delegate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public SearchDomainResponse searchDomain(SearchDomain parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchDomain(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchUserResponse searchUser(SearchUser parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchUser(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public UploadChunkResponse uploadChunk(UploadChunk parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.uploadChunk(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CreateDomainResponse createDomain(CreateDomain parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.createDomain(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchAdministratorResponse searchAdministrator(SearchAdministrator parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchAdministrator(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchDestinationResponse searchDestination(SearchDestination parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchDestination(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ModifyUserResponse modifyUser(ModifyUser parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.modifyUser(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchIpZoneResponse searchIpZone(SearchIpZone parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchIpZone(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CreateAddressResponse createAddress(CreateAddress parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.createAddress(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteChannelAuthorizationResponse deleteChannelAuthorization(DeleteChannelAuthorization parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteChannelAuthorization(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public IncidentResponse incident(Incident parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.incident(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CreateIpZoneResponse createIpZone(CreateIpZone parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.createIpZone(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteIpZoneResponse deleteIpZone(DeleteIpZone parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteIpZone(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteDomainResponse deleteDomain(DeleteDomain parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteDomain(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ListChannelMessageResponse listChannelMessage(ListChannelMessage parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.listChannelMessage(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ReportResponse report(Report parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.report(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteUserResponse deleteUser(DeleteUser parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteUser(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ModifyIpZoneResponse modifyIpZone(ModifyIpZone parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.modifyIpZone(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SubmitMessageResponse submitMessage(SubmitMessage parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.submitMessage(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchAddressResponse searchAddress(SearchAddress parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchAddress(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CreateAdministratorResponse createAdministrator(CreateAdministrator parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.createAdministrator(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ReceiveMessageResponse receiveMessage(ReceiveMessage parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.receiveMessage(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteServiceResponse deleteService(DeleteService parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteService(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchChannelResponse searchChannel(SearchChannel parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchChannel(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ReceiptMessageResponse receiptMessage(ReceiptMessage parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.receiptMessage(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SearchServiceResponse searchService(SearchService parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.searchService(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteAddressResponse deleteAddress(DeleteAddress parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteAddress(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public ModifyAdministratorResponse modifyAdministrator(ModifyAdministrator parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.modifyAdministrator(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public SetChannelAuthorizationResponse setChannelAuthorization(SetChannelAuthorization parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.setChannelAuthorization(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CreateUserResponse createUser(CreateUser parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.createUser(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public CreateServiceResponse createService(CreateService parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.createService(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DownloadChunkResponse downloadChunk(DownloadChunk parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.downloadChunk(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}
	}

	@Override
	public DeleteAdministratorResponse deleteAdministrator(DeleteAdministrator parameters) {
		ZASServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.deleteAdministrator(parameters);

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

	public ServerSecurityManager<ZASServerSession> getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(ServerSecurityManager<ZASServerSession> securityManager) {
		this.securityManager = securityManager;
	}

	public AuthorizedSessionService<ZASServerSession> getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizedSessionService<ZASServerSession> authorizationService) {
		this.authorizationService = authorizationService;
	}

	public ThreadLocalPartitionIdProvider getPartitionIdService() {
		return partitionIdService;
	}

	public void setPartitionIdService(ThreadLocalPartitionIdProvider partitionIdService) {
		this.partitionIdService = partitionIdService;
	}

	public ZAS getDelegate() {
		return delegate;
	}

	public void setDelegate(ZAS delegate) {
		this.delegate = delegate;
	}

}
