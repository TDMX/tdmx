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
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Flowsession;
import org.tdmx.core.api.v01.msg.Flowtargetsession;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.SignatureAlgorithm;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.message.domain.Message;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentSignature;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.EndpointPermissionGrant;
import org.tdmx.lib.zone.domain.FlowLimit;
import org.tdmx.lib.zone.domain.FlowSession;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSession;
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

	public Message mapMessage(Header header, Payload payload) {
		if (header == null) {
			return null;
		}
		Message msg = new Message(header.getMsgId(), CalendarUtils.getDateTime(header.getTimestamp()));
		msg.setLiveUntilTS(CalendarUtils.getDateTime(header.getTtl()));
		// header FlowChannel is separate for identifying the src/trg.
		msg.setFlowSessionId(header.getFlowsessionId());
		msg.setPayloadSignature(header.getPayloadSignature());
		msg.setHeaderSignature(header.getHeaderSignature());

		msg.setChunkSizeFactor(payload.getChunkSizeFactor());
		msg.setChunksCRC(payload.getChunksCRC());
		msg.setEncryptionContext(payload.getEncryptionContext());
		msg.setPayloadLength(payload.getLength());
		msg.setPlaintextLength(payload.getPlaintextLength());

		return msg;
	}

	public FlowTarget mapFlowTarget(AgentCredential target, Service service, Flowtargetsession fts) {
		if (fts == null) {
			return null;
		}
		FlowTarget s = new FlowTarget(target, service);
		mapFlowTargetSessions(s, fts);
		return s;
	}

	public FlowTargetSession mapFlowTargetSession(AgentCredentialDescriptor target, Flowtargetsession fts) {
		if (fts == null) {
			return null;
		}
		FlowTargetSession s = new FlowTargetSession();
		if (!fts.getFlowsessions().isEmpty()) {
			if (fts.getFlowsessions().size() > 0) {
				s.setPrimary(mapFlowSession(fts.getFlowsessions().get(0)));
			}
			if (fts.getFlowsessions().size() > 1) {
				s.setSecondary(mapFlowSession(fts.getFlowsessions().get(1)));
			}
		}

		if (fts.getSignaturevalue() != null) {
			AgentSignature sig = new AgentSignature();
			sig.setAlgorithm(mapSignatureAlgorithm(fts.getSignaturevalue().getSignatureAlgorithm()));
			sig.setValue(fts.getSignaturevalue().getSignature());
			sig.setSignatureDate(CalendarUtils.getDateTime(fts.getSignaturevalue().getTimestamp()));
			sig.setCertificateChainPem(target.getCertificateChainPem());
			s.setSignature(sig);
		}
		return s;
	}

	public void mapFlowTargetSessions(FlowTarget ft, Flowtargetsession fts) {
		if (!fts.getFlowsessions().isEmpty()) {
			if (fts.getFlowsessions().size() > 0) {
				ft.setPrimary(mapFlowSession(fts.getFlowsessions().get(0)));
			}
			if (fts.getFlowsessions().size() > 1) {
				ft.setSecondary(mapFlowSession(fts.getFlowsessions().get(1)));
			}
		}

		if (fts.getSignaturevalue() != null) {
			ft.setSignatureValue(fts.getSignaturevalue().getSignature());
			ft.setSignatureAlgorithm(mapSignatureAlgorithm(fts.getSignaturevalue().getSignatureAlgorithm()));
			ft.setSignatureDate(CalendarUtils.getDateTime(fts.getSignaturevalue().getTimestamp()));
		}
	}

	public FlowSession mapFlowSession(Flowsession fs) {
		if (fs == null) {
			return null;
		}
		FlowSession s = new FlowSession();
		s.setIdentifier(fs.getFlowsessionId());
		s.setScheme(fs.getScheme());
		s.setSessionKey(fs.getSessionKey());
		s.setValidFrom(CalendarUtils.getDateTime(fs.getValidFrom()));
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
			s.setSignatureDate(CalendarUtils.getDateTime(signature.getSignaturevalue().getTimestamp()));
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

	public EndpointPermission mapEndpointPermission(org.tdmx.core.api.v01.msg.EndpointPermission perm) {
		if (perm == null) {
			return null;
		}
		EndpointPermission p = new EndpointPermission();
		p.setGrant(mapEndpointPermissionGrant(perm.getPermission()));
		p.setMaxPlaintextSizeBytes(perm.getMaxPlaintextSizeBytes());
		p.setValidUntil(CalendarUtils.getDateTime(perm.getValidUntil()));
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
		o.setServiceProvider(origin.getServiceprovider());
		return o;
	}

	public ChannelDestination mapChannelDestination(org.tdmx.core.api.v01.msg.Destination destination) {
		if (destination == null) {
			return null;
		}
		ChannelDestination d = new ChannelDestination();
		d.setLocalName(destination.getLocalname());
		d.setDomainName(destination.getDomain());
		d.setServiceProvider(destination.getServiceprovider());
		d.setServiceName(destination.getServicename());
		return d;
	}

	public EndpointPermissionGrant mapEndpointPermissionGrant(org.tdmx.core.api.v01.msg.Permission permission) {
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
