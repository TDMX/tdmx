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
package org.tdmx.lib.zone.service;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.zone.domain.AgentCredential;

/**
 * Factory Services for AgentCredential.
 * 
 * @author Peter
 * 
 */
public interface AgentCredentialFactory {

	/**
	 * Creates an AgentCredential from the certificate chain.
	 * 
	 * @param certChain
	 * @return the AgentCredential or null if there is any problem with the certificateChain.
	 */
	public AgentCredential createAgentCredential(PKIXCertificate[] certChain);

	/**
	 * Creates an AgentCredential from certificate byte data - provided by client which is not checked by the TLS stack,
	 * ie. by WebService.
	 * 
	 * TODO validate that domainCert is signed by zacCert
	 * 
	 * TODO validate zoneApex is same as authorizedContext
	 * 
	 * @param domainCert
	 * @param zacCert
	 * @return null if the DAC is invalid, else the DAC.
	 */
	public AgentCredential createDAC(byte[] domainCert, byte[] zacCert);

	/**
	 * Creates an AgentCredential from certificate byte data - provided by client which is not checked by the TLS stack,
	 * ie. by WebService.
	 * 
	 * TODO validate that userCert is signed by domainCert
	 * 
	 * TODO validate that domainCert is signed by zacCert
	 * 
	 * TODO validate zoneApex is same as authorizedContext
	 * 
	 * @param userCert
	 * @param domainCert
	 * @param zacCert
	 * @return null if the DAC is invalid, else the DAC.
	 */
	public AgentCredential createUC(byte[] userCert, byte[] domainCert, byte[] zacCert);

}
