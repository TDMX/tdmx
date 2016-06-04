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
package org.tdmx.server.ws.mos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.mos.GetAddress;
import org.tdmx.core.api.v01.mos.GetAddressResponse;
import org.tdmx.core.api.v01.mos.GetChannel;
import org.tdmx.core.api.v01.mos.GetChannelResponse;
import org.tdmx.core.api.v01.mos.ListChannel;
import org.tdmx.core.api.v01.mos.ListChannelResponse;
import org.tdmx.core.api.v01.mos.Submit;
import org.tdmx.core.api.v01.mos.SubmitResponse;
import org.tdmx.core.api.v01.mos.Upload;
import org.tdmx.core.api.v01.mos.UploadResponse;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.tx.Commit;
import org.tdmx.core.api.v01.tx.CommitResponse;
import org.tdmx.core.api.v01.tx.Forget;
import org.tdmx.core.api.v01.tx.ForgetResponse;
import org.tdmx.core.api.v01.tx.Localtransaction;
import org.tdmx.core.api.v01.tx.Prepare;
import org.tdmx.core.api.v01.tx.PrepareResponse;
import org.tdmx.core.api.v01.tx.Recover;
import org.tdmx.core.api.v01.tx.RecoverResponse;
import org.tdmx.core.api.v01.tx.Rollback;
import org.tdmx.core.api.v01.tx.RollbackResponse;
import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelName;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.MessageState;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.ChannelService.SubmitMessageOperationStatus;
import org.tdmx.lib.zone.service.ChannelService.SubmitMessageResultHolder;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ros.client.RelayClientService;
import org.tdmx.server.ros.client.RelayStatus;
import org.tdmx.server.tos.client.TransferClientService;
import org.tdmx.server.tos.client.TransferStatus;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.mos.MOSServerSession.ChannelContextHolder;
import org.tdmx.server.ws.mos.MOSServerSession.DestinationContextHolder;
import org.tdmx.server.ws.mos.SenderTransactionContext.MessageContextHolder;
import org.tdmx.server.ws.security.service.AuthenticatedClientLookupService;
import org.tdmx.server.ws.security.service.AuthorizedSessionLookupService;

import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

/**
 * TODO #109: FlowQuota is checked on submit, but only used when a message is stored on prepare or one-phase commit.
 * Therefore FlowQuota has to be released and message deleted on rollback of 2pc, but only after prepare. Before
 * prepare, when a transaction is rolled-back or timed-out, no changes to the DB is required. Once prepared, a
 * transaction remains prepared until TM either commits or rolls back the transaction after recovers. Messages which are
 * prepared for sending but not committed or rolledback by a TM will eventually reach their TTL and be removed, freeing
 * their quota.
 * 
 * The sender should submit the message and then upload each chunk in sequential order until all chunks are uploaded.
 * Only the last chunk uploaded may be re-uploaded by the client. A XA transaction cannot prepare successfully unless
 * all chunks are completely uploaded for each message in the transaction.
 * 
 * With XA transactions, the messages received are written to the DB ( using flow quota ) on prepare with the
 * transaction XID. This is recoverable from the DB with the recover method. A prepared transaction is then forgotten /
 * removed from the MOS. The quota is used on prepare.
 * 
 * After committing ( one phase or two ) the message relay is initiated without changing quota.
 * 
 * Rollback of a prepared XA transaction requires freeing the quota and deleting the channel messages.
 * 
 * @author Peter
 *
 */
public class MOSImpl implements MOS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MOSImpl.class);

	private AuthorizedSessionLookupService<MOSServerSession> authorizedSessionService;
	private AuthenticatedClientLookupService authenticatedClientService;

	private RelayClientService relayClientService;
	private TransferClientService transferClientService;

	private DomainService domainService;
	private AddressService addressService;
	private ServiceService serviceService;
	private ChannelService channelService;
	private DestinationService destinationService;

	private AgentCredentialFactory credentialFactory;
	private AgentCredentialService credentialService;
	private AgentCredentialValidator credentialValidator;

	private ChunkService chunkService;

	private final DomainToApiMapper d2a = new DomainToApiMapper();
	private final ApiToDomainMapper a2d = new ApiToDomainMapper();
	private final ApiValidator validator = new ApiValidator();

	private int batchSize = 100;

	private ScheduledExecutorService txTimeoutScheduler = Executors.newScheduledThreadPool(1,
			new RenamingThreadFactoryProxy("SenderTxTimeoutScheduler", Executors.defaultThreadFactory()));

	private static final String NON_TX_SPEC_ID_PREFIX = "non-tx:";

	private int maxTransactionTimeoutSec = 3600 * 8; // 8hrs
	private int minTransactionTimeoutSec = 60; // 60s

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		PrepareResponse response = new PrepareResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		SenderTransactionContext stc = session.getTransactionContext(parameters.getXid());
		if (stc == null) {
			ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
			return response;
		}
		List<ChannelMessage> sentMessages = new ArrayList<>();
		try {
			for (Channel ch : stc.getChannels()) {
				List<ChannelMessage> msgs = stc.getChannelMessages(ch);

				channelService.twoPhasePrepareSend(session.getZone(), ch, msgs, stc.getTxSpec().getXid());
				// messages changed by the sending to be ready for relay.
				sentMessages.addAll(msgs);
			}
		} finally {
			discardTx(stc, session);
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		CommitResponse response = new CommitResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		SenderTransactionContext stc = session.getTransactionContext(parameters.getXid());
		if (stc != null) {
			if (parameters.isOnePhase()) {
				List<MessageState> states = onePhaseCommitTx(stc, session);
				relayOrTransferMessages(session, states);
			} else {
				// 2pc we should not have found the stc.
				ErrorCode.setError(ErrorCode.XATransactionNotPrepared, response);
				return response;
			}
		} else {
			List<MessageState> states = channelService.twoPhaseCommitSend(session.getZone(), parameters.getXid());
			if (states.isEmpty()) {
				ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
				return response;
			}
			relayOrTransferMessages(session, states);
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		RollbackResponse response = new RollbackResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		SenderTransactionContext stc = session.getTransactionContext(parameters.getXid());
		if (stc != null) {
			// rollback of a tx which is not yet prepared.
			discardTx(stc, session);
		} else {
			List<MessageState> states = channelService.twoPhaseRollbackSend(session.getZone(), parameters.getXid());
			if (states.isEmpty()) {
				ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
				return response;
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		ForgetResponse response = new ForgetResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		SenderTransactionContext stc = session.getTransactionContext(parameters.getXid());
		if (stc != null) {
			// can't forget a tx which is not yet prepared.
			ErrorCode.setError(ErrorCode.XATransactionNotPrepared, response);
			return response;
		} else {
			// forget is equivalent to rollback since we don't do heuristic commiting or rolling back ourselves.
			List<MessageState> states = channelService.twoPhaseRollbackSend(session.getZone(), parameters.getXid());
			if (states.isEmpty()) {
				ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
				return response;
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		ChannelOrigin co = new ChannelOrigin();
		co.setLocalName(session.getOriginatingAddress().getLocalName());
		co.setDomainName(session.getDomain().getDomainName());

		RecoverResponse response = new RecoverResponse();
		List<String> preparedXids = channelService.twoPhaseRecover(session.getZone(), co);

		response.getXids().addAll(preparedXids);
		response.setSuccess(true);
		return response;
	}

	@Override
	public GetAddressResponse getAddress(GetAddress parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();
		Address oa = session.getOriginatingAddress();
		Domain domain = session.getDomain();

		GetAddressResponse response = new GetAddressResponse();

		ChannelEndpoint ep = new ChannelEndpoint();
		ep.setDomain(domain.getDomainName());
		ep.setLocalname(oa.getLocalName());
		response.setOrigin(ep);

		response.setSuccess(true);
		return response;
	}

	@Override
	public SubmitResponse submit(Submit parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();
		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();

		SubmitResponse response = new SubmitResponse();

		// validate Msg fields present ( payload, header and chunk )
		if (validator.checkMessage(parameters.getMsg(), response, true) == null) {
			return response;
		}

		if (!validator.checkTransactionChoice(parameters.getTransaction(), parameters.getLocaltransaction(),
				minTransactionTimeoutSec, maxTransactionTimeoutSec, response)) {
			return response;
		}
		Transaction tx = parameters.getTransaction();
		if (tx == null) {
			tx = getNonTransactionSpecification(parameters.getLocaltransaction());
		}

		Msg msg = parameters.getMsg();
		Header header = msg.getHeader();
		Payload payload = msg.getPayload();
		if (!SignatureUtils.checkMsgId(header, payload, header.getUsersignature().getSignaturevalue().getTimestamp())) {
			ErrorCode.setError(ErrorCode.InvalidMsgId, response);
			return response;
		}
		if (!SignatureUtils.checkMessageSignature(header, payload)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureMessage, response);
			return response;
		}

		// validate Chunk fields present
		if (validator.checkChunk(msg.getChunk(), response) == null) {
			return response;
		}
		Chunk c = a2d.mapChunk(msg.getChunk());

		ChannelMessage m = a2d.mapMessage(msg);

		// check chunk's mac
		if (validator.checkChunkMac(msg.getChunk(), m.getScheme(), response) == null) {
			return response;
		}

		AgentCredentialDescriptor srcUc = credentialFactory.createAgentCredential(
				header.getUsersignature().getUserIdentity().getUsercertificate(),
				header.getUsersignature().getUserIdentity().getDomaincertificate(),
				header.getUsersignature().getUserIdentity().getRootcertificate());
		if (srcUc == null || AgentCredentialType.UC != srcUc.getCredentialType()) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		// check origin cert is same as msg channel origin cert.
		if (!srcUc.getFingerprint().equals(authorizedUser.getFingerprint())) {
			ErrorCode.setError(ErrorCode.InvalidMessageSource, response);
			return response;
		}

		AgentCredentialDescriptor dstUc = credentialFactory.createAgentCredential(header.getTo().getUsercertificate(),
				header.getTo().getDomaincertificate(), header.getTo().getRootcertificate());
		if (dstUc == null || AgentCredentialType.UC != dstUc.getCredentialType()) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		// create originating ChannelMessage
		Zone zone = session.getZone();

		// cache the last used Channel in the session to avoid this search if always sending to the same dest.
		ChannelName cn = a2d.mapChannelName(header.getChannel());
		if (!srcUc.getDomainName().equals(cn.getOrigin().getDomainName())
				|| !srcUc.getAddressName().equals(cn.getOrigin().getLocalName())) {
			// check sender cert matches origin of channel sending on.
			ErrorCode.setError(ErrorCode.InvalidChannelOrigin, response);
			return response;
		}
		if (!dstUc.getDomainName().equals(cn.getDestination().getDomainName())
				|| !dstUc.getAddressName().equals(cn.getDestination().getLocalName())) {
			// header channel dest matches the "to" User
			ErrorCode.setError(ErrorCode.InvalidChannelDestination, response);
			return response;
		}

		final String channelKey = cn.getChannelKey(cn.getOrigin().getDomainName());
		ChannelContextHolder cch = session.getChannelContext(channelKey);
		if (cch == null) {
			ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			sc.setDomainName(authorizedUser.getTdmxDomainName());
			sc.getOrigin().setDomainName(header.getChannel().getOrigin().getDomain());
			sc.getOrigin().setLocalName(header.getChannel().getOrigin().getLocalname());
			sc.getDestination().setDomainName(header.getChannel().getDestination().getDomain());
			sc.getDestination().setLocalName(header.getChannel().getDestination().getLocalname());
			sc.getDestination().setServiceName(header.getChannel().getDestination().getServicename());

			List<Channel> channels = channelService.search(zone, sc);
			if (channels.isEmpty()) {
				ErrorCode.setError(ErrorCode.ChannelNotFound, response);
				return response;
			}
			Channel channel = channels.get(0);

			// initialize session's ChannelContextHolder to cache the ROS info
			cch = new ChannelContextHolder(channelKey, channel);
			session.setChannelContext(channelKey, cch);

			// initialize session's DestinationContextHolder to cache the TOS info (only for same domain channels)
			if (channel.isSameDomain() && null == session.getDestinationContext(cn.getDestinationName())) {
				DestinationContextHolder ddh = new DestinationContextHolder(cn.getDestinationName(),
						cn.getDestination());
				session.setDestinationContext(cn.getDestinationName(), ddh);
			}
		}

		Channel cachedChannel = cch.getChannel();
		m.setChannel(cachedChannel);

		// create the message state and link with the message.
		m.initMessageState(zone, MessageStatus.NEW, srcUc.getSerialNumber(), dstUc.getSerialNumber());

		// adds the tx to the session immediately.
		SenderTransactionContext stc = startOrContinueTx(session, tx, response);
		if (stc == null) {
			return response;
		}
		// add the message to the tx
		MessageContextHolder mch = new MessageContextHolder(m);
		stc.addMessage(mch);

		// check authorization and flow control
		long totalChannelQuota = m.getPayloadLength() + stc.getTotalPayloadSizeForChannel(cachedChannel);
		SubmitMessageResultHolder quotaCheck = channelService.checkChannelQuota(zone, cachedChannel,
				m.getPayloadLength(), totalChannelQuota);
		if (quotaCheck.status != null) {
			ErrorCode.setError(mapSubmitOperationStatus(quotaCheck.status), response);
			return response;
		}

		// persist Chunk
		chunkService.createOrUpdate(c);

		// give the caller the continuationId for the next chunk
		String continuationId = mch.getContinuationId(c.getPos() + 1);
		if (continuationId == null && isNonTransactionSpecification(stc.getTxSpec())) {
			// last chunk - auto-commit if we have no tx.
			List<MessageState> msgs = onePhaseCommitTx(stc, session);
			relayOrTransferMessages(session, msgs);
		}

		response.setContinuation(continuationId);
		response.setSuccess(true);
		return response;
	}

	@Override
	public UploadResponse upload(Upload parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		UploadResponse response = new UploadResponse();

		String continuationId = parameters.getContinuation();
		if (!StringUtils.hasText(continuationId)) {
			ErrorCode.setError(ErrorCode.MissingChunkContinuationId, response);
			return response;
		}

		// validate Chunk fields present
		if (validator.checkChunk(parameters.getChunk(), response) == null) {
			return response;
		}

		Chunk c = a2d.mapChunk(parameters.getChunk());

		SenderTransactionContext stc = session.getTransactionByMsgId(parameters.getChunk().getMsgId());
		if (stc == null) {
			ErrorCode.setError(ErrorCode.MessageNotFound, response);
			return response;
		}
		MessageContextHolder cmh = stc.getMessage(parameters.getChunk().getMsgId());
		// calculate the continuationId for the chunk and check that it matches the continuationId
		if (!continuationId.equals(cmh.getContinuationId(c.getPos()))) {
			ErrorCode.setError(ErrorCode.InvalidChunkContinuationId, response);
			return response;
		}
		if (c.getPos() < cmh.getLastChunkReceived() || c.getPos() > cmh.getLastChunkReceived() + 1) {
			ErrorCode.setError(ErrorCode.InvalidChunkOrder, response);
			return response;
		}

		ChannelMessage m = cmh.getMsg();
		ChannelName cn = m.getChannel().getChannelName();
		final String channelKey = cn.getChannelKey(cn.getOrigin().getDomainName());
		ChannelContextHolder cch = session.getChannelContext(channelKey);
		if (cch == null) {
			ErrorCode.setError(ErrorCode.ChannelNotFound, response);
			return response;
		}
		// check chunk's mac
		if (validator.checkChunkMac(parameters.getChunk(), m.getScheme(), response) == null) {
			return response;
		}

		chunkService.createOrUpdate(c);

		// calculate the next continuationId
		String nextContinuationId = cmh.getContinuationId(c.getPos() + 1);
		if (nextContinuationId == null) {
			// this was the last chunk
			if (isNonTransactionSpecification(stc.getTxSpec())) {
				List<MessageState> states = onePhaseCommitTx(stc, session);
				relayOrTransferMessages(session, states);
			}
		}
		// record the last successful uploaded so we allow only it again or the next.
		cmh.setLastChunkReceived(c.getPos());
		response.setContinuation(nextContinuationId);

		response.setSuccess(true);
		return response;
	}

	@Override
	public GetChannelResponse getChannel(GetChannel parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		GetChannelResponse response = new GetChannelResponse();

		if (validator.checkChannelDestination(parameters.getDestination(), response) == null) {
			return response;
		}

		Zone zone = session.getZone();
		Address address = session.getOriginatingAddress();
		Domain domain = session.getDomain();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		sc.setDomain(domain);

		sc.getOrigin().setLocalName(address.getLocalName());
		sc.getOrigin().setDomainName(domain.getDomainName());
		sc.getDestination().setDomainName(parameters.getDestination().getDomain());
		sc.getDestination().setLocalName(parameters.getDestination().getLocalname());
		sc.getDestination().setServiceName(parameters.getDestination().getServicename());

		List<Channel> channels = channelService.search(zone, sc);
		for (Channel c : channels) {
			// only 1
			response.setChannelinfo(d2a.mapChannelInfo(c));
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public ListChannelResponse listChannel(ListChannel parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		ListChannelResponse response = new ListChannelResponse();

		Zone zone = session.getZone();
		Domain domain = session.getDomain();
		Address address = session.getOriginatingAddress();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));
		sc.setDomain(domain);

		sc.getOrigin().setLocalName(address.getLocalName());
		sc.getOrigin().setDomainName(domain.getDomainName());
		if (parameters.getDestination() != null) {
			sc.getDestination().setDomainName(parameters.getDestination().getDomain());
			sc.getDestination().setLocalName(parameters.getDestination().getLocalname());
			sc.getDestination().setServiceName(parameters.getDestination().getServicename());
		}
		List<Channel> channels = channelService.search(zone, sc);
		for (Channel c : channels) {
			response.getChannelinfos().add(d2a.mapChannelInfo(c));
		}

		response.setSuccess(true);
		return response;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private ErrorCode mapSubmitOperationStatus(SubmitMessageOperationStatus status) {
		switch (status) {
		case FLOW_CONTROL_CLOSED:
			return ErrorCode.SubmitFlowControlClosed;
		case CHANNEL_CLOSED:
			return ErrorCode.SubmitChannelClosed;
		case NOT_ENOUGH_QUOTA_AVAILABLE:
			return ErrorCode.SubmitQuotaNotSufficient;
		case MESSAGE_TOO_LARGE:
			return ErrorCode.SubmitMessageTooLarge;
		default:
			return null;
		}
	}

	private void relayOrTransferMessages(MOSServerSession session, List<MessageState> states) {
		for (MessageState state : states) {
			final ChannelName cn = state.getChannelName();

			if (MessageStatus.SUBMITTED == state.getStatus()) {
				final String channelKey = cn.getChannelKey(cn.getOrigin().getDomainName());
				ChannelContextHolder cch = session.getChannelContext(channelKey);

				// give the message to the ROS to relay.
				relayWithRetry(session, cch, state);

			} else if (MessageStatus.READY == state.getStatus()) {
				DestinationContextHolder ddh = session.getDestinationContext(cn.getDestinationName());

				// same domain so transfer to the receiver.
				transferReceiver(session, ddh, state);
			} else {
				log.warn("Unexpected message state for relay. " + state);
			}
		}
	}

	private List<MessageState> onePhaseCommitTx(SenderTransactionContext stc, MOSServerSession session) {
		List<MessageState> sentMessages = new ArrayList<>();
		try {
			for (Channel ch : stc.getChannels()) {
				List<ChannelMessage> msgs = stc.getChannelMessages(ch);

				channelService.onePhaseCommitSend(session.getZone(), ch, msgs);
				// messages changed by the sending to be ready for relay.
				for (ChannelMessage msg : msgs) {
					sentMessages.add(msg.getState());
				}
			}
		} finally {
			discardTx(stc, session);
		}
		return sentMessages;
	}

	private void discardTx(SenderTransactionContext stc, MOSServerSession session) {
		ScheduledFuture<?> timeout = stc.getTimeoutFuture();
		if (timeout != null) {
			timeout.cancel(false);
		}
		session.removeTransactionContext(stc.getXid());
	}

	private SenderTransactionContext startOrContinueTx(MOSServerSession session, Transaction tx, SubmitResponse ack) {
		SenderTransactionContext stc = session.getTransactionContext(tx.getXid());
		if (stc != null && isNonTransactionSpecification(tx)) {
			// continuation of a tx
			// a local transaction should have finished before we start the next - so we discard the current
			// incomplete
			log.warn("Local transaction discarded " + tx.getXid());
			// TODO #112: raise client incident
			discardTx(stc, session);
			stc = null;
		}
		if (stc != null && stc.getTimeoutFuture().isDone()) {
			// can only happen with a race condition
			ErrorCode.setError(ErrorCode.XATransactionTimeout, ack, tx.getXid());
			// TODO #112: raise client incident
			discardTx(stc, session);
			return null;
		}
		if (stc == null) {
			stc = new SenderTransactionContext(tx);
			session.setTransactionContext(tx.getXid(), stc);
			scheduleTransactionTimeout(stc, session);
			log.debug("Starting new transaction " + tx.getXid());
		} else {
			log.debug("Continuing existing transaction " + tx.getXid());
		}
		return stc;
	}

	private void scheduleTransactionTimeout(SenderTransactionContext stc, MOSServerSession session) {
		// transaction will timeout after the tx timeout seconds after starting the tx.
		ScheduledFuture<?> timeoutFuture = txTimeoutScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				// we simply forget the transaction if it times-out.
				log.info("Timeout of " + stc.getTxSpec().getXid());
				discardTx(stc, session);
			}
		}, stc.getTxSpec().getTxtimeout(), TimeUnit.SECONDS);
		// we need to record the future object so we can cancel later ( when tx completes )
		stc.setTimeoutFuture(timeoutFuture);
	}

	private void transferReceiver(MOSServerSession session, DestinationContextHolder ddh, MessageState state) {
		// transfer message to MDS for same domain receiver to take.
		// we don't retry since the receiver finds the messages from the DB if we don't fast transfer with this TOS
		// mechanism.
		final TransferStatus ts = transferClientService.transferMDS(ddh.getTosTcpAddress(), ddh.getSessionId(),
				session.getAccountZone(), ddh.getDestination(), state.getId());
		if (!ts.isSuccess()) {
			// we clear the not working TOS address of the MDS and it's session
			if (ts.getErrorCode() != org.tdmx.server.tos.client.TransferStatus.ErrorCode.PCS_SESSION_NOT_FOUND) {
				// PCS session not found is normal if there is no active receiver.
				log.warn("MOS->MDS message transfer failure  " + ts);
			}
			ddh.setSessionId(null);
			ddh.setTosTcpAddress(null);
		} else {
			// possibly changed
			ddh.setTosTcpAddress(ts.getTosTcpAddress());
			ddh.setSessionId(ts.getSessionId());
		}
	}

	/**
	 * Relay to the ROS, retrying if there are retryable errors. The ChannelContextHolder caches the last good
	 * RosTcpAddress.
	 * 
	 * @param session
	 * @param cch
	 * @param state
	 */
	private void relayWithRetry(MOSServerSession session, ChannelContextHolder cch, MessageState state) {
		final RelayStatus rs = relayClientService.relayChannelMessage(cch.getRosTcpAddress(), session.getAccountZone(),
				session.getZone(), cch.getChannel().getDomain(), cch.getChannel(), state);
		if (!rs.isSuccess()) {
			// we 'clear' a possibly wrong ROS address
			cch.setRosTcpAddress(null);
			if (rs.getErrorCode().isRetryable()) {
				RelayStatus retry = relayClientService.relayChannelMessage(null /* null get new ROS session */,
						session.getAccountZone(), session.getZone(), cch.getChannel().getDomain(), cch.getChannel(),
						state);
				if (!retry.isSuccess()) {
					ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
							rs.getErrorCode().getErrorMessage());
					channelService.updateMessageProcessingState(state.getId(), error);
				} else {
					// cache the potentially changed ROS address
					cch.setRosTcpAddress(retry.getRosTcpAddress());
				}
			} else {
				ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
						rs.getErrorCode().getErrorMessage());
				channelService.updateMessageProcessingState(state.getId(), error);
			}

		} else {
			// cache the working ROS address
			cch.setRosTcpAddress(rs.getRosTcpAddress());
		}
	}

	private Transaction getNonTransactionSpecification(Localtransaction local) {
		Transaction tx = new Transaction();
		tx.setTxtimeout(local.getTxtimeout());
		tx.setXid(NON_TX_SPEC_ID_PREFIX + local.getClientId());
		return tx;
	}

	private boolean isNonTransactionSpecification(Transaction txSpec) {
		return txSpec.getXid().startsWith(NON_TX_SPEC_ID_PREFIX);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AuthorizedSessionLookupService<MOSServerSession> getAuthorizedSessionService() {
		return authorizedSessionService;
	}

	public void setAuthorizedSessionService(AuthorizedSessionLookupService<MOSServerSession> authorizedSessionService) {
		this.authorizedSessionService = authorizedSessionService;
	}

	public AuthenticatedClientLookupService getAuthenticatedClientService() {
		return authenticatedClientService;
	}

	public void setAuthenticatedClientService(AuthenticatedClientLookupService authenticatedClientService) {
		this.authenticatedClientService = authenticatedClientService;
	}

	public RelayClientService getRelayClientService() {
		return relayClientService;
	}

	public void setRelayClientService(RelayClientService relayClientService) {
		this.relayClientService = relayClientService;
	}

	public TransferClientService getTransferClientService() {
		return transferClientService;
	}

	public void setTransferClientService(TransferClientService transferClientService) {
		this.transferClientService = transferClientService;
	}

	public DomainService getDomainService() {
		return domainService;
	}

	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	public AddressService getAddressService() {
		return addressService;
	}

	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}

	public ServiceService getServiceService() {
		return serviceService;
	}

	public void setServiceService(ServiceService serviceService) {
		this.serviceService = serviceService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public DestinationService getDestinationService() {
		return destinationService;
	}

	public void setDestinationService(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

	public AgentCredentialFactory getCredentialFactory() {
		return credentialFactory;
	}

	public void setCredentialFactory(AgentCredentialFactory credentialFactory) {
		this.credentialFactory = credentialFactory;
	}

	public AgentCredentialService getCredentialService() {
		return credentialService;
	}

	public void setCredentialService(AgentCredentialService credentialService) {
		this.credentialService = credentialService;
	}

	public AgentCredentialValidator getCredentialValidator() {
		return credentialValidator;
	}

	public void setCredentialValidator(AgentCredentialValidator credentialValidator) {
		this.credentialValidator = credentialValidator;
	}

	public ChunkService getChunkService() {
		return chunkService;
	}

	public void setChunkService(ChunkService chunkService) {
		this.chunkService = chunkService;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
