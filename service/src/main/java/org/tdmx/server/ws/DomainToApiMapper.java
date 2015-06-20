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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.v01.common.Processingstatus;
import org.tdmx.core.api.v01.common.Taskstatus;
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
import org.tdmx.core.api.v01.msg.Flow;
import org.tdmx.core.api.v01.msg.FlowControlLevel;
import org.tdmx.core.api.v01.msg.FlowControlLimit;
import org.tdmx.core.api.v01.msg.Flowcontrolstatus;
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
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.ChannelFlowOrigin;
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

	public Flow mapFlow(ChannelFlowOrigin fo) {
		if (fo == null) {
			return null;
		}
		Flow f = new Flow();

		f.setFlowcontrolstatus(Flowcontrolstatus.OPEN); // TODO
		f.setLevel(new FlowControlLevel()); // TODO
		f.setLimit(new FlowControlLimit()); // TODO
		f.setFlowtargetsession(mapFlowTargetSession(fo.getFlowTarget().getFlowTargetSession()));

		f.setSource(mapUserIdentity(fo.getSourceCertificateChain()));
		f.setTarget(mapUserIdentity(fo.getFlowTarget().getTargetCertificateChain()));
		f.setServicename(fo.getFlowTarget().getChannel().getDestination().getServiceName());
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
		if (fts.getSignature() != null) {
			Signaturevalue sv = new Signaturevalue();
			sv.setSignature(fts.getSignature().getValue());
			sv.setTimestamp(CalendarUtils.getDateTime(fts.getSignature().getSignatureDate()));
			sv.setSignatureAlgorithm(mapSignatureAlgorithm(fts.getSignature().getAlgorithm()));

			f.setSignaturevalue(sv);
		}

		return f;
	}

	public Flowtarget mapFlowTarget(FlowTarget ft) {
		if (ft == null) {
			return null;
		}
		Flowtarget f = new Flowtarget();

		f.setTarget(mapUserIdentity(ft.getTarget().getCertificateChain()));
		f.setServicename(ft.getService().getServiceName());

		f.setConcurrencyLevel(ft.getConcurrency().getConcurrencyLevel());
		f.setConcurrencyLimit(ft.getConcurrency().getConcurrencyLimit());

		Signaturevalue sv = new Signaturevalue();
		sv.setSignature(ft.getSignatureValue());
		sv.setTimestamp(CalendarUtils.getDateTime(ft.getSignatureDate()));
		sv.setSignatureAlgorithm(mapSignatureAlgorithm(ft.getSignatureAlgorithm()));

		Flowtargetsession fts = new Flowtargetsession();
		if (ft.getPrimary() != null) {
			fts.getFlowsessions().add(mapFlowSession(ft.getPrimary()));
		}
		if (ft.getSecondary() != null) {
			fts.getFlowsessions().add(mapFlowSession(ft.getSecondary()));
		}
		fts.setSignaturevalue(sv);
		f.setFlowtargetsession(fts);

		return f;
	}

	public Flowsession mapFlowSession(FlowSession fs) {
		if (fs == null) {
			return null;
		}
		Flowsession s = new Flowsession();
		s.setFlowsessionId(fs.getIdentifier());
		s.setScheme(fs.getScheme());
		s.setSessionKey(fs.getSessionKey());
		s.setValidFrom(CalendarUtils.getDateTime(fs.getValidFrom()));
		return s;
	}

	public User mapUser(AgentCredential cred) {
		if (cred == null) {
			return null;
		}
		User us = new User();
		us.setUserIdentity(mapUserIdentity(cred.getCertificateChain()));
		us.setStatus(CredentialStatus.fromValue(cred.getCredentialStatus().name()));
		us.setWhitelist(new IpAddressList()); // TODO ipwhitelist
		return us;
	}

	public UserIdentity mapUserIdentity(PKIXCertificate[] userCertChain) {
		if (userCertChain == null || userCertChain.length != 3) {
			return null;
		}
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(PKIXCertificate.getPublicKey(userCertChain).getX509Encoded());
		u.setDomaincertificate(PKIXCertificate.getIssuerPublicKey(userCertChain).getX509Encoded());
		u.setRootcertificate(PKIXCertificate.getZoneRootPublicKey(userCertChain).getX509Encoded());

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

	public ChannelEndpoint mapOrigin(org.tdmx.lib.zone.domain.ChannelOrigin origin) {
		if (origin == null) {
			return null;
		}
		ChannelEndpoint o = new ChannelEndpoint();
		o.setDomain(origin.getDomainName());
		o.setLocalname(origin.getLocalName());
		o.setServiceprovider(origin.getServiceProvider());
		return o;
	}

	public Destination mapDestination(org.tdmx.lib.zone.domain.ChannelDestination dest) {
		if (dest == null) {
			return null;
		}
		Destination d = new Destination();
		d.setDomain(dest.getDomainName());
		d.setLocalname(dest.getLocalName());
		d.setServiceprovider(dest.getServiceProvider());
		d.setServicename(dest.getServiceName());
		return d;
	}

	public Channel mapChannel(org.tdmx.lib.zone.domain.Channel channel) {
		if (channel == null) {
			return null;
		}

		Channel c = new Channel();
		c.setDestination(mapDestination(channel.getDestination()));
		c.setOrigin(mapOrigin(channel.getOrigin()));
		return c;
	}

	public Channelauthorization mapChannelAuthorization(org.tdmx.lib.zone.domain.ChannelAuthorization ca) {
		if (ca == null) {
			return null;
		}

		Currentchannelauthorization current = new Currentchannelauthorization();
		current.setChannel(mapChannel(ca.getChannel()));
		current.setOrigin(mapPermission(ca.getSendAuthorization()));
		current.setDestination(mapPermission(ca.getRecvAuthorization()));

		FlowControlLimit limit = new FlowControlLimit();
		limit.setUnsentBuffer(mapLimit(ca.getUnsentBuffer()));
		limit.setUndeliveredBuffer(mapLimit(ca.getUndeliveredBuffer()));
		current.setLimit(limit);
		current.setAdministratorsignature(mapAdministratorSignature(ca.getSignature()));

		Channelauthorization c = new Channelauthorization();
		c.setDomain(ca.getChannel().getDomain().getDomainName());
		c.setCurrent(current);
		if (ca.getReqRecvAuthorization() != null || ca.getSendAuthorization() != null) {
			RequestedChannelAuthorization unconfirmed = new RequestedChannelAuthorization();
			unconfirmed.setOrigin(mapPermission(ca.getReqSendAuthorization()));
			unconfirmed.setDestination(mapPermission(ca.getReqRecvAuthorization()));
			c.setUnconfirmed(unconfirmed);
		}
		c.setProcessingstatus(mapProcessingStatus(ca.getProcessingState()));
		return c;
	}

	public Processingstatus mapProcessingStatus(org.tdmx.lib.common.domain.ProcessingState ps) {
		Processingstatus p = new Processingstatus();
		p.setStatus(Taskstatus.fromValue(ps.getStatus().toString()));
		p.setTimestamp(CalendarUtils.getDateTime(ps.getTimestamp()));
		p.setError(mapError(ps.getErrorCode(), ps.getErrorMessage()));
		return p;
	}

	public org.tdmx.core.api.v01.common.Error mapError(Integer errorCode, String message) {
		if (errorCode == null) {
			return null;
		}
		org.tdmx.core.api.v01.common.Error e = new org.tdmx.core.api.v01.common.Error();
		e.setCode(errorCode);
		e.setDescription(message);
		return e;
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
		sig.setTimestamp(CalendarUtils.getDateTime(agentSignature.getSignatureDate()));
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
		p.setValidUntil(CalendarUtils.getDateTime(ep.getValidUntil()));
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
