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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
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
import org.tdmx.core.api.v01.msg.Destinationinfo;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.NonTransaction;
import org.tdmx.core.api.v01.tx.Commit;
import org.tdmx.core.api.v01.tx.CommitResponse;
import org.tdmx.core.api.v01.tx.Forget;
import org.tdmx.core.api.v01.tx.ForgetResponse;
import org.tdmx.core.api.v01.tx.Prepare;
import org.tdmx.core.api.v01.tx.PrepareResponse;
import org.tdmx.core.api.v01.tx.Recover;
import org.tdmx.core.api.v01.tx.RecoverResponse;
import org.tdmx.core.api.v01.tx.Rollback;
import org.tdmx.core.api.v01.tx.RollbackResponse;
import org.tdmx.core.api.v01.tx.TransactionSpecification;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AgentSignature;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.Domain;
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
	private int maxWaitTimeoutSec = 300;

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
					// TODO LATER: we could think about caching the ROS addresses per channel known to the destination

					RelayStatus rs = relayClientService.relayChannelDestinationSession(null, session.getAccountZone(),
							zone, session.getDomain(), updatedChannel);
					if (!rs.isSuccess()) {
						ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_INITIATION,
								rs.getErrorCode().getErrorMessage());
						channelService.updateStatusDestinationSession(updatedChannel.getId(), error);
					}
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
	public DownloadResponse download(Download parameters) {
		// TODO Auto-generated method stub
		return null;
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

		TransactionSpecification tx = null;
		if (recvRequest.getTransaction() != null) {
			// TODO #101: validate tx
			tx = validator.checkTransaction(recvRequest.getTransaction(), response);
			if (tx == null) {
				return response;
			}
			if (recvRequest.getNonTransaction() != null) {
				ErrorCode.setError(ErrorCode.InvalidReceiveAcknowledgeMode, response);
				return response;
			}
		} else if (recvRequest.getNonTransaction() != null) {
			// validate NonTransaction info from api
			NonTransaction ack = validator.checkNonTransaction(recvRequest.getNonTransaction(), response);
			if (ack == null) {
				return response;
			}
			tx = ack;

			// TODO #95: Handle the ack of previous received message and initiate relay back of DR
			// caching the ROS address of the reverse channel
			MessageContext ctx = rcv.endTransaction(tx);
			if (ctx != null && ack.getDr() == null) {
				// error not acknowledging previously received msg
				ErrorCode.setError(ErrorCode.InvalidNonTransactionalAcknowledge, response, tx.getXid(), ctx.getMsgId());
				return response;
			} else if (ctx == null && ack.getDr() != null) {
				// no current message in the session - cannot have a DeliveryReport
				ErrorCode.setError(ErrorCode.InvalidDeliveryReportNoReceive, response, tx.getXid(), ctx.getMsgId());
				return response;
			} else if (ctx != null && ack.getDr() != null) {
				// check that the ack of the msg in the dr matches the ctx.
				if (!ack.getDr().getMsgreference().getMsgId().equals(ctx.getMsgId())) {
					ErrorCode.setError(ErrorCode.InvalidDeliveryReportMsgIdMismatch, response, tx.getXid(),
							ctx.getMsgId(), ack.getDr().getMsgreference().getMsgId());
					return response;
				}
				if (!ack.getDr().getMsgreference().getSignature().equals(ctx.getMsg().getSignature().getValue())) {
					ErrorCode.setError(ErrorCode.InvalidDeliveryReportSignatureMismatch, response, tx.getXid(),
							ctx.getMsgId());
					return response;
				}
				// extref is optional but must be replied exactly as received
				if (StringUtils.hasText(ack.getDr().getMsgreference().getExternalReference())) {
					if (!ack.getDr().getMsgreference().getExternalReference()
							.equals(ctx.getMsg().getExternalReference())) {
						ErrorCode.setError(ErrorCode.InvalidDeliveryReportExtRefMismatch, response, tx.getXid(),
								ctx.getMsgId());
						return response;
					}
				} else if (StringUtils.hasText(ctx.getMsg().getExternalReference())) {
					ErrorCode.setError(ErrorCode.InvalidDeliveryReportExtRefMismatch, response, tx.getXid(),
							ctx.getMsgId());
					return response;
				}
				// TODO #95: check signature - receiver

				AgentSignature receipt = a2d.mapUserSignature(ack.getDr().getReceiptsignature());

				// acknowledge the message, possibly opening the relay flow control
				ReceiveMessageResultHolder ackStatus = channelService.acknowledgeMessageReceipt(session.getZone(),
						ctx.getMsg(), receipt);
				// TODO #93: cache the ROS address in the receiver context and retry on failre
				if (ackStatus.flowControlOpened) {
					RelayStatus rs = relayClientService.relayChannelFlowControl(null, session.getAccountZone(),
							session.getZone(), session.getDomain(), ctx.getMsg().getChannel(), ackStatus.flowQuota);
					if (!rs.isSuccess()) {
						ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_INITIATION,
								rs.getErrorCode().getErrorMessage());
						channelService.updateStatusFlowQuota(ackStatus.flowQuota.getId(), error);
					}
				}
				if (ProcessingStatus.PENDING == ackStatus.msg.getProcessingState().getStatus()) {
					// relay DR back to origin
					RelayStatus rs = relayClientService.relayChannelMessage(null, session.getAccountZone(),
							session.getZone(), session.getDomain(), ctx.getMsg().getChannel(), ctx.getMsg());
					if (!rs.isSuccess()) {
						ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_INITIATION,
								rs.getErrorCode().getErrorMessage());
						channelService.updateStatusMessage(ctx.getMsg().getId(), error);
					}
				}

			} else {
				log.debug("No current message and no DR for " + tx.getXid());
			}
		} else {
			// must be tx or non tx not neither
			ErrorCode.setError(ErrorCode.MissingReceiveAcknowledgeMode, response);
			return response;
		}

		boolean mustFetch = rcv.isFetchRequired();
		if (mustFetch) {
			// fetch next batch of messages
			ChannelMessageSearchCriteria criteria = new ChannelMessageSearchCriteria(new PageSpecifier(0, batchSize));
			criteria.getDestination().setDomainName(session.getDomain().getDomainName());
			criteria.getDestination().setLocalName(session.getDestinationAddress().getLocalName());
			criteria.getDestination().setServiceName(session.getService().getServiceName());
			criteria.setAcknowledged(false);
			criteria.setProcessingStatus(ProcessingStatus.NONE);
			List<ChannelMessage> pendingMsgs = channelService.search(session.getZone(), criteria);
			rcv.addPendingMessages(pendingMsgs, pendingMsgs.size() == batchSize);
		}

		// get any ready message, waiting if none available.
		MessageContext msgCtx = rcv.getNextPendingMessage(recvRequest.getWaitTimeoutSec() * 1000, tx);
		if (msgCtx == null) {
			// no message available even after waiting.
			response.setSuccess(true);
			return response;
		}

		ChannelMessage msg = msgCtx.getMsg();
		Msg m = d2a.mapChannelMessage(msg);

		// get 1st chunk and map it into msg
		Chunk chunk = chunkService.findByMsgIdAndPos(msg.getMsgId(), 0);
		m.setChunk(d2a.mapChunk(chunk));

		response.setMsg(m);
		response.setRetryCount(msgCtx.getNumDeliveries());
		response.setSuccess(true);
		response.setContinuation(""); // TODO #95: chunks
		return response;
	}

	@Override
	public AcknowledgeResponse acknowledge(Acknowledge parameters) {
		// TODO #95: handle acknowledge
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		// TODO Auto-generated method stub
		return null;
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
