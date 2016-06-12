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
package org.tdmx.server.ws.mds;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.mds.Acknowledge;
import org.tdmx.core.api.v01.mds.AcknowledgeResponse;
import org.tdmx.core.api.v01.mds.Download;
import org.tdmx.core.api.v01.mds.DownloadResponse;
import org.tdmx.core.api.v01.mds.GetDestinationSession;
import org.tdmx.core.api.v01.mds.GetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ListChannel;
import org.tdmx.core.api.v01.mds.ListChannelResponse;
import org.tdmx.core.api.v01.mds.Receive;
import org.tdmx.core.api.v01.mds.ReceiveResponse;
import org.tdmx.core.api.v01.mds.SetDestinationSession;
import org.tdmx.core.api.v01.mds.SetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.ChunkReference;
import org.tdmx.core.api.v01.msg.Destinationinfo;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Msg;
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
import org.tdmx.lib.chunk.domain.Chunk;
import org.tdmx.lib.chunk.service.ChunkService;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.MessageState;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.MessageStatusSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.ChannelService.ReceiveMessageResultHolder;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ros.client.RelayClientService;
import org.tdmx.server.ros.client.RelayStatus;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientLookupService;
import org.tdmx.server.ws.security.service.AuthorizedSessionLookupService;

import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

public class MDSImpl implements MDS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MDSImpl.class);

	private AuthorizedSessionLookupService<MDSServerSession> authorizedSessionService;
	private AuthenticatedClientLookupService authenticatedClientService;

	private RelayClientService relayClientService;

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
	private int maxWaitTimeoutSec = 600;

	private int maxTransactionTimeoutSec = 3600 * 8; // 8hrs
	private int minTransactionTimeoutSec = 60; // 60s

	private ScheduledExecutorService txTimeoutScheduler = Executors.newScheduledThreadPool(1,
			new RenamingThreadFactoryProxy("ReceiveTxTimeoutScheduler", Executors.defaultThreadFactory()));

	private static final String NON_TX_SPEC_ID_PREFIX = "non-tx:";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public GetDestinationSessionResponse getDestinationSession(GetDestinationSession parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		GetDestinationSessionResponse response = new GetDestinationSessionResponse();

		Address address = session.getDestinationAddress();
		Domain domain = session.getDomain();
		Service service = session.getService();

		Destination dest = destinationService.findByDestination(address, service);

		Destinationinfo info = new Destinationinfo();
		info.setDomain(domain.getDomainName());
		info.setLocalname(address.getLocalName());
		info.setServicename(service.getServiceName());
		if (dest != null) {
			info.setDestinationsession(d2a.mapDestinationSession(dest.getDestinationSession()));
		}
		response.setDestination(info);

		response.setSuccess(true);
		return response;
	}

	@Override
	public SetDestinationSessionResponse setDestinationSession(SetDestinationSession parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();
		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		SetDestinationSessionResponse response = new SetDestinationSessionResponse();

		// validate all DestinationSession fields are specified.
		Destinationsession ds = validator.checkDestinationsession(parameters.getDestinationsession(), response);
		if (ds == null) {
			return response;
		}

		Address address = session.getDestinationAddress();
		Domain domain = session.getDomain();
		Service service = session.getService();

		// check authUser is ds.signer
		PKIXCertificate uc = CertificateIOUtils
				.safeDecodeX509(ds.getUsersignature().getUserIdentity().getUsercertificate());
		if (uc == null || !authorizedUser.isIdentical(uc)) {
			ErrorCode.setError(ErrorCode.InvalidSignerDestinationSession, response);
			return response;
		}

		// check that the FTS signature is ok for the targetagent.
		if (!SignatureUtils.checkDestinationSessionSignature(service.getServiceName(), ds)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureDestinationSession, response);
			return response;
		}

		// lookup existing service exists
		Destination dest = a2d.mapDestination(address, service, ds);
		destinationService.setSession(dest);

		Zone zone = session.getZone();

		boolean more = true;
		// fetch ALL Channels which have this Destination as Destination.
		for (int pageNo = 0; more; pageNo++) {
			ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
					new PageSpecifier(pageNo, getBatchSize()));
			sc.setDomain(domain);
			sc.getDestination().setLocalName(address.getLocalName());
			sc.getDestination().setDomainName(domain.getDomainName());
			sc.getDestination().setServiceName(service.getServiceName());

			List<Channel> channels = channelService.search(zone, sc);
			for (Channel channel : channels) {
				Channel updatedChannel = channelService.setChannelDestinationSession(zone, channel.getId(),
						dest.getDestinationSession());

				if (ProcessingStatus.PENDING == updatedChannel.getProcessingState().getStatus()) {
					// relay "pending" channel destination sessions back to origin
					// caching the ROS addresses per channel known to the destination
					relayCDSWithRetry(session, rcv, updatedChannel);
				}
			}
			if (channels.isEmpty()) {
				more = false;
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public ReceiveResponse receive(Receive recvRequest) {
		ReceiveResponse response = new ReceiveResponse();

		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		// validate waitTimeout(sec) postive < some maximum
		if (recvRequest.getWaitTimeoutSec() < 0 || recvRequest.getWaitTimeoutSec() > maxWaitTimeoutSec) {
			ErrorCode.setError(ErrorCode.InvalidTimeout, response, maxWaitTimeoutSec);
			return response;
		}

		Transaction tx = null;
		if (recvRequest.getTransaction() != null) {
			// validate XA tx
			tx = validator.checkTransaction(recvRequest.getTransaction(), response, minTransactionTimeoutSec,
					maxTransactionTimeoutSec);
			if (tx == null) {
				return response;
			}
			if (recvRequest.getLocaltransaction() != null) {
				ErrorCode.setError(ErrorCode.InvalidReceiveAcknowledgeMode, response);
				return response;
			}
		} else if (recvRequest.getLocaltransaction() != null) {
			// validate NonTransaction info from api
			Localtransaction ack = validator.checkLocalTransaction(recvRequest.getLocaltransaction(), response,
					minTransactionTimeoutSec, maxTransactionTimeoutSec);
			if (ack == null) {
				return response;
			}
			tx = getNonTransactionSpecification(ack);

			// handle the non transactional ACK of previously received message
			TransactionContext txCtx = rcv.getTransaction(tx.getXid());
			if (txCtx != null) {
				// we are auto acking the last received message in the previous transaction
				MessageContext msgCtx = txCtx.getCurrentMessage();
				if (!StringUtils.hasText(recvRequest.getMsgId())) {
					// abort existing tx, since same client now requests new receive without acking old msg received.
					ReceiveMessageResultHolder ackStatus = channelService.onePhaseRollbackReceive(session.getZone(),
							msgCtx.getMsg());
					if (ackStatus.flowControlOpened) {
						// relay opened FC back to origin
						relayFCWithRetry(session, rcv, msgCtx.getMsg().getChannel(), ackStatus.flowQuota);
					}
					finishTx(rcv, txCtx);

				} else if (!recvRequest.getMsgId().equals(msgCtx.getMsgId())) {
					// ack message not received
					ErrorCode.setError(ErrorCode.InvalidAcknowledgeNotReceived, response);
					return response;
				} else {
					// acknowledge the message, possibly opening the relay flow control
					ReceiveMessageResultHolder ackStatus = channelService.onePhaseCommitReceive(session.getZone(),
							msgCtx.getMsg());
					// TODO #107: delete chunks.
					if (ackStatus.flowControlOpened) {
						// relay opened FC back to origin
						relayFCWithRetry(session, rcv, msgCtx.getMsg().getChannel(), ackStatus.flowQuota);
					}
					finishTx(rcv, txCtx);
				}
			} else if (StringUtils.hasText(recvRequest.getMsgId())) {
				// we don't have a transaction existing
				// acking a message which we don't know
				ErrorCode.setError(ErrorCode.InvalidAcknowledgeNoReceive, response);
				return response;
			}

		} else {
			// must be tx or non tx not neither
			ErrorCode.setError(ErrorCode.MissingReceiveAcknowledgeMode, response);
			return response;
		}

		boolean mustFetch = rcv.isFetchRequired();
		if (mustFetch) {
			// fetch next batch of messages
			MessageStatusSearchCriteria criteria = new MessageStatusSearchCriteria(new PageSpecifier(0, batchSize));
			criteria.getDestination().setLocalName(session.getDestinationAddress().getLocalName());
			criteria.getDestination().setDomainName(session.getDomain().getDomainName());
			criteria.getDestination().setServiceName(session.getService().getServiceName());
			criteria.setDestinationSerialNr(authorizedUser.getSerialNumber());
			criteria.setMessageStatus(MessageStatus.READY);
			criteria.setProcessingStatus(ProcessingStatus.NONE);
			List<Long> pendingStatusIds = channelService.getStatusReferences(session.getZone(), criteria, batchSize);
			rcv.addPendingMessages(pendingStatusIds, pendingStatusIds.size() == batchSize);
		}

		// get any ready message, waiting if none available.
		Long stateId = rcv.getNextPendingStateId(recvRequest.getWaitTimeoutSec() * 1000);
		if (stateId == null) {
			// no message available even after waiting.
			response.setSuccess(true);
			return response;
		}

		// fetch the message data for reply
		ReceiveMessageResultHolder recvMsg = channelService.receiveMessage(stateId, tx.getTxtimeout());
		if (recvMsg == null) {
			// not valid after fetching.
			response.setSuccess(true);
			return response;
		}

		if (recvMsg.flowControlOpened) {
			// relay opened FC back to origin
			relayFCWithRetry(session, rcv, recvMsg.msg.getChannel(), recvMsg.flowQuota);
		}
		if (MessageStatus.RECEIVING != recvMsg.msg.getState().getStatus()) {
			// message redelivered too many times
			response.setSuccess(true);
			return response;
		}
		if (rcv.getUnackedMessage(recvMsg.msg.getMsgId()) != null) {
			// strange error / race condition - don't bother the receiver - consumed the stateId
			log.info("Found pending message doesn't fit receive criteria. " + recvMsg.msg);
			response.setSuccess(true);
			return response;
		}
		ChannelMessage msg = recvMsg.msg;

		MessageContext msgCtx = startTx(rcv, tx, msg);
		Msg m = d2a.mapChannelMessage(msg);

		// get 1st chunk and map it into msg
		Chunk chunk = chunkService.fetchChunk(msg, 0);
		if (chunk == null) {
			ErrorCode.setError(ErrorCode.ChunkDataLost, response, msg.getMsgId(), 0);
			return response;
		}
		m.setChunk(d2a.mapChunk(chunk));

		response.setMsg(m);
		response.setRetryCount(msg.getState().getDeliveryCount() - 1);
		response.setSuccess(true);
		if (msg.getNumberOfChunks() > 1) {
			// set the continuationId for the next chunk.
			response.setContinuation(msgCtx.getContinuationId(chunk.getPos() + 1));
		}
		return response;
	}

	@Override
	public DownloadResponse download(Download downloadRequest) {
		DownloadResponse response = new DownloadResponse();

		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		// validate chunk ref
		if (!StringUtils.hasText(downloadRequest.getContinuation())) {
			ErrorCode.setError(ErrorCode.MissingChunkContinuationId, response);
			return response;
		}
		ChunkReference cr = validator.checkChunkReference(downloadRequest.getChunkref(), response);
		if (cr == null) {
			return response;
		}

		MessageContext msg = rcv.getUnackedMessage(cr.getMsgId());
		if (msg == null) {
			ErrorCode.setError(ErrorCode.InvalidMsgId, response);
			return response;
		}

		// check continuationId matches.
		if (!downloadRequest.getContinuation().equals(msg.getContinuationId(cr.getPos()))) {
			ErrorCode.setError(ErrorCode.InvalidChunkContinuationId, response);
			return response;
		}

		// get next chunk and map it into msg
		Chunk chunk = chunkService.fetchChunk(msg.getMsg(), cr.getPos());
		if (chunk == null) {
			ErrorCode.setError(ErrorCode.ChunkDataLost, response, msg.getMsgId(),
					downloadRequest.getChunkref().getPos());
			return response;
		}
		response.setChunk(d2a.mapChunk(chunk));
		response.setContinuation(msg.getContinuationId(cr.getPos() + 1));
		return response;
	}

	@Override
	public AcknowledgeResponse acknowledge(Acknowledge ackRequest) {
		AcknowledgeResponse response = new AcknowledgeResponse();

		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		if (!StringUtils.hasText(ackRequest.getClientId())) {
			ErrorCode.setError(ErrorCode.MissingLocalTransactionClientId, response);
			return response;
		}
		if (!StringUtils.hasText(ackRequest.getMsgId())) {
			ErrorCode.setError(ErrorCode.MissingAcknowledgeMsgId, response);
			return response;
		}

		// handle the non transactional ACK of previously received message
		TransactionContext txCtx = rcv.getTransaction(getNonTransactionXid(ackRequest.getClientId()));
		if (txCtx != null) {
			// we are auto acking the last received message in the previous transaction
			MessageContext msgCtx = txCtx.getCurrentMessage();
			if (!ackRequest.getMsgId().equals(msgCtx.getMsgId())) {
				// ack message not received
				ErrorCode.setError(ErrorCode.InvalidAcknowledgeNotReceived, response);
				return response;
			} else {
				// acknowledge the message, possibly opening the relay flow control
				ReceiveMessageResultHolder ackStatus = channelService.onePhaseCommitReceive(session.getZone(),
						msgCtx.getMsg());
				// TODO #107: delete messag chunks (async).
				if (ackStatus.flowControlOpened) {
					// relay opened FC back to origin
					relayFCWithRetry(session, rcv, msgCtx.getMsg().getChannel(), ackStatus.flowQuota);
				}
			}
			finishTx(rcv, txCtx);

		} else if (StringUtils.hasText(ackRequest.getMsgId())) {
			// we don't have a transaction existing
			// acking a message which we don't know
			ErrorCode.setError(ErrorCode.InvalidAcknowledgeNoReceive, response);
			return response;
		}
		response.setSuccess(true);
		return response;
	}

	@Override
	public ListChannelResponse listChannel(ListChannel parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();
		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();

		ListChannelResponse response = new ListChannelResponse();

		Service service = session.getService();
		Zone zone = session.getZone();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));
		sc.setDomainName(authorizedUser.getTdmxDomainName());
		sc.getDestination().setLocalName(authorizedUser.getTdmxUserName());
		sc.getDestination().setDomainName(authorizedUser.getTdmxDomainName());
		sc.getDestination().setServiceName(service.getServiceName());
		if (parameters.getOrigin() != null) {
			sc.getOrigin().setDomainName(parameters.getOrigin().getDomain());
			sc.getOrigin().setLocalName(parameters.getOrigin().getLocalname());
		}
		List<Channel> channels = channelService.search(zone, sc);
		for (Channel c : channels) {
			response.getChannelinfos().add(d2a.mapChannelInfo(c));
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		PrepareResponse response = new PrepareResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		TransactionContext txCtx = rcv.getTransaction(parameters.getXid());
		if (txCtx == null) {
			ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
			return response;
		}
		MessageContext msgCtx = txCtx.getCurrentMessage();
		try {
			ReceiveMessageResultHolder ackStatus = channelService.twoPhasePrepareReceive(session.getZone(),
					msgCtx.getMsg(), txCtx.getTxId());

			if (ackStatus.flowControlOpened) {
				// relay opened FC back to origin
				relayFCWithRetry(session, rcv, msgCtx.getMsg().getChannel(), ackStatus.flowQuota);
			}
		} finally {
			finishTx(rcv, txCtx);
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		ForgetResponse response = new ForgetResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		TransactionContext txCtx = rcv.getTransaction(parameters.getXid());
		if (txCtx != null) {
			// can't forget a tx which is not yet prepared.
			ErrorCode.setError(ErrorCode.XATransactionNotPrepared, response);
			return response;
		} else {
			// forget is equivalent to rollback since we don't do heuristic commiting or rolling back ourselves.
			List<MessageState> states = channelService.twoPhaseRollbackReceive(session.getZone(),
					session.getChannelDestination(), authorizedUser.getSerialNumber(), parameters.getXid());
			if (states.isEmpty()) {
				ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
				return response;
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		RollbackResponse response = new RollbackResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		TransactionContext txCtx = rcv.getTransaction(parameters.getXid());
		if (txCtx != null) {
			MessageContext msgCtx = txCtx.getCurrentMessage();
			try {
				ReceiveMessageResultHolder ackStatus = channelService.onePhaseRollbackReceive(session.getZone(),
						msgCtx.getMsg());

				if (ackStatus.flowControlOpened) {
					// relay opened FC back to origin
					relayFCWithRetry(session, rcv, msgCtx.getMsg().getChannel(), ackStatus.flowQuota);
				}
			} finally {
				finishTx(rcv, txCtx);
			}
		} else {
			List<MessageState> states = channelService.twoPhaseRollbackReceive(session.getZone(),
					session.getChannelDestination(), authorizedUser.getSerialNumber(), parameters.getXid());
			if (states.isEmpty()) {
				ErrorCode.setError(ErrorCode.XATransactionUnknown, response);
				return response;
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();
		ReceiverContext rcv = session.getReceiverContext(authorizedUser.getSerialNumber());

		CommitResponse response = new CommitResponse();
		if (!StringUtils.hasText(parameters.getXid())) {
			ErrorCode.setError(ErrorCode.MissingTransactionXID, response);
			return response;

		}
		// prepare deletes the tx so we should only find it in the case of one phase commit
		TransactionContext txCtx = rcv.getTransaction(parameters.getXid());
		if (txCtx != null) {
			if (parameters.isOnePhase()) {
				MessageContext msgCtx = txCtx.getCurrentMessage();
				// one phase commit - where the message was not yet prepared.
				ReceiveMessageResultHolder ackStatus = channelService.onePhaseCommitReceive(session.getZone(),
						msgCtx.getMsg());
				// TODO #107: delete chunks of received message
				if (ackStatus.flowControlOpened) {
					// relay opened FC back to origin
					relayFCWithRetry(session, rcv, msgCtx.getMsg().getChannel(), ackStatus.flowQuota);
				}
			} else {
				// 2pc we should not have found the stc.
				ErrorCode.setError(ErrorCode.XATransactionNotPrepared, response);
				return response;
			}
		} else {
			List<MessageState> states = channelService.twoPhaseCommitReceive(session.getZone(),
					session.getChannelDestination(), authorizedUser.getSerialNumber(), parameters.getXid());
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
		MDSServerSession session = authorizedSessionService.getAuthorizedSession();

		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();

		RecoverResponse response = new RecoverResponse();
		List<String> preparedXids = channelService.twoPhaseRecoverReceive(session.getZone(),
				session.getChannelDestination(), authorizedUser.getSerialNumber());

		response.getXids().addAll(preparedXids);
		response.setSuccess(true);
		return response;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void relayCDSWithRetry(MDSServerSession session, ReceiverContext rc, Channel channel) {
		// relay to the last known good ros for the channel.
		RelayStatus rs = relayClientService.relayChannelDestinationSession(rc.getRosTcpAddress(channel),
				session.getAccountZone(), session.getZone(), session.getDomain(), channel);
		if (!rs.isSuccess()) {
			if (rs.getErrorCode().isRetryable()) {
				RelayStatus retry = relayClientService.relayChannelDestinationSession(null /* get new MRS session */,
						session.getAccountZone(), session.getZone(), session.getDomain(), channel);
				if (!retry.isSuccess()) {
					ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
							rs.getErrorCode().getErrorMessage());
					channelService.updateStatusDestinationSession(channel.getId(), error);
					// remove any cached ros address since not working
					rc.clearRosTcpAddress(channel);
				} else {
					// cache the potentially changed ROS address
					rc.setRosTcpAddress(channel, retry.getRosTcpAddress());
				}
			} else {
				ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
						rs.getErrorCode().getErrorMessage());
				channelService.updateStatusDestinationSession(channel.getId(), error);
				// remove any cached ros address since not working
				rc.clearRosTcpAddress(channel);
			}
		} else {
			// cache the working ROS address
			rc.setRosTcpAddress(channel, rs.getRosTcpAddress());
		}
	}

	private void relayFCWithRetry(MDSServerSession session, ReceiverContext rc, Channel channel, FlowQuota fc) {
		// relay to the last known good ros for the channel.
		RelayStatus rs = relayClientService.relayChannelFlowControl(rc.getRosTcpAddress(channel),
				session.getAccountZone(), session.getZone(), session.getDomain(), channel, fc);
		if (!rs.isSuccess()) {
			if (rs.getErrorCode().isRetryable()) {
				RelayStatus retry = relayClientService.relayChannelFlowControl(null /* get new MRS session */,
						session.getAccountZone(), session.getZone(), session.getDomain(), channel, fc);
				if (!retry.isSuccess()) {
					ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
							rs.getErrorCode().getErrorMessage());
					channelService.updateStatusFlowQuota(fc.getId(), error);
					// remove any cached ros address since not working
					rc.clearRosTcpAddress(channel);
				} else {
					// cache the potentially changed ROS address
					rc.setRosTcpAddress(channel, retry.getRosTcpAddress());
				}
			} else {
				ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
						rs.getErrorCode().getErrorMessage());
				channelService.updateStatusFlowQuota(fc.getId(), error);
				// remove any cached ros address since not working
				rc.clearRosTcpAddress(channel);
			}
		} else {
			// cache the working ROS address
			rc.setRosTcpAddress(channel, rs.getRosTcpAddress());
		}
	}

	private Transaction getNonTransactionSpecification(Localtransaction ack) {
		Transaction tx = new Transaction();
		tx.setTxtimeout(ack.getTxtimeout());
		tx.setXid(getNonTransactionXid(ack.getClientId()));
		return tx;
	}

	private String getNonTransactionXid(String clientId) {
		return NON_TX_SPEC_ID_PREFIX + clientId;
	}

	private MessageContext startTx(ReceiverContext rcv, Transaction tx, ChannelMessage msg) {
		TransactionContext txCtx = new TransactionContext(tx);
		MessageContext msgCtx = new MessageContext(msg);
		txCtx.setCurrentMessage(msgCtx);
		rcv.addTransaction(txCtx);
		scheduleTransactionTimeout(txCtx, rcv);
		return msgCtx;
	}

	private void finishTx(ReceiverContext rcv, TransactionContext txCtx) {
		ScheduledFuture<?> timeout = txCtx.getTimeoutFuture();
		if (timeout != null) {
			timeout.cancel(false);
		}
		rcv.removeTransaction(txCtx.getTxId());
	}

	private void scheduleTransactionTimeout(TransactionContext txCtx, ReceiverContext rcv) {
		// transaction will timeout after the tx timeout seconds after starting the tx.
		ScheduledFuture<?> timeoutFuture = txTimeoutScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				// we simply forget the transaction if it times-out.
				log.info("Timeout of " + txCtx.getTxId());
				finishTx(rcv, txCtx);
			}
		}, txCtx.getTxSpec().getTxtimeout(), TimeUnit.SECONDS);
		// we need to record the future object so we can cancel later ( when tx completes )
		txCtx.setTimeoutFuture(timeoutFuture);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AuthorizedSessionLookupService<MDSServerSession> getAuthorizedSessionService() {
		return authorizedSessionService;
	}

	public void setAuthorizedSessionService(AuthorizedSessionLookupService<MDSServerSession> authorizedSessionService) {
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

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getMaxWaitTimeoutSec() {
		return maxWaitTimeoutSec;
	}

	public void setMaxWaitTimeoutSec(int maxWaitTimeoutSec) {
		this.maxWaitTimeoutSec = maxWaitTimeoutSec;
	}

	public ChunkService getChunkService() {
		return chunkService;
	}

	public void setChunkService(ChunkService chunkService) {
		this.chunkService = chunkService;
	}

}
