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
package org.tdmx.server.ws.scs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.scs.Endpoint;
import org.tdmx.core.api.v01.scs.GetMDSSession;
import org.tdmx.core.api.v01.scs.GetMDSSessionResponse;
import org.tdmx.core.api.v01.scs.GetMOSSession;
import org.tdmx.core.api.v01.scs.GetMOSSessionResponse;
import org.tdmx.core.api.v01.scs.GetMRSSession;
import org.tdmx.core.api.v01.scs.GetMRSSessionResponse;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.Session;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.DomainZoneApexInfo;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.runtime.DomainZoneResolutionService;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.scs.SessionDataService;
import org.tdmx.server.session.ServerSessionAllocationService;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientLookupService;
import org.tdmx.server.ws.session.WebServiceApiName;

public class SCSImpl implements SCS, Manageable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// TODO #86 zone data cache based on EhCache
	// (zoneApex->)AccountZone,(zoneApex+partitionId->)Zone,(zoneapex+fingerprint->)AgentCredential (ehCache)

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SCSImpl.class);

	private AuthenticatedClientLookupService authenticatedClientService;

	private DomainZoneResolutionService domainZoneResolutionService;

	private ServerSessionAllocationService sessionAllocationService;

	private SessionDataService sessionDataService;

	private final ApiValidator validator = new ApiValidator();
	private final ApiToDomainMapper a2d = new ApiToDomainMapper();

	// internal
	private Segment segment;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
		// the SCS needs to know which is it's own segment for relay authorization.
		this.segment = segment;
	}

	@Override
	public void stop() {
		this.segment = null;
	}

	@Override
	public GetMRSSessionResponse getMRSSession(GetMRSSession parameters) {
		GetMRSSessionResponse response = new GetMRSSessionResponse();
		PKIXCertificate sp = checkNonTDMXClientAuthenticated(response);
		if (sp == null) {
			return response;
		}

		// service not yet started :(
		if (segment == null) {
			ErrorCode.setError(ErrorCode.MissingSegment, response);
			return response;
		}

		String serviceProviderName = sp.getCommonName();

		if (validator.checkChannel(parameters.getChannel(), response) == null) {
			return response;
		}
		ChannelOrigin co = a2d.mapChannelOrigin(parameters.getChannel().getOrigin());
		ChannelDestination cd = a2d.mapChannelDestination(parameters.getChannel().getDestination());

		DomainZoneApexInfo destZoneApexInfo = domainZoneResolutionService.resolveDomain(cd.getDomainName());
		if (destZoneApexInfo == null) {
			ErrorCode.setError(ErrorCode.DnsZoneApexMissing, response, cd.getDomainName());
			return response;
		}
		DomainZoneApexInfo originZoneApexInfo = domainZoneResolutionService.resolveDomain(co.getDomainName());
		if (originZoneApexInfo == null) {
			ErrorCode.setError(ErrorCode.DnsZoneApexMissing, response, co.getDomainName());
			return response;
		}

		String zoneApex = null;
		String domainName = null;
		String localName = null;
		String serviceName = null;
		if (segment.getScsUrl().equals(originZoneApexInfo.getScsUrl().toExternalForm())
				&& segment.getScsUrl().equals(destZoneApexInfo.getScsUrl().toExternalForm())) {
			ErrorCode.setError(ErrorCode.RelayNotAllowedOnSameSCS, response);
			return response;

		} else if (segment.getScsUrl().equals(originZoneApexInfo.getScsUrl().toExternalForm())) {
			// if the origin's DNS information points to our own scsHostname, then the client certificate's name must
			// match the destination domain's scsHostname
			if (!serviceProviderName.equals(destZoneApexInfo.getScsUrl().getHost())) {
				ErrorCode.setError(ErrorCode.NonDnsAuthorizedPKIXAccess, response);
				return response;
			}
			zoneApex = originZoneApexInfo.getZoneApex();
			domainName = originZoneApexInfo.getDomainName();
			localName = co.getLocalName();
		} else if (segment.getScsUrl().equals(destZoneApexInfo.getScsUrl().toExternalForm())) {
			if (!serviceProviderName.equals(originZoneApexInfo.getScsUrl().getHost())) {
				ErrorCode.setError(ErrorCode.NonDnsAuthorizedPKIXAccess, response);
				return response;
			}
			zoneApex = destZoneApexInfo.getZoneApex();
			domainName = destZoneApexInfo.getDomainName();
			localName = cd.getLocalName();
			serviceName = cd.getServiceName();
		} else {
			ErrorCode.setError(ErrorCode.NonDnsAuthorizedPKIXAccess, response);
			return response;
		}
		AccountZone az = sessionDataService.getAccountZone(zoneApex);
		if (az == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}

		Zone zone = sessionDataService.getZone(az);
		if (zone == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}

		Domain domain = sessionDataService.getDomain(az, zone, domainName);
		if (domain == null) {
			ErrorCode.setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		ChannelAuthorization existingChannelAuth = sessionDataService.findChannelAuthorization(az, zone, domain, co,
				cd);

		WebServiceSessionEndpoint ep = null;
		if (existingChannelAuth != null) {
			ep = sessionAllocationService.associateMRSSession(az, zone, sp, existingChannelAuth.getChannel());
		} else {
			TemporaryChannel tempChannel = sessionDataService.findTemporaryChannel(az, zone, domain, co, cd);
			if (tempChannel == null) {
				tempChannel = sessionDataService.createTemporaryChannel(az, domain, co, cd);
			}
			ep = sessionAllocationService.associateMRSSession(az, zone, sp, tempChannel);
		}

		if (ep == null) {
			ErrorCode.setError(ErrorCode.NoSessionCapacity, response);
			return response;
		}

		Session session = new Session();
		session.setSessionId(ep.getSessionId());
		session.setZoneapex(zone.getZoneApex());
		session.setLocalname(localName);
		session.setDomain(domainName);
		session.setServicename(serviceName);
		session.setServiceprovider(segment.getScsUrl());
		response.setSession(session);

		Endpoint endpoint = new Endpoint();
		endpoint.setTlsCertificate(ep.getPublicCertificate().getX509Encoded());
		endpoint.setUrl(ep.getHttpsUrl());
		response.setEndpoint(endpoint);

		response.setSuccess(true);
		return response;
	}

	@Override
	public GetMDSSessionResponse getMDSSession(GetMDSSession parameters) {
		GetMDSSessionResponse response = new GetMDSSessionResponse();
		PKIXCertificate user = checkUserAuthorized(response);
		if (user == null) {
			return response;
		}

		String serviceName = parameters.getServicename();
		if (!StringUtils.hasText(serviceName)) {
			ErrorCode.setError(ErrorCode.MissingServiceName, response);
			return response;
		}

		String zoneApex = user.getTdmxZoneInfo().getZoneRoot();

		AccountZone az = sessionDataService.getAccountZone(zoneApex);
		if (az == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}
		// TODO #86 check the account zone is active.

		Zone zone = sessionDataService.getZone(az);
		if (zone == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}

		AgentCredential existingCred = sessionDataService.getAgentCredential(az, user);
		if (existingCred == null) {
			ErrorCode.setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}
		if (checkCredential(existingCred, user, response) == null) {
			return response;
		}

		Service service = sessionDataService.getService(az, existingCred.getDomain(), serviceName);
		if (service == null) {
			ErrorCode.setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		WebServiceSessionEndpoint ep = sessionAllocationService.associateMDSSession(az, zone, existingCred, service);
		if (ep == null) {
			ErrorCode.setError(ErrorCode.NoSessionCapacity, response);
			return response;
		}

		Session session = mapSession(existingCred, ep.getSessionId(), service.getServiceName(), segment.getScsUrl());
		response.setSession(session);

		Endpoint endpoint = new Endpoint();
		endpoint.setTlsCertificate(ep.getPublicCertificate().getX509Encoded());
		endpoint.setUrl(ep.getHttpsUrl());
		response.setEndpoint(endpoint);

		response.setSuccess(true);
		return response;
	}

	@Override
	public GetMOSSessionResponse getMOSSession(GetMOSSession parameters) {
		GetMOSSessionResponse response = new GetMOSSessionResponse();
		PKIXCertificate user = checkUserAuthorized(response);
		if (user == null) {
			return response;
		}

		String zoneApex = user.getTdmxZoneInfo().getZoneRoot();

		AccountZone az = sessionDataService.getAccountZone(zoneApex);
		if (az == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}
		// TODO #86 check the account zone is active.

		Zone zone = sessionDataService.getZone(az);
		if (zone == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}

		AgentCredential existingCred = sessionDataService.getAgentCredential(az, user);
		if (existingCred == null) {
			ErrorCode.setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}
		if (checkCredential(existingCred, user, response) == null) {
			return response;
		}

		WebServiceSessionEndpoint ep = sessionAllocationService.associateMOSSession(az, zone, existingCred);
		if (ep == null) {
			ErrorCode.setError(ErrorCode.NoSessionCapacity, response);
			return response;
		}

		Session session = mapSession(existingCred, ep.getSessionId(), null, segment.getScsUrl());
		response.setSession(session);

		Endpoint endpoint = new Endpoint();
		endpoint.setTlsCertificate(ep.getPublicCertificate().getX509Encoded());
		endpoint.setUrl(ep.getHttpsUrl());
		response.setEndpoint(endpoint);

		response.setSuccess(true);
		return response;
	}

	@Override
	public GetZASSessionResponse getZASSession(GetZASSession parameters) {
		GetZASSessionResponse response = new GetZASSessionResponse();
		PKIXCertificate admin = checkZACorDACAuthorized(response);
		if (admin == null) {
			return response;
		}
		String zoneApex = admin.getTdmxZoneInfo().getZoneRoot();

		AccountZone az = sessionDataService.getAccountZone(zoneApex);
		if (az == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}
		// TODO #86 check the account zone is active.

		Zone zone = sessionDataService.getZone(az);
		if (zone == null) {
			ErrorCode.setError(ErrorCode.ZoneNotFound, response);
			return response;
		}

		AgentCredential existingCred = sessionDataService.getAgentCredential(az, admin);
		if (existingCred == null) {
			ErrorCode.setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}
		if (checkCredential(existingCred, admin, response) == null) {
			return response;
		}

		WebServiceSessionEndpoint ep = sessionAllocationService.associateZASSession(az, zone, existingCred);
		if (ep == null) {
			ErrorCode.setError(ErrorCode.NoSessionCapacity, response);
			return response;
		}

		Session session = mapSession(existingCred, ep.getSessionId(), null, segment.getScsUrl());
		response.setSession(session);

		Endpoint endpoint = new Endpoint();
		endpoint.setTlsCertificate(ep.getPublicCertificate().getX509Encoded());
		endpoint.setUrl(ep.getHttpsUrl());
		response.setEndpoint(endpoint);

		response.setSuccess(true);
		return response;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private Session mapSession(AgentCredential agent, String sessionId, String service, String serviceProviderUrl) {
		Session session = new Session();
		session.setSessionId(sessionId);
		session.setZoneapex(agent.getZone().getZoneApex());
		if (agent.getDomain() != null) {
			session.setDomain(agent.getDomain().getDomainName());
		}
		if (agent.getAddress() != null) {
			session.setLocalname(agent.getAddress().getLocalName());
		}

		session.setServicename(service);
		session.setServiceprovider(serviceProviderUrl);
		return session;
	}

	private AgentCredential checkCredential(AgentCredential existingUser, PKIXCertificate cert, Acknowledge ack) {
		if (AgentCredentialStatus.ACTIVE != existingUser.getCredentialStatus()) {
			ErrorCode.setError(ErrorCode.SuspendedAccess, ack);
			return null;
		}
		// paranoia checks - in case the fingerprint matches some other cert by mistake
		if (!existingUser.getZone().getZoneApex().equals(cert.getTdmxZoneInfo().getZoneRoot())) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, ack);
			return null;
		}
		// paranoia checks - in case the fingerprint matches some other cert by mistake
		if (existingUser.getDomain() != null
				&& !existingUser.getDomain().getDomainName().equals(cert.getTdmxDomainName())) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, ack);
			return null;
		}
		// paranoia checks - in case the fingerprint matches some other cert by mistake
		if (existingUser.getAddress() != null
				&& !existingUser.getAddress().getLocalName().equals(cert.getTdmxUserName())) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, ack);
			return null;
		}
		return existingUser;
	}

	private PKIXCertificate checkUserAuthorized(Acknowledge ack) {
		PKIXCertificate user = authenticatedClientService.getAuthenticatedClient();
		if (user == null) {
			ErrorCode.setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (!user.isTdmxUserCertificate()) {
			ErrorCode.setError(ErrorCode.NonUserAccess, ack);
			return null;
		}
		return user;
	}

	private PKIXCertificate checkZACorDACAuthorized(Acknowledge ack) {
		PKIXCertificate user = authenticatedClientService.getAuthenticatedClient();
		if (user == null) {
			ErrorCode.setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (!user.isTdmxZoneAdminCertificate() && !user.isTdmxDomainAdminCertificate()) {
			ErrorCode.setError(ErrorCode.NonAdministratorAccess, ack);
			return null;
		}
		return user;
	}

	private PKIXCertificate checkNonTDMXClientAuthenticated(Acknowledge ack) {
		PKIXCertificate user = authenticatedClientService.getAuthenticatedClient();
		if (user == null) {
			ErrorCode.setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (user.isTdmxUserCertificate() || user.isTdmxDomainAdminCertificate() || user.isTdmxZoneAdminCertificate()) {
			ErrorCode.setError(ErrorCode.NonPKIXAccess, ack);
			return null;
		}
		return user;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AuthenticatedClientLookupService getAuthenticatedClientService() {
		return authenticatedClientService;
	}

	public void setAuthenticatedClientService(AuthenticatedClientLookupService authenticatedClientService) {
		this.authenticatedClientService = authenticatedClientService;
	}

	public DomainZoneResolutionService getDnsZoneResolutionService() {
		return domainZoneResolutionService;
	}

	public void setDnsZoneResolutionService(DomainZoneResolutionService domainZoneResolutionService) {
		this.domainZoneResolutionService = domainZoneResolutionService;
	}

	public ServerSessionAllocationService getSessionAllocationService() {
		return sessionAllocationService;
	}

	public void setSessionAllocationService(ServerSessionAllocationService sessionAllocationService) {
		this.sessionAllocationService = sessionAllocationService;
	}

	public SessionDataService getSessionDataService() {
		return sessionDataService;
	}

	public void setSessionDataService(SessionDataService sessionDataService) {
		this.sessionDataService = sessionDataService;
	}

	public DomainZoneResolutionService getDomainZoneResolutionService() {
		return domainZoneResolutionService;
	}

	public void setDomainZoneResolutionService(DomainZoneResolutionService domainZoneResolutionService) {
		this.domainZoneResolutionService = domainZoneResolutionService;
	}

}
