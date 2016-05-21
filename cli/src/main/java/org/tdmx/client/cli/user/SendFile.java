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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoContext.ChunkSequentialReader;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.client.crypto.scheme.IntegratedCryptoSchemeFactory;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Taskstatus;
import org.tdmx.core.api.v01.mos.GetChannel;
import org.tdmx.core.api.v01.mos.GetChannelResponse;
import org.tdmx.core.api.v01.mos.Submit;
import org.tdmx.core.api.v01.mos.SubmitResponse;
import org.tdmx.core.api.v01.mos.Upload;
import org.tdmx.core.api.v01.mos.UploadResponse;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.Channelauthorization;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Grant;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.scs.GetMOSSession;
import org.tdmx.core.api.v01.scs.GetMOSSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.core.system.lang.StreamUtils;

@Cli(name = "send:file", description = "sends a single file to a destination")
public class SendFile implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "from", required = true, description = "the from address. Format: <localname>@<domain>")
	private String origin;

	@Parameter(name = "to", required = true, description = "the destination address. Format: <localname>@<domain>#<service>")
	private String to;

	@Parameter(name = "userSerial", defaultValueText = "<greatest existing User serial>", description = "the origin user's certificate serialNumber.")
	private Integer userSerialNumber;

	@Parameter(name = "userPassword", required = true, masked = true, description = "the origin user's keystore password.")
	private String userPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

	@Parameter(name = "file", required = true, description = "the filename to transfer.")
	private String file;

	@Parameter(name = "ttlHours", defaultValue = "24", description = "the time to live in hours.")
	private int ttlHours;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		ClientCliUtils.checkValidDestination(to);

		ClientCliUtils.checkValidUserName(origin);
		String originDomain = ClientCliUtils.getDomainName(origin);
		String originLocalName = ClientCliUtils.getLocalName(origin);
		String serviceName = ClientCliUtils.getServiceName(to);
		String destinationDomain = ClientCliUtils.getDomainName(to);

		TdmxZoneRecord originDomainInfo = ClientCliUtils.getSystemDnsInfo(originDomain);
		if (originDomainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + originDomain);
			return;
		}
		out.println("Origin domain info: " + originDomainInfo);

		File fileHandle = new File(file);
		if (!fileHandle.exists()) {
			out.println("File " + file + " not found.");
			return;
		}

		// -------------------------------------------------------------------------
		// GET RECEIVER CONTEXT
		// -------------------------------------------------------------------------

		int ucSerial = ClientCliUtils.getUCMaxSerialNumber(originDomain, originLocalName);
		if (userSerialNumber != null) {
			ucSerial = userSerialNumber;
		}
		PKIXCredential uc = ClientCliUtils.getUC(originDomain, originLocalName, ucSerial, userPassword);

		// -------------------------------------------------------------------------
		// GET MOS SESSION
		// -------------------------------------------------------------------------

		PKIXCertificate scsPublicCertificate = ClientCliUtils.loadSCSTrustedCertificate(scsTrustedCertFile);
		SCS scs = ClientCliUtils.createSCSClient(uc, originDomainInfo.getScsUrl(), scsPublicCertificate);

		GetMOSSession sessionRequest = new GetMOSSession();
		GetMOSSessionResponse sessionResponse = scs.getMOSSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			out.println("Unable to get MOS session. ", sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		MOS mos = ClientCliUtils.createMOSClient(uc, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		GetChannel channelReq = new GetChannel();
		ChannelDestination cd = new ChannelDestination();
		cd.setLocalname(ClientCliUtils.getLocalName(to));
		cd.setDomain(destinationDomain);
		cd.setServicename(serviceName);
		channelReq.setDestination(cd);
		channelReq.setSessionId(sessionResponse.getSession().getSessionId());

		GetChannelResponse destRes = mos.getChannel(channelReq);
		if (!destRes.isSuccess()) {
			out.println("Unable to get channel information. ", destRes.getError());
			return;
		}

		if (destRes.getChannelinfo() == null) {
			out.println("No channel information found from " + origin + " to " + to);
			return;
		}
		// check the channel is authorized and open towards the destination
		Channelauthorization ci = destRes.getChannelinfo().getChannelauthorization();
		if (ci.getUnconfirmed() != null) {
			// if the destination's permission is changed but we haven't yet confirmed it we have potential conflicts if
			// we send now.
			out.println("Warning: channel has unconfirmed authorization. ", ci.getUnconfirmed());
		}
		if (Taskstatus.NONE != ci.getPs().getStatus()) {
			// if the CA is not propagated yet or failed to propagate to the destination, we have a possible problem for
			// the sender
			out.println("Warning: channel authorization processing status. ", ci.getPs());
		}
		if (ci.getCurrent() == null) {
			out.println("No channel current authorization.");
			return;
		}
		// check the send authorization
		Permission sendPermission = ci.getCurrent().getOriginPermission();
		if (sendPermission == null) {
			out.println("Warning: No current send permission.");
			return;
		}
		if (Grant.ALLOW != sendPermission.getPermission()) {
			out.println("Sending not permitted. ", sendPermission);
			return;
		}
		// .. send:file list from=user1@z1.tdmx.org to=user2@z2.tdmx.org#service1 userPassword=changeme exec
		// check that the channel authorization we received is signed correctly and we trust the authorizer.
		PKIXCertificate[] sendAuthorizer = ClientCliUtils
				.getValidAdministrator(ci.getCurrent().getAdministratorsignature().getAdministratorIdentity());
		if (!SignatureUtils.checkChannelAuthorizationSignature(ci.getChannel(), ci.getCurrent())) {
			out.println("Channel authorization signature invalid.");
			return;
		}
		PKIXCertificate authorizerRoot = PKIXCertificate.getZoneRootPublicKey(sendAuthorizer);
		if (!authorizerRoot.getFingerprint().equals(originDomainInfo.getZacFingerprint())) {
			out.println("Warning: authorizer root certificate not anchored in DNS for " + originDomain);
		}
		if (!ClientCliUtils.isSameRootCertificate(uc.getCertificateChain(), sendAuthorizer)) {
			out.println("Warning: authorizer's root certificate not identical to sender's.");
			if (!authorizerRoot.getFingerprint().equals(originDomainInfo.getZacFingerprint())) {
				out.println(
						"Sending prohibited due to distrust of authorizer. Note: any change of zone administrator certificate should be anchored in DNS.");
				return;
			}
		}
		// check the recv permitter
		Permission recvPermission = ci.getCurrent().getDestinationPermission();
		if (recvPermission == null) {
			out.println("Send prohibited due to lack of current receiver permission.");
			return;
		}
		if (Grant.ALLOW != recvPermission.getPermission()) {
			out.println("Destination does not permit receive. ", recvPermission);
			return;
		}
		// check that the channel authorization we received is signed correctly and we trust the authorizer.
		PKIXCertificate[] recvPermitter = ClientCliUtils
				.getValidAdministrator(recvPermission.getAdministratorsignature().getAdministratorIdentity());
		if (!SignatureUtils.checkEndpointPermissionSignature(ci.getChannel(), recvPermission)) {
			out.println("Receive permitter signature invalid.");
			return;
		}

		// check the channel has a destination session
		if (destRes.getChannelinfo().getSessioninfo() == null
				|| destRes.getChannelinfo().getSessioninfo().getDestinationsession() == null) {
			out.println("No destination session information.");
			return;
		}

		// the destination session signature must be correct and we must trust the signer before sending.
		// to trust the signer, the signer must share the same root certificate as the destination's authorization
		// (which our authorizer has allowed).
		Destinationsession ds = destRes.getChannelinfo().getSessioninfo().getDestinationsession();
		if (!SignatureUtils.checkDestinationSessionSignature(serviceName, ds)) {
			out.println("Destination session signature invalid.");
			return;
		}
		IntegratedCryptoScheme scheme = IntegratedCryptoScheme.fromName(ds.getScheme());
		if (scheme == null) {
			out.println("Unknown encryption scheme " + ds.getScheme());
			return;
		}
		PKIXCertificate[] toUserChain = ClientCliUtils.getValidUserIdentity(ds.getUsersignature().getUserIdentity());

		// establish that we trust the "to" user by checking that it's zone root is the same as in the
		// destination permission which "our" administrator has trusted enough to allow us to send to it
		if (!ClientCliUtils.isSameRootCertificate(toUserChain, recvPermitter)) {
			out.println("Warning: authorizer's root certificate not identical to sender's.");
			TdmxZoneRecord destinationDomainInfo = ClientCliUtils.getSystemDnsInfo(destinationDomain);
			if (destinationDomainInfo == null) {
				out.println("No TDMX DNS TXT record found for " + destinationDomain);
				return;
			}
			out.println("Destination domain info: " + destinationDomainInfo);
			PKIXCertificate toUserRoot = PKIXCertificate.getZoneRootPublicKey(toUserChain);
			if (!toUserRoot.getFingerprint().equals(destinationDomainInfo.getZacFingerprint())) {
				out.println(
						"Sending prohibited due to distrust of destination user. Note: any change of zone administrator certificate should be anchored in DNS.");
				return;
			} else {
				out.println("Send allowed since destination user's zone root certificate anchored in DNS for "
						+ destinationDomain);
			}
		}

		Calendar now = CalendarUtils.getTimestamp(new Date());
		Calendar ttl = CalendarUtils.getTimestamp(now.getTime());
		ttl.add(Calendar.HOUR, ttlHours);

		TemporaryFileManagerImpl bufferManager = new TemporaryFileManagerImpl();

		IntegratedCryptoSchemeFactory iecFactory = new IntegratedCryptoSchemeFactory(uc.getKeyPair(),
				PKIXCertificate.getPublicKey(toUserChain).getCertificate().getPublicKey(), bufferManager);

		try (InputStream bais = new FileInputStream(fileHandle)) {

			try {
				Encrypter enc = iecFactory.getEncrypter(scheme, ds.getSessionKey());
				try (OutputStream os = enc.getOutputStream()) {
					StreamUtils.transfer(bais, os);
				}
				CryptoContext cc = enc.getResult();

				Msg m = mapMsg(uc, now, cc, ci, ds, ttl, file);

				try (ChunkSequentialReader csr = cc.getChunkReader()) {
					Chunk chunk = csr.getNextChunk(m.getHeader().getMsgId());
					m.setChunk(chunk);

					Submit submitReq = new Submit();
					submitReq.setSessionId(sessionResponse.getSession().getSessionId());
					submitReq.setMsg(m);

					SubmitResponse submitResponse = mos.submit(submitReq);
					if (!submitResponse.isSuccess()) {
						out.println("Message submission failed. ", submitResponse.getError());
					}
					out.println("Message header uploaded successfully.");
					out.println("Message chunk[0] uploaded successfully.");

					String continuationId = submitResponse.getContinuation();
					while ((chunk = csr.getNextChunk(m.getHeader().getMsgId())) != null) {
						Upload upl = new Upload();
						upl.setChunk(chunk);
						upl.setContinuation(continuationId);
						upl.setSessionId(sessionResponse.getSession().getSessionId());

						UploadResponse uploadResponse = mos.upload(upl);
						if (!uploadResponse.isSuccess()) {
							out.println("Chunk submission failed. ", submitResponse.getError());
							return;
						}
						out.println("Message chunk[" + chunk.getPos() + "] uploaded successfully.");
						// get the next continuationId
						continuationId = uploadResponse.getContinuation();
					}
				}
				out.println("Message sent successfully. MsgId=" + m.getHeader().getMsgId());

			} catch (CryptoException e) {
				throw new IllegalStateException(e);
			}

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	private Msg mapMsg(PKIXCredential from, Calendar now, CryptoContext cc, Channelauthorization ci,
			Destinationsession ds, Calendar ttl, String externalReference) {
		Msg m = new Msg();

		Header hdr = new Header();
		hdr.setChannel(ci.getChannel());
		hdr.setEncryptionContextId(ds.getEncryptionContextId());
		hdr.setScheme(ds.getScheme());
		hdr.setExternalReference(externalReference);
		hdr.setTo(ds.getUsersignature().getUserIdentity());
		hdr.setTtl(ttl);
		m.setHeader(hdr);

		Payload p = new Payload();
		p.setEncryptionContext(cc.getEncryptionContext());
		p.setLength(cc.getCiphertextLength());
		p.setMACofMACs(ByteArray.asHex(cc.getMacOfMacs()));
		p.setPlaintextLength(cc.getPlaintextLength());
		m.setPayload(p);

		SignatureUtils.setMsgId(hdr, p, now);
		SignatureUtils.createMessageSignature(from, SignatureAlgorithm.SHA_256_RSA, now, hdr, p);
		return m;
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
