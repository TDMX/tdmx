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
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Flowsession;
import org.tdmx.core.api.v01.msg.Flowtargetsession;
import org.tdmx.core.api.v01.msg.SignatureAlgorithm;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowSession;
import org.tdmx.lib.zone.domain.FlowTarget;
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

	public FlowTarget mapFlowTarget(AgentCredential target, Service service, Flowtargetsession fts) {
		if (fts == null) {
			return null;
		}
		FlowTarget s = new FlowTarget(target, service);
		mapFlowTargetSessions(s, fts);
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
		s.setScheme(fs.getScheme());
		s.setSessionKey(fs.getSessionKey());
		s.setValidFrom(CalendarUtils.getDateTime(fs.getValidFrom()));
		return s;
	}

	public ChannelAuthorization mapChannelAuthorization(Domain domain, Currentchannelauthorization ca) {
		if (ca == null) {
			return null;
		}
		ChannelAuthorization a = new ChannelAuthorization(domain);
		// TODO

		return a;
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
