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
package org.tdmx.client.cli.user;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.DestinationDescriptor;
import org.tdmx.client.cli.ClientCliUtils.UnencryptedSessionKey;
import org.tdmx.client.cli.ClientErrorCode;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.Decrypter;
import org.tdmx.client.crypto.scheme.IntegratedCryptoSchemeFactory;
import org.tdmx.client.crypto.stream.FileBackedOutputStream;
import org.tdmx.client.crypto.stream.MacOfMacCalculator;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.mds.Acknowledge;
import org.tdmx.core.api.v01.mds.AcknowledgeResponse;
import org.tdmx.core.api.v01.mds.Download;
import org.tdmx.core.api.v01.mds.DownloadResponse;
import org.tdmx.core.api.v01.mds.GetDestinationSession;
import org.tdmx.core.api.v01.mds.GetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.Receive;
import org.tdmx.core.api.v01.mds.ReceiveResponse;
import org.tdmx.core.api.v01.mds.SetDestinationSession;
import org.tdmx.core.api.v01.mds.SetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.ChunkReference;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Dr;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Msgreference;
import org.tdmx.core.api.v01.msg.NonTransaction;
import org.tdmx.core.api.v01.scs.GetMDSSession;
import org.tdmx.core.api.v01.scs.GetMDSSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;
import org.tdmx.core.system.lang.StreamUtils;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "receive:poll", description = "performs a single receive from a destination", note = "Configure a destination first with destination:configure before receive.")
public class PollReceive implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "destination", required = true, description = "the destination address. Format: <localname>@<domain>#<service>")
	private String destination;

	@Parameter(name = "userSerial", defaultValueText = "<greatest existing User serial>", description = "the destination user's certificate serialNumber.")
	private Integer userSerialNumber;

	@Parameter(name = "userPassword", required = true, description = "the destination user's keystore password.")
	private String userPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		ClientCliUtils.checkValidDestination(destination);

		String domain = ClientCliUtils.getDomainName(destination);
		String localName = ClientCliUtils.getLocalName(destination);
		String service = ClientCliUtils.getServiceName(destination);

		TdmxZoneRecord domainInfo = ClientCliUtils.getSystemDnsInfo(domain);
		if (domainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + domain);
			return;
		}
		out.println("Domain info: " + domainInfo);

		// -------------------------------------------------------------------------
		// GET RECEIVER CONTEXT
		// -------------------------------------------------------------------------

		int ucSerial = ClientCliUtils.getUCMaxSerialNumber(domain, localName);
		if (userSerialNumber != null) {
			ucSerial = userSerialNumber;
		}
		PKIXCredential uc = ClientCliUtils.getUC(domain, localName, ucSerial, userPassword);

		DestinationDescriptor dd = null;
		if (ClientCliUtils.destinationDescriptorExists(destination)) {
			dd = ClientCliUtils.loadDestinationDescriptor(destination, userPassword);
		} else {
			out.println("Destination not configured yet. Use destination:configure command to initialize it.");
			return;
		}
		if (dd.containsOutdatedSessionKeys()) {
			out.println("Purging session keys which have exceeded their retention time.");
			dd.removeOutdatedSessionKeys();
			ClientCliUtils.storeDestinationDescriptor(dd, destination, userPassword);
		}

		// -------------------------------------------------------------------------
		// GET MDS SESSION
		// -------------------------------------------------------------------------

		PKIXCertificate scsPublicCertificate = ClientCliUtils.loadSCSTrustedCertificate(scsTrustedCertFile);
		SCS scs = ClientCliUtils.createSCSClient(uc, domainInfo.getScsUrl(), scsPublicCertificate);

		GetMDSSession sessionRequest = new GetMDSSession();
		sessionRequest.setServicename(service);
		GetMDSSessionResponse sessionResponse = scs.getMDSSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			out.println("Unable to get MDS session. ", sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		MDS mds = ClientCliUtils.createMDSClient(uc, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		GetDestinationSession destReq = new GetDestinationSession();
		destReq.setSessionId(sessionResponse.getSession().getSessionId());

		GetDestinationSessionResponse destRes = mds.getDestinationSession(destReq);
		if (!destRes.isSuccess()) {
			out.println("Unable to get current destination session. ", destRes.getError());
			return;
		}
		Destinationsession ds = destRes.getDestination().getDestinationsession();

		boolean uploadNewDs = false;
		if (ds == null) {
			out.println("No current destination session - initialization");
			uploadNewDs = true;
		} else if (!SignatureUtils.checkDestinationSessionSignature(service, ds)) {
			out.println("Current destination session signature invalid - replace.");
			uploadNewDs = true;
		} else {
			PKIXCertificate[] toUserChain = ClientCliUtils
					.getValidUserIdentity(ds.getUsersignature().getUserIdentity());
			PKIXCertificate signingUser = PKIXCertificate.getPublicKey(toUserChain);

			UnencryptedSessionKey sk = dd.getSessionKey(ds.getEncryptionContextId());

			if (sk == null) {
				out.println("Current destination session at service provider unknow.");
				// there is a foreign DS
				PKIXCertificate otherUC = CertificateIOUtils
						.safeDecodeX509(ds.getUsersignature().getUserIdentity().getUsercertificate());
				if (uc.getPublicCert().getSerialNumber() >= otherUC.getSerialNumber()) {
					out.println(
							"Re-initialize destination session at service provider since our user is same or more recent.");
					uploadNewDs = true;
				} else {
					out.println(
							"Our serialnumber is not uptodate - destination session at service provider not changed.");
				}

			} else if (signingUser.getSerialNumber() > uc.getPublicCert().getSerialNumber()) {
				out.println(
						"Current destination session at service provider defined by more recent user - standing down.");
			} else if (sk.isValidityExpired(dd.getSessionDurationInHours())) {
				out.println("Current destination session at service provider expired - renew.");
				uploadNewDs = true;
			}
		}

		if (uploadNewDs) {
			UnencryptedSessionKey sk = dd.createNewSessionKey();

			ds = sk.getNewDestinationSession(uc, service);
			// if we change the DD we must store it immediately.
			ClientCliUtils.storeDestinationDescriptor(dd, destination, userPassword);

			// upload new session to ServiceProvider.
			SetDestinationSession setDestReq = new SetDestinationSession();
			setDestReq.setSessionId(sessionResponse.getSession().getSessionId());
			setDestReq.setDestinationsession(ds);
			SetDestinationSessionResponse setDestRes = mds.setDestinationSession(setDestReq);
			if (!setDestRes.isSuccess()) {
				out.println("Unable to set new current destination session. ", setDestRes.getError());
				return;
			}
			out.println("New session set at the service provider.");
		}

		// do receive of the message
		String uniqTxId = ByteArray.asHex(EntropySource.getRandomBytes(16));
		NonTransaction nonTx = new NonTransaction();
		nonTx.setXid(uniqTxId);
		nonTx.setTxtimeout(60);

		Receive receiveRequest = new Receive();
		receiveRequest.setSessionId(sessionResponse.getSession().getSessionId());
		receiveRequest.setNonTransaction(nonTx);
		receiveRequest.setWaitTimeoutSec(60);

		ReceiveResponse receiveResponse = mds.receive(receiveRequest);
		if (!receiveResponse.isSuccess()) {
			out.println("Failed to receive Msg. ", receiveResponse.getError());
			return;
		}

		Msg msg = receiveResponse.getMsg();
		if (msg != null) {
			out.println("Received msgId=" + msg.getHeader().getMsgId());
			String continuationId = receiveResponse.getContinuation();
			// check the messageId is ok
			if (!SignatureUtils.checkMsgId(msg.getHeader(), msg.getPayload(),
					msg.getHeader().getUsersignature().getSignaturevalue().getTimestamp())) {
				out.println("Corrupted msgId.");
				handleAcknowledge(uc, msg, ClientErrorCode.getError(ClientErrorCode.ReceiveInvalidMessageId), mds,
						uniqTxId, sessionResponse.getSession().getSessionId(), out);
				return;
			}
			// check the message signature is ok
			if (!SignatureUtils.checkMessageSignature(msg.getHeader(), msg.getPayload())) {
				out.println("Msg signature invalid.");
				handleAcknowledge(uc, msg, ClientErrorCode.getError(ClientErrorCode.ReceiveInvalidMessageSignature),
						mds, uniqTxId, sessionResponse.getSession().getSessionId(), out);
				return;
			}
			PKIXCertificate[] fromUserChain = ClientCliUtils
					.getValidUserIdentity(msg.getHeader().getUsersignature().getUserIdentity());

			// TODO double check the channel from the fromUser is authorized

			// TODO from user is trusted, ie. has the same root cert as the send permissions root cert

			TemporaryFileManagerImpl bufferManager = new TemporaryFileManagerImpl();

			IntegratedCryptoSchemeFactory iecFactory = new IntegratedCryptoSchemeFactory(uc.getKeyPair(),
					PKIXCertificate.getPublicKey(fromUserChain).getCertificate().getPublicKey(), bufferManager);

			// prepare to download decrypt
			UnencryptedSessionKey sk = dd.getSessionKey(msg.getHeader().getEncryptionContextId());
			if (sk == null) {
				// we are unable to decrypt the message since we don't know the session key
				out.println("unable to decrypt the message, session key unknown.");

				handleAcknowledge(uc, msg, ClientErrorCode.getError(ClientErrorCode.ReceiveNoSessionKey), mds, uniqTxId,
						sessionResponse.getSession().getSessionId(), out);
				return;
			}
			if (!sk.getScheme().getName().equals(msg.getHeader().getScheme())) {
				out.println("Mismatch in encryption scheme.");
				handleAcknowledge(uc, msg, ClientErrorCode.getError(ClientErrorCode.MessageSchemeSessionMismatch), mds,
						uniqTxId, sessionResponse.getSession().getSessionId(), out);
				return;
			}
			FileBackedOutputStream fbos = bufferManager.getOutputStream(sk.getScheme().getChunkSize());
			boolean chunksOk = true;
			MacOfMacCalculator macOfMacCalculator = new MacOfMacCalculator(sk.getScheme().getChunkMACAlgorithm());

			try {
				// download the chunks, checking each mac, and appending the chunk to the temporary buffer
				int numChunks = (int) (1 + (msg.getPayload().getLength() / sk.getScheme().getChunkSize()));

				int pos = 0;
				Chunk c = msg.getChunk();

				do {
					if (!SignatureUtils.checkChunkMac(c, sk.getScheme())) {
						out.println("Chunk " + pos + " MAC invalid.");
						chunksOk = false;
					} else {
						out.println("Chunk " + pos + " MAC valid.");
						// write chunk data to the fbos
						fbos.write(c.getData());
						// add to mac to the MacOfMacs
						macOfMacCalculator.addChunkMac(ByteArray.fromHex(c.getMac()));
					}
					pos++;

					if (pos < numChunks && chunksOk) {
						// fetch next chunk
						ChunkReference cr = new ChunkReference();
						cr.setMsgId(msg.getHeader().getMsgId());
						cr.setPos(pos);

						Download downloadRequest = new Download();
						downloadRequest.setSessionId(sessionResponse.getSession().getSessionId());
						downloadRequest.setContinuation(continuationId);
						downloadRequest.setChunkref(cr);

						DownloadResponse downloadResponse = mds.download(downloadRequest);
						if (!downloadResponse.isSuccess()) {
							out.println("Failed to receive Chunk. ", downloadResponse.getError());
							chunksOk = false;
						} else {
							continuationId = downloadResponse.getContinuation();
						}
					}

				} while (pos < numChunks && chunksOk);

			} catch (IOException e) {
				throw new IllegalStateException(e);
			} finally {
				try {
					fbos.close();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}

			try {
				// check mac of mac after last chunk fetched
				if (chunksOk
						&& !macOfMacCalculator.checkMacOfMacs(ByteArray.fromHex(msg.getPayload().getMACofMACs()))) {
					out.println("MACofMAC invalid.");
					chunksOk = false;
				}
				if (!chunksOk) {
					handleAcknowledge(uc, msg, ClientErrorCode.getError(ClientErrorCode.ReceiveInvalidChunk), mds,
							uniqTxId, sessionResponse.getSession().getSessionId(), out);
					return;
				}

				// decrypt the message
				boolean decryptedOk = true;
				String filename = msg.getHeader().getExternalReference();
				if (StringUtils.hasText(filename)) {
					filename = "TDMX-" + System.currentTimeMillis();
					out.println("No external reference - so filename is defaulted to " + filename);
				}
				try (FileOutputStream fos = new FileOutputStream(filename)) {
					Decrypter dec = iecFactory.getDecrypter(sk.getScheme(), sk.getSessionKeyPair());
					InputStream plainContent = dec.getInputStream(fbos.getInputStream(),
							msg.getPayload().getEncryptionContext());

					StreamUtils.transfer(plainContent, fos);

				} catch (FileNotFoundException e) {
					out.println("Output file " + filename + " not found. ", e);
					decryptedOk = false;
				} catch (IOException e) {
					out.println("Error writing file " + filename + ". ", e);
					decryptedOk = false;
				} catch (CryptoException e) {
					out.println("Error decrypting to file " + filename + ". ", e);
					decryptedOk = false;
				}

				if (decryptedOk) {
					out.println("Sucessfully decrypted to " + filename);
					handleAcknowledge(uc, msg, null, mds, uniqTxId, sessionResponse.getSession().getSessionId(), out);
				} else {
					handleAcknowledge(uc, msg, ClientErrorCode.getError(ClientErrorCode.MessageDecryptionFailure), mds,
							uniqTxId, sessionResponse.getSession().getSessionId(), out);

				}
			} finally {
				fbos.discard();
			}

		} else {
			out.println("No message received.");
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void handleAcknowledge(PKIXCredential uc, Msg msg, Error error, MDS mds, String xid, String sessionId,
			CliPrinter out) {
		Msgreference msgRef = new Msgreference();
		msgRef.setExternalReference(msg.getHeader().getExternalReference());
		msgRef.setMsgId(msg.getHeader().getMsgId());
		msgRef.setSignature(msg.getHeader().getUsersignature().getSignaturevalue().getSignature());

		Dr dr = new Dr();
		dr.setError(error);
		dr.setMsgreference(msgRef);

		SignatureUtils.createDeliveryReceiptSignature(uc, SignatureAlgorithm.SHA_384_RSA, new Date(), dr);

		Acknowledge ackRequest = new Acknowledge();
		ackRequest.setXid(xid);
		ackRequest.setSessionId(sessionId);
		ackRequest.setDr(dr);
		AcknowledgeResponse ackResponse = mds.acknowledge(ackRequest);
		if (!ackResponse.isSuccess()) {
			out.println("Failed to NACK receipt. ", ackResponse.getError());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
