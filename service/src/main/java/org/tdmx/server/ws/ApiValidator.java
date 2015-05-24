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
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.EndpointPermission;
import org.tdmx.core.api.v01.msg.Flowsession;
import org.tdmx.core.api.v01.msg.Flowtargetsession;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.system.lang.StringUtils;

public class ApiValidator {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ApiValidator.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public Flowtargetsession checkFlowtargetsession(Flowtargetsession fts, Acknowledge ack) {
		if (fts == null) {
			setError(ErrorCode.MissingFlowTargetSession, ack);
		}
		if (checkSignaturevalue(fts.getSignaturevalue(), ack) == null) {
			return null;
		}
		for (Flowsession fs : fts.getFlowsessions()) {
			if (checkFlowsession(fs, ack) == null) {
				return null;
			}
		}
		return fts;
	}

	public Flowsession checkFlowsession(Flowsession fs, Acknowledge ack) {
		if (fs == null) {
			setError(ErrorCode.MissingFlowSession, ack);
		}
		if (!StringUtils.hasText(fs.getScheme())) {
			setError(ErrorCode.MissingFlowSessionScheme, ack);
			return null;
		}
		if (fs.getValidFrom() == null) {
			setError(ErrorCode.MissingFlowSessionValidFrom, ack);
			return null;
		}
		if (fs.getSessionKey() == null) {
			setError(ErrorCode.MissingFlowSessionSessionKey, ack);
			return null;
		}
		return fs;
	}

	public Currentchannelauthorization checkChannelauthorization(Currentchannelauthorization ca, Acknowledge ack) {
		if (ca == null) {
			setError(ErrorCode.MissingChannelAuthorization, ack);
			return null;
		}
		if (checkChannel(ca.getChannel(), ack) == null) {
			return null;
		}
		if (ca.getOrigin() != null && checkEndpointPermission(ca.getOrigin(), ack) == null) {
			return null;
		}
		if (ca.getDestination() != null && checkEndpointPermission(ca.getDestination(), ack) == null) {
			return null;
		}
		if (ca.getLimit() == null) {
			setError(ErrorCode.MissingFlowControlLimit, ack);
			return null;
		}
		if (checkAdministratorsignature(ca.getAdministratorsignature(), ack) == null) {
			return null;
		}
		return ca;
	}

	public EndpointPermission checkEndpointPermission(EndpointPermission perm, Acknowledge ack) {
		if (perm == null) {
			setError(ErrorCode.MissingEndpointPermission, ack);
			return null;
		}
		if (checkAdministratorsignature(perm.getAdministratorsignature(), ack) == null) {
			return null;
		}
		if (perm.getMaxPlaintextSizeBytes() == null) {
			setError(ErrorCode.MissingPlaintextSizeEndpointPermission, ack);
			return null;
		}
		if (perm.getValidUntil() == null) {
			setError(ErrorCode.MissingValidUntilEndpointPermission, ack);
			return null;
		}
		if (perm.getPermission() == null) {
			setError(ErrorCode.MissingPermissionEndpointPermission, ack);
			return null;
		}
		return perm;
	}

	public Administratorsignature checkAdministratorsignature(Administratorsignature signature, Acknowledge ack) {
		if (signature == null) {
			setError(ErrorCode.MissingAdministratorSignature, ack);
			return null;
		}
		if (checkAdministratorIdentity(signature.getAdministratorIdentity(), ack) == null) {
			return null;
		}
		if (checkSignaturevalue(signature.getSignaturevalue(), ack) == null) {
			return null;
		}
		return signature;
	}

	public Signaturevalue checkSignaturevalue(Signaturevalue sig, Acknowledge ack) {
		if (sig == null) {
			setError(ErrorCode.MissingSignatureValue, ack);
			return null;
		}
		if (!StringUtils.hasText(sig.getSignature())) {
			setError(ErrorCode.MissingSignature, ack);
			return null;
		}
		if (sig.getSignatureAlgorithm() == null) {
			setError(ErrorCode.MissingSignatureAlgorithm, ack);
			return null;
		}
		if (sig.getTimestamp() == null) {
			setError(ErrorCode.MissingSignatureTimestamp, ack);
			return null;
		}
		return sig;
	}

	public AdministratorIdentity checkAdministratorIdentity(AdministratorIdentity admin, Acknowledge ack) {
		if (admin == null) {
			setError(ErrorCode.MissingAdministratorIdentity, ack);
			return null;
		}
		if (admin.getDomaincertificate() == null) {
			setError(ErrorCode.MissingDomainAdministratorPublicKey, ack);
			return null;
		}
		if (admin.getRootcertificate() == null) {
			setError(ErrorCode.MissingDomainAdministratorZoneRootPublicKey, ack);
			return null;
		}
		return admin;
	}

	public ChannelEndpoint checkChannelEndpoint(ChannelEndpoint channelEndpoint, Acknowledge ack) {
		if (channelEndpoint == null) {
			setError(ErrorCode.MissingChannelEndpoint, ack);
			return null;
		}
		if (!StringUtils.hasText(channelEndpoint.getDomain())) {
			setError(ErrorCode.MissingChannelEndpointDomain, ack);
			return null;
		}
		if (!StringUtils.hasText(channelEndpoint.getLocalname())) {
			setError(ErrorCode.MissingChannelEndpointLocalname, ack);
			return null;
		}
		if (!StringUtils.hasText(channelEndpoint.getServiceprovider())) {
			setError(ErrorCode.MissingChannelEndpointServiceprovider, ack);
			return null;
		}
		return channelEndpoint;
	}

	public Destination checkChannelDestination(Destination dest, Acknowledge ack) {
		if (checkChannelEndpoint(dest, ack) == null) {
			return null;
		}
		if (!StringUtils.hasText(dest.getServicename())) {
			setError(ErrorCode.MissingChannelDestinationService, ack);
			return null;
		}
		return dest;
	}

	public Channel checkChannel(Channel channel, Acknowledge ack) {
		if (channel == null) {
			setError(ErrorCode.MissingChannel, ack);
			return null;
		}
		if (checkChannelEndpoint(channel.getOrigin(), ack) == null) {
			return null;
		}
		if (checkChannelDestination(channel.getDestination(), ack) == null) {
			return null;
		}
		return channel;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setError(ErrorCode ec, Acknowledge ack) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription());
		ack.setError(error);
		ack.setSuccess(false);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
