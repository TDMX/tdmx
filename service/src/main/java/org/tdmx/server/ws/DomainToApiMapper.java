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
package org.tdmx.server.ws;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.v01.common.Processingstatus;
import org.tdmx.core.api.v01.msg.Address;
import org.tdmx.core.api.v01.msg.Administrator;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Channelauthorization;
import org.tdmx.core.api.v01.msg.CredentialStatus;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.EndpointPermission;
import org.tdmx.core.api.v01.msg.FlowControlLimit;
import org.tdmx.core.api.v01.msg.Flowsession;
import org.tdmx.core.api.v01.msg.Flowtarget;
import org.tdmx.core.api.v01.msg.Flowtargetsession;
import org.tdmx.core.api.v01.msg.IpAddressList;
import org.tdmx.core.api.v01.msg.Limit;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.RequestedChannelAuthorization;
import org.tdmx.core.api.v01.msg.Service;
import org.tdmx.core.api.v01.msg.Servicestate;
import org.tdmx.core.api.v01.msg.SignatureAlgorithm;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.api.v01.msg.User;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.FlowSession;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSession;

public class DomainToApiMapper {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DomainToApiMapper.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public Flowtarget mapFlowTarget(FlowTarget ft) {
		if (ft == null) {
			return null;
		}
		Flowtarget f = new Flowtarget();

		f.setTarget(mapUserIdentity(ft.getTarget()));
		f.setConcurrencyLevel(ft.getConcurrency().getConcurrencyLevel());
		f.setConcurrencyLimit(ft.getConcurrency().getConcurrencyLimit());
		f.setFlowtargetsession(mapFlowTargetSession(ft.getFts()));
		f.setServicename(ft.getService().getServiceName());

		return f;
	}

	public Flowtargetsession mapFlowTargetSession(FlowTargetSession fts) {
		if (fts == null) {
			return null;
		}
		Flowtargetsession f = new Flowtargetsession();
		if (fts.getPrimary() != null) {
			f.getFlowsessions().add(mapFlowSession(fts.getPrimary()));
		}
		if (fts.getSecondary() != null) {
			f.getFlowsessions().add(mapFlowSession(fts.getSecondary()));
		}
		f.setSignaturevalue(mapSignature(fts.getSignature()));
		return f;
	}

	public Flowsession mapFlowSession(FlowSession fs) {
		if (fs == null) {
			return null;
		}
		Flowsession s = new Flowsession();
		s.setScheme(fs.getScheme());
		s.setSessionKey(fs.getSessionKey());
		s.setValidFrom(mapTimestamp(fs.getValidFrom()));
		return s;
	}

	public User mapUser(AgentCredential cred) {
		if (cred == null) {
			return null;
		}
		User us = new User();
		us.setUserIdentity(mapUserIdentity(cred));
		us.setStatus(CredentialStatus.fromValue(cred.getCredentialStatus().name()));
		us.setWhitelist(new IpAddressList()); // TODO ipwhitelist
		return us;
	}

	public UserIdentity mapUserIdentity(AgentCredential cred) {
		if (cred == null) {
			return null;
		}
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(PKIXCertificate.getPublicKey(cred.getCertificateChain()).getX509Encoded());
		u.setDomaincertificate(PKIXCertificate.getIssuerPublicKey(cred.getCertificateChain()).getX509Encoded());
		u.setRootcertificate(PKIXCertificate.getZoneRootPublicKey(cred.getCertificateChain()).getX509Encoded());

		return u;
	}

	public Administrator mapAdministrator(AgentCredential cred) {
		if (cred == null) {
			return null;
		}
		Administrator us = new Administrator();
		us.setStatus(CredentialStatus.fromValue(cred.getCredentialStatus().name()));
		us.setAdministratorIdentity(mapAdministratorIdentity(cred.getCertificateChain()));
		us.setWhitelist(new IpAddressList()); // TODO ipwhitelist
		return us;
	}

	public Address mapAddress(org.tdmx.lib.zone.domain.Address address) {
		if (address == null) {
			return null;
		}
		Address a = new Address();
		a.setDomain(address.getDomain().getDomainName());
		a.setLocalname(address.getLocalName());
		return a;
	}

	public Servicestate mapService(org.tdmx.lib.zone.domain.Service service) {
		if (service == null) {
			return null;
		}
		Service s = new Service();
		s.setDomain(service.getDomain().getDomainName());
		s.setServicename(service.getServiceName());

		Servicestate ss = new Servicestate();
		ss.setService(s);
		ss.setConcurrencyLimit(service.getConcurrencyLimit());
		return ss;
	}

	public Channelauthorization mapChannelAuthorization(org.tdmx.lib.zone.domain.ChannelAuthorization ca) {
		if (ca == null) {
			return null;
		}
		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(ca.getOrigin().getDomainName());
		origin.setLocalname(ca.getOrigin().getLocalName());
		origin.setServiceprovider(ca.getOrigin().getServiceProvider());

		Destination dest = new Destination();
		dest.setDomain(ca.getDestination().getDomainName());
		dest.setLocalname(ca.getDestination().getLocalName());
		dest.setServicename(ca.getDestination().getServiceName());
		dest.setServiceprovider(ca.getDestination().getServiceProvider());

		Channel channel = new Channel();
		channel.setDestination(dest);
		channel.setOrigin(origin);

		FlowControlLimit limit = new FlowControlLimit();
		limit.setUnsentBuffer(mapLimit(ca.getUnsentBuffer()));
		limit.setUndeliveredBuffer(mapLimit(ca.getUndeliveredBuffer()));

		Currentchannelauthorization current = new Currentchannelauthorization();
		current.setChannel(channel);
		current.setOrigin(mapPermission(ca.getSendAuthorization()));
		current.setDestination(mapPermission(ca.getRecvAuthorization()));
		current.setLimit(limit);
		current.setAdministratorsignature(mapAdministratorSignature(ca.getSignature()));

		RequestedChannelAuthorization unconfirmed = new RequestedChannelAuthorization();
		unconfirmed.setOrigin(mapPermission(ca.getReqSendAuthorization()));
		unconfirmed.setDestination(mapPermission(ca.getReqRecvAuthorization()));

		Processingstatus processingstatus = new Processingstatus();
		// TODO

		Channelauthorization c = new Channelauthorization();
		c.setDomain(ca.getDomain().getDomainName());
		c.setCurrent(current);
		c.setUnconfirmed(unconfirmed);
		c.setProcessingstatus(processingstatus);
		return c;
	}

	public Administratorsignature mapAdministratorSignature(org.tdmx.lib.zone.domain.AgentSignature agentSignature) {
		if (agentSignature == null) {
			return null;
		}
		Administratorsignature s = new Administratorsignature();
		s.setAdministratorIdentity(mapAdministratorIdentity(agentSignature.getCertificateChain()));
		s.setSignaturevalue(mapSignature(agentSignature));
		return s;
	}

	public Signaturevalue mapSignature(org.tdmx.lib.zone.domain.AgentSignature agentSignature) {
		if (agentSignature == null) {
			return null;
		}
		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(mapTimestamp(agentSignature.getSignatureDate()));
		sig.setSignature(agentSignature.getValue());
		sig.setSignatureAlgorithm(mapSignatureAlgorithm(agentSignature.getAlgorithm()));
		return sig;
	}

	public SignatureAlgorithm mapSignatureAlgorithm(org.tdmx.client.crypto.algorithm.SignatureAlgorithm sa) {
		if (sa == null) {
			return null;
		}
		return SignatureAlgorithm.fromValue(sa.getAlgorithm());
	}

	public Calendar mapTimestamp(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public AdministratorIdentity mapAdministratorIdentity(PKIXCertificate[] adminCertChain) {
		if (adminCertChain == null) {
			return null;
		}
		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(PKIXCertificate.getPublicKey(adminCertChain).getX509Encoded());
		u.setRootcertificate(PKIXCertificate.getZoneRootPublicKey(adminCertChain).getX509Encoded());
		return u;
	}

	public Limit mapLimit(org.tdmx.lib.zone.domain.FlowLimit limit) {
		if (limit == null) {
			return null;
		}
		Limit l = new Limit();
		l.setHighBytes(limit.getHighMarkBytes());
		l.setLowBytes(limit.getLowMarkBytes());
		return l;
	}

	public EndpointPermission mapPermission(org.tdmx.lib.zone.domain.EndpointPermission ep) {
		if (ep == null) {
			return null;
		}
		EndpointPermission p = new EndpointPermission();
		p.setAdministratorsignature(mapAdministratorSignature(ep.getSignature()));
		p.setMaxPlaintextSizeBytes(ep.getMaxPlaintextSizeBytes());
		p.setPermission(Permission.valueOf(ep.getGrant().toString()));
		p.setValidUntil(mapTimestamp(ep.getValidUntil()));
		return p;
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
