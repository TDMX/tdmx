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
import org.tdmx.core.api.v01.msg.Authorization;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.msg.UserSignature;
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

	public Destinationsession checkDestinationsession(Destinationsession fts, Acknowledge ack) {
		if (fts == null) {
			setError(ErrorCode.MissingDestinationSession, ack);
			return null;
		}
		if (checkUsersignature(fts.getUsersignature(), ack) == null) {
			return null;
		}
		if (!StringUtils.hasText(fts.getEncryptionContextId())) {
			setError(ErrorCode.MissingDestinationSessionEncryptionContextIdentifier, ack);
			return null;
		}
		if (!StringUtils.hasText(fts.getScheme())) {
			setError(ErrorCode.MissingDestinationSessionScheme, ack);
			return null;
		}
		if (fts.getSessionKey() == null) {
			setError(ErrorCode.MissingDestinationSessionSessionKey, ack);
			return null;
		}
		return fts;
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

	public Permission checkEndpointPermission(Permission perm, Acknowledge ack) {
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

	public UserSignature checkUsersignature(UserSignature signature, Acknowledge ack) {
		if (signature == null) {
			setError(ErrorCode.MissingUserSignature, ack);
			return null;
		}
		if (checkUserIdentity(signature.getUserIdentity(), ack) == null) {
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
			setError(ErrorCode.MissingZoneRootPublicKey, ack);
			return null;
		}
		return admin;
	}

	public UserIdentity checkUserIdentity(UserIdentity user, Acknowledge ack) {
		if (user == null) {
			setError(ErrorCode.MissingUserIdentity, ack);
			return null;
		}
		if (user.getUsercertificate() == null) {
			setError(ErrorCode.MissingUserPublicKey, ack);
			return null;
		}
		if (user.getDomaincertificate() == null) {
			setError(ErrorCode.MissingDomainAdministratorPublicKey, ack);
			return null;
		}
		if (user.getRootcertificate() == null) {
			setError(ErrorCode.MissingZoneRootPublicKey, ack);
			return null;
		}
		return user;
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

	public Msg checkMessage(Msg msg, Acknowledge ack) {
		if (msg == null) {
			setError(ErrorCode.MissingMessage, ack);
			return null;
		}
		if (checkHeader(msg.getHeader(), ack) == null) {
			return null;
		}
		if (checkPayload(msg.getPayload(), ack) == null) {
			return null;
		}
		if (checkChunk(msg.getChunk(), ack) == null) {
			return null;
		}
		return msg;
	}

	public Chunk checkChunk(Chunk chunk, Acknowledge ack) {
		if (chunk == null) {
			setError(ErrorCode.MissingChunk, ack);
			return null;
		}
		if (chunk.getData() == null) {
			setError(ErrorCode.MissingChunkData, ack);
			return null;
		}
		if (!StringUtils.hasText(chunk.getMsgId())) {
			setError(ErrorCode.MissingChunkMsgId, ack);
			return null;
		}
		if (!StringUtils.hasText(chunk.getMac())) {
			setError(ErrorCode.MissingChunkMac, ack);
			return null;
		}
		if (chunk.getPos() < 0) {
			setError(ErrorCode.InvalidChunkPos, ack);
			return null;
		}
		return chunk;
	}

	public Header checkHeader(Header hdr, Acknowledge ack) {
		if (hdr == null) {
			setError(ErrorCode.MissingHeader, ack);
			return null;
		}
		if (checkChannel(hdr.getChannel(), ack) == null) {
			return null;
		}
		if (checkUsersignature(hdr.getUsersignature(), ack) == null) {
			return null;
		}
		if (hdr.getTo() == null) {
			setError(ErrorCode.MissingHeaderTo, ack);
			return null;
		}
		if (checkUserIdentity(hdr.getTo(), ack) == null) {
			return null;
		}
		if (hdr.getTtl() == null) {
			setError(ErrorCode.MissingHeaderTTL, ack);
			return null;
		}
		if (!StringUtils.hasText(hdr.getEncryptionContextId())) {
			setError(ErrorCode.MissingHeaderEncryptionContextId, ack);
			return null;
		}
		if (!StringUtils.hasText(hdr.getMsgId())) {
			setError(ErrorCode.MissingHeaderMsgId, ack);
			return null;
		}
		if (!StringUtils.hasText(hdr.getPayloadSignature())) {
			setError(ErrorCode.MissingHeaderPayloadSignature, ack);
			return null;
		}
		return hdr;
	}

	public Payload checkPayload(Payload payload, Acknowledge ack) {
		if (payload == null) {
			setError(ErrorCode.MissingPayload, ack);
			return null;
		}

		if (!StringUtils.hasText(payload.getMACofMACs())) {
			setError(ErrorCode.MissingPayloadChunksMACofMACs, ack);
			return null;
		}
		if (payload.getEncryptionContext() == null) {
			setError(ErrorCode.MissingPayloadEncryptionContext, ack);
			return null;
		}
		if (payload.getChunkSize() < 0) {
			setError(ErrorCode.InvalidChunkSizeFactor, ack);
			return null;
		}
		if (payload.getLength() < 0) {
			setError(ErrorCode.InvalidPayloadLength, ack);
			return null;
		}
		if (payload.getPlaintextLength() < 0) {
			setError(ErrorCode.InvalidPlaintextLength, ack);
			return null;
		}
		return payload;
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

	public Authorization checkAuthorization(Authorization auth, Acknowledge ack) {
		if (auth == null) {
			setError(ErrorCode.MissingAuthorization, ack);
			return null;
		}
		if (checkChannel(auth, ack) == null) {
			return null;
		}
		if (checkEndpointPermission(auth.getPermission(), ack) == null) {
			return null;
		}
		return auth;
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
