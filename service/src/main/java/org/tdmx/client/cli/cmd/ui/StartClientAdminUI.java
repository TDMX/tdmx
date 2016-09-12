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
package org.tdmx.client.cli.cmd.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.tdmx.client.cli.cmd.AbstractCliCommand;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ServerIpCredentialSpecifier;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "ui:start", description = "starts the client administration UI", note = "A default keystore is created if it doesn't exist, featuring a self signed certificate for 'localhost'.")
public class StartClientAdminUI extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "serverAddress", description = "the specific server hostname to bind to for multi-homed hosts.")
	private String serverAddress;
	@Parameter(name = "httpsPort", required = true, defaultValue = "443", description = "the HTTPS port.")
	private Integer httpsPort;

	@Parameter(name = "keystoreFile", required = true, defaultValue = "client-ui.keystore", description = "the HTTPS keystore file.")
	private String keyStoreFile;
	@Parameter(name = "keystorePassword", required = true, defaultValue = "Un4GettableRändomUngu33ssable", description = "the HTTPS keystore password.")
	private String keyStorePassword;
	@Parameter(name = "keystoreType", required = true, defaultValue = "jks", description = "the HTTPS keystore type.")
	private String keyStoreType;
	@Parameter(name = "keystoreAlias", required = true, defaultValue = "server", description = "the HTTPS keystore key alias.")
	private String keyStoreAlias;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {

		// we create a HTTPS keystore to provide secure access to https://localhost:443

		if (!getUiKeystoreService().existsServerKey(keyStoreFile)) {
			if (!createKeystore(out)) {
				out.println("Unable to create HTTPS keystore.");
			}
		} else {
			PKIXCredential serverKey = getUiKeystoreService().getServerKey(keyStoreFile, keyStoreType, keyStorePassword,
					keyStoreAlias);
			if (serverKey == null) {
				out.println("Abort starting UI due to inability to get server key.");
				return;
			}
			// check expiration.
			if (Calendar.getInstance().before(serverKey.getPublicCert().getNotAfter())) {
				out.println("HTTPS server key has expired. Delete the keystore and restart.");
				return;
			}
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private boolean createKeystore(CliPrinter out) {
		try {
			InetAddress serverInterface = StringUtils.hasText(serverAddress) ? InetAddress.getByName(serverAddress)
					: InetAddress.getLocalHost();
			String serverLocalIPAddress = serverInterface.getHostAddress();
			out.println("creating self signed HTTPS server key for " + serverLocalIPAddress + ":" + httpsPort);

			PKIXCredential serverCred;
			try {
				serverCred = createServerPrivateKey(serverLocalIPAddress);
			} catch (CryptoCertificateException e) {
				out.println("Unable to create HTTPS private key.", e);
				return false;
			}

			getUiKeystoreService().saveServerKey(serverCred, keyStoreFile, keyStoreType, keyStorePassword,
					keyStoreAlias);
			return true;
		} catch (UnknownHostException e) {
			out.println("Unable to resolve host " + e.getMessage(), e);
		}
		return false;

	}

	private PKIXCredential createServerPrivateKey(String serverLocalIPAddress) throws CryptoCertificateException {
		Calendar now = Calendar.getInstance();
		Calendar later = Calendar.getInstance();
		later.add(Calendar.DATE, 720);

		ServerIpCredentialSpecifier sics = new ServerIpCredentialSpecifier(serverLocalIPAddress);
		sics.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		sics.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);

		sics.setNotAfter(later);
		sics.setNotBefore(now);

		return CredentialUtils.createServerIpCredential(sics);
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
