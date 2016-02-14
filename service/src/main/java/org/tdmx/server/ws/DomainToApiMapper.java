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
import org.tdmx.core.api.v01.common.Ps;
import org.tdmx.core.api.v01.common.Taskstatus;
import org.tdmx.core.api.v01.msg.Address;
import org.tdmx.core.api.v01.msg.Administrator;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Channelauthorization;
import org.tdmx.core.api.v01.msg.Channelinfo;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.CredentialStatus;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destinationinfo;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Dr;
import org.tdmx.core.api.v01.msg.FlowStatus;
import org.tdmx.core.api.v01.msg.Flowcontrolstatus;
import org.tdmx.core.api.v01.msg.Grant;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.IpAddressList;
import org.tdmx.core.api.v01.msg.Limit;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Msgreference;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.RequestedChannelAuthorization;
import org.tdmx.core.api.v01.msg.Service;
import org.tdmx.core.api.v01.msg.Sessioninfo;
import org.tdmx.core.api.v01.msg.SignatureAlgorithm;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.api.v01.msg.User;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.msg.UserSignature;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.FlowControlStatus;
import org.tdmx.lib.zone.domain.FlowQuota;

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

	public Channelinfo mapChannelInfo(org.tdmx.lib.zone.domain.Channel c) {
		if (c == null) {
			return null;
		}
		Channelinfo ci = new Channelinfo();
		ci.setChannelauthorization(mapChannelAuthorization(c.getAuthorization()));
		ci.setStatus(mapFlowStatus(c.getQuota()));
		ci.setSessioninfo(mapSessionInfo(c.getSession(), c.getProcessingState()));
		return ci;
	}

	public FlowStatus mapFlowStatus(FlowQuota quota) {
		if (quota == null) {
			return null;
		}
		FlowStatus fs = new FlowStatus();
		fs.setRelayStatus(mapFlowControlStatus(quota.getRelayStatus()));
		fs.setFlowStatus(mapFlowControlStatus(quota.getFlowStatus()));
		fs.setUsedBytes(quota.getUsedBytes());
		return fs;
	}

	public Sessioninfo mapSessionInfo(DestinationSession ds, ProcessingState ps) {
		if (ds == null || ps == null) {
			return null;
		}
		Sessioninfo s = new Sessioninfo();
		s.setDestinationsession(mapDestinationSession(ds));
		s.setPs(mapProcessingStatus(ps));
		return s;
	}

	public Destinationinfo mapDestinationInfo(org.tdmx.lib.zone.domain.ChannelDestination d, DestinationSession ds) {
		if (d == null || ds == null) {
			return null;
		}
		Destinationinfo di = new Destinationinfo();
		di.setDestinationsession(mapDestinationSession(ds));
		di.setDomain(d.getDomainName());
		di.setLocalname(d.getLocalName());
		di.setServicename(d.getServiceName());
		return di;
	}

	public Destinationsession mapDestinationSession(DestinationSession ds) {
		if (ds == null) {
			return null;
		}
		Destinationsession f = new Destinationsession();

		f.setUsersignature(mapUserSignature(ds.getSignature()));
		f.setEncryptionContextId(ds.getEncryptionContextId());
		f.setScheme(ds.getScheme());
		f.setSessionKey(ds.getSessionKey());

		return f;
	}

	public User mapUser(AgentCredential cred) {
		if (cred == null) {
			return null;
		}
		User us = new User();
		us.setUserIdentity(mapUserIdentity(cred.getCertificateChain()));
		us.setStatus(CredentialStatus.fromValue(cred.getCredentialStatus().name()));
		us.setWhitelist(new IpAddressList()); // TODO #99: ipwhitelist
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
		us.setWhitelist(new IpAddressList()); // TODO #99: ipwhitelist
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

	public Service mapService(org.tdmx.lib.zone.domain.Service service) {
		if (service == null) {
			return null;
		}
		Service s = new Service();
		s.setDomain(service.getDomain().getDomainName());
		s.setServicename(service.getServiceName());
		return s;
	}

	public ChannelEndpoint mapChannelOrigin(org.tdmx.lib.zone.domain.ChannelOrigin origin) {
		if (origin == null) {
			return null;
		}
		ChannelEndpoint o = new ChannelEndpoint();
		o.setDomain(origin.getDomainName());
		o.setLocalname(origin.getLocalName());
		return o;
	}

	public ChannelDestination mapChannelDestination(org.tdmx.lib.zone.domain.ChannelDestination dest) {
		if (dest == null) {
			return null;
		}
		ChannelDestination d = new ChannelDestination();
		d.setDomain(dest.getDomainName());
		d.setLocalname(dest.getLocalName());
		d.setServicename(dest.getServiceName());
		return d;
	}

	public Msg mapChannelMessage(org.tdmx.lib.zone.domain.ChannelMessage msg) {
		if (msg == null) {
			return null;
		}
		Msg m = new Msg();

		Header h = new Header();
		h.setMsgId(msg.getMsgId());
		h.setChannel(mapChannel(msg.getChannel()));
		h.setEncryptionContextId(msg.getEncryptionContextId());
		h.setExternalReference(msg.getExternalReference());
		h.setTtl(CalendarUtils.cast(msg.getTtlTimestamp()));
		h.setPayloadSignature(msg.getPayloadSignature());
		h.setTo(mapUserIdentity(msg.getReceipt().getCertificateChain()));
		h.setUsersignature(mapUserSignature(msg.getSignature()));
		m.setHeader(h);

		Payload p = new Payload();
		p.setChunkSize(msg.getChunkSize());
		p.setEncryptionContext(msg.getEncryptionContext());
		p.setLength(msg.getPayloadLength());
		p.setPlaintextLength(msg.getPlaintextLength());
		p.setMACofMACs(msg.getMacOfMacs());
		m.setPayload(p);

		return m;
	}

	public Chunk mapChunk(org.tdmx.lib.message.domain.Chunk chunk) {
		if (chunk == null) {
			return null;
		}
		Chunk c = new Chunk();
		c.setMsgId(chunk.getMsgId());
		c.setPos(chunk.getPos());
		c.setMac(chunk.getMac());
		c.setData(chunk.getData());
		return c;
	}

	public Dr mapDeliveryReceipt(org.tdmx.lib.zone.domain.ChannelMessage msg) {
		if (msg == null) {
			return null;
		}
		Dr dr = new Dr();

		Msgreference r = new Msgreference();
		r.setMsgId(msg.getMsgId());
		r.setExternalReference(msg.getExternalReference());
		r.setSignature(msg.getSignature().getValue());
		dr.setMsgreference(r);

		dr.setReceiptsignature(mapUserSignature(msg.getReceipt()));
		return dr;
	}

	public Channel mapChannel(org.tdmx.lib.zone.domain.Channel channel) {
		if (channel == null) {
			return null;
		}
		Channel c = new Channel();
		c.setDestination(mapChannelDestination(channel.getDestination()));
		c.setOrigin(mapChannelOrigin(channel.getOrigin()));
		return c;
	}

	public Channel mapChannel(org.tdmx.lib.zone.domain.TemporaryChannel channel) {
		if (channel == null) {
			return null;
		}
		Channel c = new Channel();
		c.setDestination(mapChannelDestination(channel.getDestination()));
		c.setOrigin(mapChannelOrigin(channel.getOrigin()));
		return c;
	}

	public Channelauthorization mapChannelAuthorization(org.tdmx.lib.zone.domain.ChannelAuthorization ca) {
		if (ca == null) {
			return null;
		}
		Channelauthorization c = new Channelauthorization();
		c.setChannel(mapChannel(ca.getChannel()));
		c.setDomain(ca.getChannel().getDomain().getDomainName());
		if (ca.getSendAuthorization() != null || ca.getRecvAuthorization() != null) {
			Currentchannelauthorization current = new Currentchannelauthorization();
			current.setOriginPermission(mapPermission(ca.getSendAuthorization()));
			current.setDestinationPermission(mapPermission(ca.getRecvAuthorization()));
			current.setLimit(mapLimit(ca.getLimit()));
			current.setAdministratorsignature(mapAdministratorSignature(ca.getSignature()));
			c.setCurrent(current);
		}
		if (ca.getReqRecvAuthorization() != null || ca.getSendAuthorization() != null) {
			RequestedChannelAuthorization unconfirmed = new RequestedChannelAuthorization();
			unconfirmed.setOriginPermission(mapPermission(ca.getReqSendAuthorization()));
			unconfirmed.setDestinationPermission(mapPermission(ca.getReqRecvAuthorization()));
			c.setUnconfirmed(unconfirmed);
		}
		c.setPs(mapProcessingStatus(ca.getProcessingState()));
		return c;
	}

	public Ps mapProcessingStatus(org.tdmx.lib.common.domain.ProcessingState ps) {
		Ps p = new Ps();
		p.setStatus(Taskstatus.fromValue(ps.getStatus().toString()));
		p.setTimestamp(CalendarUtils.cast(ps.getTimestamp()));
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

	public UserSignature mapUserSignature(org.tdmx.lib.zone.domain.AgentSignature agentSignature) {
		if (agentSignature == null) {
			return null;
		}
		UserSignature us = new UserSignature();
		us.setSignaturevalue(mapSignature(agentSignature));
		us.setUserIdentity(mapUserIdentity(agentSignature.getCertificateChain()));
		return us;
	}

	public Signaturevalue mapSignature(org.tdmx.lib.zone.domain.AgentSignature agentSignature) {
		if (agentSignature == null) {
			return null;
		}
		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.cast(agentSignature.getSignatureDate()));
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

	public Flowcontrolstatus mapFlowControlStatus(FlowControlStatus sa) {
		if (sa == null) {
			return null;
		}
		return Flowcontrolstatus.fromValue(sa.name());
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

	public Permission mapPermission(org.tdmx.lib.zone.domain.EndpointPermission ep) {
		if (ep == null) {
			return null;
		}
		Permission p = new Permission();
		p.setAdministratorsignature(mapAdministratorSignature(ep.getSignature()));
		p.setMaxPlaintextSizeBytes(ep.getMaxPlaintextSizeBytes());
		p.setPermission(Grant.valueOf(ep.getGrant().toString()));
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
