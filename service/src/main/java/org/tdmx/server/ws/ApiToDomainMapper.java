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
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.SignatureAlgorithm;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AgentSignature;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelName;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.EndpointPermissionGrant;
import org.tdmx.lib.zone.domain.FlowLimit;
import org.tdmx.lib.zone.domain.Service;

public class ApiToDomainMapper {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ApiToDomainMapper.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public PageSpecifier mapPage(Page p) {
		if (p == null) {
			return null;
		}
		return new PageSpecifier(p.getNumber(), p.getSize());
	}

	public ChannelName mapChannelName(Channel c) {
		if (c == null) {
			return null;
		}
		ChannelName cn = new ChannelName(mapChannelOrigin(c.getOrigin()), mapChannelDestination(c.getDestination()));
		return cn;
	}

	public ChannelMessage mapMessage(Msg msg) {
		ChannelMessage md = new ChannelMessage();
		md.setMsgId(msg.getHeader().getMsgId());
		md.setTtlTimestamp(CalendarUtils.cast(msg.getHeader().getTtl()));
		md.setEncryptionContextId(msg.getHeader().getEncryptionContextId());
		md.setPayloadSignature(msg.getHeader().getPayloadSignature());
		md.setExternalReference(msg.getHeader().getExternalReference());
		md.setReceiverCertificateChainPem(CertificateIOUtils.safeX509certsToPem(msg.getHeader().getTo()
				.getUsercertificate(), msg.getHeader().getTo().getDomaincertificate(), msg.getHeader().getTo()
				.getRootcertificate()));
		md.setSignature(mapUserSignature(msg.getHeader().getUsersignature()));

		md.setChunkSize(msg.getPayload().getChunkSize());
		md.setPayloadLength(msg.getPayload().getLength());
		md.setEncryptionContext(msg.getPayload().getEncryptionContext());
		md.setPlaintextLength(msg.getPayload().getPlaintextLength());
		md.setMacOfMacs(msg.getPayload().getMACofMACs());
		return md;
	}

	public Chunk mapChunk(org.tdmx.core.api.v01.msg.Chunk chunk) {
		if (chunk == null) {
			return null;
		}
		Chunk c = new Chunk(chunk.getMsgId(), chunk.getPos());
		c.setData(chunk.getData());
		c.setMac(chunk.getMac());
		return c;
	}

	public Destination mapDestination(Address address, Service service, Destinationsession ds) {
		if (ds == null) {
			return null;
		}
		Destination s = new Destination(address, service);
		s.setDestinationSession(mapDestinationSession(ds));
		return s;
	}

	public DestinationSession mapDestinationSession(Destinationsession ds) {
		if (ds == null) {
			return null;
		}
		DestinationSession s = new DestinationSession();
		s.setIdentifier(ds.getEncryptionContextId());
		s.setScheme(ds.getScheme());
		s.setSessionKey(ds.getSessionKey());

		s.setSignature(mapUserSignature(ds.getUsersignature()));
		return s;
	}

	public ChannelAuthorization mapChannelAuthorization(Currentchannelauthorization ca) {
		if (ca == null) {
			return null;
		}
		ChannelAuthorization a = new ChannelAuthorization();
		a.setSendAuthorization(mapEndpointPermission(ca.getOrigin()));
		a.setRecvAuthorization(mapEndpointPermission(ca.getDestination()));
		a.setReqSendAuthorization(null);
		a.setReqSendAuthorization(null);
		if (ca.getLimit() != null) {
			a.setUndeliveredBuffer(mapFlowLimit(ca.getLimit().getUndeliveredBuffer()));
			a.setUnsentBuffer(mapFlowLimit(ca.getLimit().getUnsentBuffer()));
		}
		a.setSignature(mapAdministratorSignature(ca.getAdministratorsignature()));
		return a;
	}

	public AgentSignature mapAdministratorSignature(org.tdmx.core.api.v01.msg.Administratorsignature signature) {
		if (signature == null) {
			return null;
		}
		AgentSignature s = new AgentSignature();
		if (signature.getAdministratorIdentity() != null) {
			s.setCertificateChainPem(CertificateIOUtils.safeX509certsToPem(signature.getAdministratorIdentity()
					.getDomaincertificate(), signature.getAdministratorIdentity().getRootcertificate()));
		}
		if (signature.getSignaturevalue() != null) {
			s.setAlgorithm(mapSignatureAlgorithm(signature.getSignaturevalue().getSignatureAlgorithm()));
			s.setSignatureDate(CalendarUtils.cast(signature.getSignaturevalue().getTimestamp()));
			s.setValue(signature.getSignaturevalue().getSignature());
		}
		return s;
	}

	public AgentSignature mapUserSignature(org.tdmx.core.api.v01.msg.Usersignature signature) {
		if (signature == null) {
			return null;
		}
		AgentSignature s = new AgentSignature();
		if (signature.getUserIdentity() != null) {
			s.setCertificateChainPem(CertificateIOUtils.safeX509certsToPem(signature.getUserIdentity()
					.getUsercertificate(), signature.getUserIdentity().getDomaincertificate(), signature
					.getUserIdentity().getRootcertificate()));
		}
		if (signature.getSignaturevalue() != null) {
			s.setAlgorithm(mapSignatureAlgorithm(signature.getSignaturevalue().getSignatureAlgorithm()));
			s.setSignatureDate(CalendarUtils.cast(signature.getSignaturevalue().getTimestamp()));
			s.setValue(signature.getSignaturevalue().getSignature());
		}
		return s;
	}

	public FlowLimit mapFlowLimit(org.tdmx.core.api.v01.msg.Limit limit) {
		if (limit == null) {
			return null;
		}
		FlowLimit l = new FlowLimit();
		l.setHighMarkBytes(limit.getHighBytes());
		l.setLowMarkBytes(limit.getLowBytes());
		return l;
	}

	public EndpointPermission mapEndpointPermission(org.tdmx.core.api.v01.msg.Permission perm) {
		if (perm == null) {
			return null;
		}
		EndpointPermission p = new EndpointPermission();
		p.setGrant(mapEndpointPermissionGrant(perm.getPermission()));
		p.setMaxPlaintextSizeBytes(perm.getMaxPlaintextSizeBytes());
		p.setValidUntil(CalendarUtils.cast(perm.getValidUntil()));
		p.setSignature(mapAdministratorSignature(perm.getAdministratorsignature()));
		return p;
	}

	public ChannelOrigin mapChannelOrigin(org.tdmx.core.api.v01.msg.ChannelEndpoint origin) {
		if (origin == null) {
			return null;
		}
		ChannelOrigin o = new ChannelOrigin();
		o.setLocalName(origin.getLocalname());
		o.setDomainName(origin.getDomain());
		return o;
	}

	public ChannelDestination mapChannelDestination(org.tdmx.core.api.v01.msg.ChannelDestination destination) {
		if (destination == null) {
			return null;
		}
		ChannelDestination d = new ChannelDestination();
		d.setLocalName(destination.getLocalname());
		d.setDomainName(destination.getDomain());
		d.setServiceName(destination.getServicename());
		return d;
	}

	public EndpointPermissionGrant mapEndpointPermissionGrant(org.tdmx.core.api.v01.msg.Grant permission) {
		if (permission == null) {
			return null;
		}
		return EndpointPermissionGrant.valueOf(permission.value());
	}

	public org.tdmx.client.crypto.algorithm.SignatureAlgorithm mapSignatureAlgorithm(SignatureAlgorithm sa) {
		if (sa == null) {
			return null;
		}
		return org.tdmx.client.crypto.algorithm.SignatureAlgorithm.getByAlgorithmName(sa.value());
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
