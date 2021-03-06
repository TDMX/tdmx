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
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.msg.Address;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Authorization;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.ChunkReference;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.Service;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.msg.Usersignature;
import org.tdmx.core.api.v01.tx.Localtransaction;
import org.tdmx.core.api.v01.tx.Transaction;
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
			ErrorCode.setError(ErrorCode.MissingDestinationSession, ack);
			return null;
		}
		if (checkUserSignature(fts.getUsersignature(), ack) == null) {
			return null;
		}
		if (!StringUtils.hasText(fts.getEncryptionContextId())) {
			ErrorCode.setError(ErrorCode.MissingDestinationSessionEncryptionContextIdentifier, ack);
			return null;
		}
		if (!StringUtils.hasText(fts.getScheme())) {
			ErrorCode.setError(ErrorCode.MissingDestinationSessionScheme, ack);
			return null;
		}
		if (fts.getSessionKey() == null) {
			ErrorCode.setError(ErrorCode.MissingDestinationSessionSessionKey, ack);
			return null;
		}
		return fts;
	}

	public Currentchannelauthorization checkChannelauthorization(Currentchannelauthorization ca, Acknowledge ack) {
		if (ca == null) {
			ErrorCode.setError(ErrorCode.MissingChannelAuthorization, ack);
			return null;
		}
		if (ca.getOriginPermission() != null && checkEndpointPermission(ca.getOriginPermission(), ack) == null) {
			return null;
		}
		if (ca.getDestinationPermission() != null
				&& checkEndpointPermission(ca.getDestinationPermission(), ack) == null) {
			return null;
		}
		if (ca.getLimit() == null) {
			ErrorCode.setError(ErrorCode.MissingFlowControlLimit, ack);
			return null;
		}
		if (ca.getDestinationPermission() != null) {
			// we must have retry count and delay set
			if (ca.getMaxRedeliveryCount() == null) {
				ErrorCode.setError(ErrorCode.MissingMaxRedeliveryCount, ack);
				return null;
			}
			if (ca.getMinRedeliveryDelaySec() == null) {
				ErrorCode.setError(ErrorCode.MissingMinRedeliveryDelay, ack);
				return null;
			}
		} else {
			// we must have retry count and delay set
			if (ca.getMaxRedeliveryCount() != null) {
				ErrorCode.setError(ErrorCode.InvalidMaxRedeliveryCount, ack);
				return null;
			}
			if (ca.getMinRedeliveryDelaySec() != null) {
				ErrorCode.setError(ErrorCode.InvalidMinRedeliveryDelay, ack);
				return null;
			}
		}
		if (checkAdministratorsignature(ca.getAdministratorsignature(), ack) == null) {
			return null;
		}
		return ca;
	}

	public Permission checkEndpointPermission(Permission perm, Acknowledge ack) {
		if (perm == null) {
			ErrorCode.setError(ErrorCode.MissingEndpointPermission, ack);
			return null;
		}
		if (checkAdministratorsignature(perm.getAdministratorsignature(), ack) == null) {
			return null;
		}
		if (perm.getMaxPlaintextSizeBytes() == null) {
			ErrorCode.setError(ErrorCode.MissingPlaintextSizeEndpointPermission, ack);
			return null;
		}
		if (perm.getPermission() == null) {
			ErrorCode.setError(ErrorCode.MissingPermissionEndpointPermission, ack);
			return null;
		}
		return perm;
	}

	/**
	 * Checks all fields are present.
	 * 
	 * @param signature
	 * @param ack
	 * @return
	 */
	public Administratorsignature checkAdministratorsignature(Administratorsignature signature, Acknowledge ack) {
		if (signature == null) {
			ErrorCode.setError(ErrorCode.MissingAdministratorSignature, ack);
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

	/**
	 * Checks all fields are present.
	 * 
	 * @param signature
	 * @param ack
	 * @return
	 */
	public Usersignature checkUserSignature(Usersignature signature, Acknowledge ack) {
		if (signature == null) {
			ErrorCode.setError(ErrorCode.MissingUserSignature, ack);
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

	/**
	 * Checks all fields are present.
	 * 
	 * @param sig
	 * @param ack
	 * @return
	 */
	public Signaturevalue checkSignaturevalue(Signaturevalue sig, Acknowledge ack) {
		if (sig == null) {
			ErrorCode.setError(ErrorCode.MissingSignatureValue, ack);
			return null;
		}
		if (!StringUtils.hasText(sig.getSignature())) {
			ErrorCode.setError(ErrorCode.MissingSignature, ack);
			return null;
		}
		if (sig.getSignatureAlgorithm() == null) {
			ErrorCode.setError(ErrorCode.MissingSignatureAlgorithm, ack);
			return null;
		}
		if (sig.getTimestamp() == null) {
			ErrorCode.setError(ErrorCode.MissingSignatureTimestamp, ack);
			return null;
		}
		return sig;
	}

	public AdministratorIdentity checkAdministratorIdentity(AdministratorIdentity admin, Acknowledge ack) {
		if (admin == null) {
			ErrorCode.setError(ErrorCode.MissingAdministratorIdentity, ack);
			return null;
		}
		if (admin.getDomaincertificate() == null) {
			ErrorCode.setError(ErrorCode.MissingDomainAdministratorPublicKey, ack);
			return null;
		}
		if (admin.getRootcertificate() == null) {
			ErrorCode.setError(ErrorCode.MissingZoneRootPublicKey, ack);
			return null;
		}
		return admin;
	}

	public UserIdentity checkUserIdentity(UserIdentity user, Acknowledge ack) {
		if (user == null) {
			ErrorCode.setError(ErrorCode.MissingUserIdentity, ack);
			return null;
		}
		if (user.getUsercertificate() == null) {
			ErrorCode.setError(ErrorCode.MissingUserPublicKey, ack);
			return null;
		}
		if (user.getDomaincertificate() == null) {
			ErrorCode.setError(ErrorCode.MissingDomainAdministratorPublicKey, ack);
			return null;
		}
		if (user.getRootcertificate() == null) {
			ErrorCode.setError(ErrorCode.MissingZoneRootPublicKey, ack);
			return null;
		}
		return user;
	}

	public Address checkAddress(Address address, Acknowledge ack) {
		if (address == null) {
			ErrorCode.setError(ErrorCode.MissingAddress, ack);
			return null;
		}
		if (!StringUtils.hasText(address.getDomain())) {
			ErrorCode.setError(ErrorCode.MissingDomain, ack);
			return null;
		}
		if (!StringUtils.hasText(address.getLocalname())) {
			ErrorCode.setError(ErrorCode.MissingLocalname, ack);
			return null;
		}
		return address;
	}

	public Service checkService(Service service, Acknowledge ack) {
		if (service == null) {
			ErrorCode.setError(ErrorCode.MissingService, ack);
			return null;
		}
		if (!StringUtils.hasText(service.getDomain())) {
			ErrorCode.setError(ErrorCode.MissingDomain, ack);
			return null;
		}
		if (!StringUtils.hasText(service.getServicename())) {
			ErrorCode.setError(ErrorCode.MissingServiceName, ack);
			return null;
		}
		return service;
	}

	public ChannelEndpoint checkChannelEndpoint(ChannelEndpoint channelEndpoint, Acknowledge ack) {
		if (channelEndpoint == null) {
			ErrorCode.setError(ErrorCode.MissingChannelEndpoint, ack);
			return null;
		}
		if (!StringUtils.hasText(channelEndpoint.getDomain())) {
			ErrorCode.setError(ErrorCode.MissingChannelEndpointDomain, ack);
			return null;
		}
		if (!StringUtils.hasText(channelEndpoint.getLocalname())) {
			ErrorCode.setError(ErrorCode.MissingChannelEndpointLocalname, ack);
			return null;
		}
		return channelEndpoint;
	}

	public ChannelDestination checkChannelDestination(ChannelDestination dest, Acknowledge ack) {
		if (checkChannelEndpoint(dest, ack) == null) {
			return null;
		}
		if (!StringUtils.hasText(dest.getServicename())) {
			ErrorCode.setError(ErrorCode.MissingChannelDestinationService, ack);
			return null;
		}
		return dest;
	}

	public Msg checkMessage(Msg msg, Acknowledge ack, boolean chunkRequired) {
		if (msg == null) {
			ErrorCode.setError(ErrorCode.MissingMessage, ack);
			return null;
		}
		if (checkHeader(msg.getHeader(), ack) == null) {
			return null;
		}
		if (checkPayload(msg.getPayload(), ack) == null) {
			return null;
		}
		if (chunkRequired && checkChunk(msg.getChunk(), ack) == null) {
			return null;
		}
		return msg;
	}

	public boolean checkTransactionChoice(Transaction tx, Localtransaction local, Acknowledge ack) {
		if (tx == null && local == null) {
			ErrorCode.setError(ErrorCode.MissingTransaction, ack);
			return false;
		} else if (tx != null && local != null) {
			ErrorCode.setError(ErrorCode.InvalidTransaction, ack);
			return false;
		}
		return true;
	}

	public boolean checkTransactionChoice(Transaction tx, Localtransaction local, int minTxTimeoutSec,
			int maxTxTimeoutSec, Acknowledge ack) {
		if (tx == null && local == null) {
			ErrorCode.setError(ErrorCode.MissingTransaction, ack);
			return false;
		} else if (tx != null && local != null) {
			ErrorCode.setError(ErrorCode.InvalidTransaction, ack);
			return false;
		} else if (tx != null) {
			return checkTransaction(tx, ack, minTxTimeoutSec, maxTxTimeoutSec) != null;
		}
		return checkLocalTransaction(local, ack, minTxTimeoutSec, maxTxTimeoutSec) != null;
	}

	public Transaction checkTransaction(Transaction tx, Acknowledge ack, int minTxTimeoutSec, int maxTxTimeoutSec) {
		if (!StringUtils.hasText(tx.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, ack);
			return null;
		}
		if (tx.getTxtimeout() < minTxTimeoutSec || tx.getTxtimeout() > maxTxTimeoutSec) {
			ErrorCode.setError(ErrorCode.InvalidTransactionTimeout, ack, minTxTimeoutSec, maxTxTimeoutSec);
			return null;
		}
		return tx;
	}

	public Localtransaction checkLocalTransaction(Localtransaction local, Acknowledge ack, int minTxTimeoutSec,
			int maxTxTimeoutSec) {
		if (!StringUtils.hasText(local.getClientId())) {
			ErrorCode.setError(ErrorCode.MissingLocalTransactionClientId, ack);
			return null;
		}
		if (local.getTxtimeout() < minTxTimeoutSec || local.getTxtimeout() > maxTxTimeoutSec) {
			ErrorCode.setError(ErrorCode.InvalidTransactionTimeout, ack, minTxTimeoutSec, maxTxTimeoutSec);
			return null;
		}
		return local;
	}

	public ChunkReference checkChunkReference(ChunkReference chunk, Acknowledge ack) {
		if (chunk == null) {
			ErrorCode.setError(ErrorCode.MissingChunk, ack);
			return null;
		}
		if (!StringUtils.hasText(chunk.getMsgId())) {
			ErrorCode.setError(ErrorCode.MissingChunkMsgId, ack);
			return null;
		}
		if (chunk.getPos() < 0) {
			ErrorCode.setError(ErrorCode.InvalidChunkPos, ack);
			return null;
		}
		return chunk;
	}

	public Chunk checkChunk(Chunk chunk, Acknowledge ack) {
		if (chunk == null) {
			ErrorCode.setError(ErrorCode.MissingChunk, ack);
			return null;
		}
		if (chunk.getData() == null) {
			ErrorCode.setError(ErrorCode.MissingChunkData, ack);
			return null;
		}
		if (!StringUtils.hasText(chunk.getMsgId())) {
			ErrorCode.setError(ErrorCode.MissingChunkMsgId, ack);
			return null;
		}
		if (!StringUtils.hasText(chunk.getMac())) {
			ErrorCode.setError(ErrorCode.MissingChunkMac, ack);
			return null;
		}
		if (chunk.getPos() < 0) {
			ErrorCode.setError(ErrorCode.InvalidChunkPos, ack);
			return null;
		}
		return chunk;
	}

	public Chunk checkChunkMac(Chunk c, IntegratedCryptoScheme scheme, Acknowledge ack) {
		if (c == null) {
			ErrorCode.setError(ErrorCode.MissingChunk, ack);
			return null;
		}
		if (!StringUtils.hasText(c.getMac())) {
			ErrorCode.setError(ErrorCode.MissingChunkMac, ack);
			return null;
		}
		if (!SignatureUtils.checkChunkMac(c, scheme)) {
			ErrorCode.setError(ErrorCode.InvalidChunkMac, ack);
			return null;
		}
		return c;
	}

	public Header checkHeader(Header hdr, Acknowledge ack) {
		if (hdr == null) {
			ErrorCode.setError(ErrorCode.MissingHeader, ack);
			return null;
		}
		if (!StringUtils.hasText(hdr.getMsgId())) {
			ErrorCode.setError(ErrorCode.MissingHeaderMsgId, ack);
			return null;
		}
		if (checkChannel(hdr.getChannel(), ack) == null) {
			return null;
		}
		if (checkUserSignature(hdr.getUsersignature(), ack) == null) {
			return null;
		}
		if (hdr.getTo() == null) {
			ErrorCode.setError(ErrorCode.MissingHeaderTo, ack);
			return null;
		}
		if (checkUserIdentity(hdr.getTo(), ack) == null) {
			return null;
		}
		if (hdr.getTtl() == null) {
			ErrorCode.setError(ErrorCode.MissingHeaderTTL, ack);
			return null;
		}
		if (!StringUtils.hasText(hdr.getEncryptionContextId())) {
			ErrorCode.setError(ErrorCode.MissingHeaderEncryptionContextId, ack);
			return null;
		}
		if (!StringUtils.hasText(hdr.getScheme())) {
			ErrorCode.setError(ErrorCode.MissingHeaderScheme, ack);
			return null;
		}
		return hdr;
	}

	public Payload checkPayload(Payload payload, Acknowledge ack) {
		if (payload == null) {
			ErrorCode.setError(ErrorCode.MissingPayload, ack);
			return null;
		}

		if (!StringUtils.hasText(payload.getMACofMACs())) {
			ErrorCode.setError(ErrorCode.MissingPayloadChunksMACofMACs, ack);
			return null;
		}
		if (payload.getEncryptionContext() == null) {
			ErrorCode.setError(ErrorCode.MissingPayloadEncryptionContext, ack);
			return null;
		}
		if (payload.getLength() < 0) {
			ErrorCode.setError(ErrorCode.InvalidPayloadLength, ack);
			return null;
		}
		if (payload.getPlaintextLength() < 0) {
			ErrorCode.setError(ErrorCode.InvalidPlaintextLength, ack);
			return null;
		}
		return payload;
	}

	public Channel checkChannel(Channel channel, Acknowledge ack) {
		if (channel == null) {
			ErrorCode.setError(ErrorCode.MissingChannel, ack);
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
			ErrorCode.setError(ErrorCode.MissingAuthorization, ack);
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

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
