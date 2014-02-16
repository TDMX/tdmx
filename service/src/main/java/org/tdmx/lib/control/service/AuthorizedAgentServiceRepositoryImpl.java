/**
 *   Copyright 2010 Peter Klauser
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
*/
package org.tdmx.lib.control.service;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.TdmxZoneInfo;
import org.tdmx.lib.control.dao.AuthorizedAgentDao;
import org.tdmx.lib.control.domain.AuthorizationStatus;
import org.tdmx.lib.control.domain.AuthorizedAgent;

/**
 * @author Peter Klauser
 *
 */
public class AuthorizedAgentServiceRepositoryImpl implements AuthorizedAgentService {

	private static Logger log = LoggerFactory.getLogger(AuthorizedAgentServiceRepositoryImpl.class);
	 
	private AuthorizedAgentDao authorizedAgentDao;
	
	@Override
	@Transactional(value="ControlDB")
	public void createOrUpdate(PKIXCertificate certificate, AuthorizationStatus status) {
		String fp = certificate.getFingerprint();
		TdmxZoneInfo zi = certificate.getTdmxZoneInfo();
		if ( zi == null ) {
			throw new IllegalArgumentException("certificate missing TdmxZoneInfo");
		}
		
		AuthorizedAgent agent = new AuthorizedAgent();
		agent.setSha1fingerprint(certificate.getFingerprint());
		agent.setAuthorizationStatus(status);
		agent.setZoneApex(zi.getZoneRoot());
		try {
			agent.setCertificatePem(CertificateIOUtils.x509certToPem(certificate));
		} catch (CryptoCertificateException e) {
			throw new PersistenceException(e);
		}
		
		AuthorizedAgent storedAgent = getAuthorizedAgentDao().loadByFingerprint(fp);
		if ( storedAgent == null ) {
			getAuthorizedAgentDao().persist(agent);
		} else {
			getAuthorizedAgentDao().merge(agent);
		}
	}


	@Override
	@Transactional(value="ControlDB", readOnly=true)
	public AuthorizationStatus checkAuthorization(PKIXCertificate certificate) {
		String fp = certificate.getFingerprint();
		AuthorizedAgent agent = getAuthorizedAgentDao().loadByFingerprint(fp);
		if ( agent == null ) {
			return AuthorizationStatus.UNKNOWN;
		} else {
			try {
				PKIXCertificate storedCert = CertificateIOUtils.pemToX509cert(agent.getCertificatePem());
				if ( !certificate.isIdentical(storedCert) ) {
					return AuthorizationStatus.CONFLICT;
				}
			} catch (CryptoCertificateException e) {
				log.warn("Unable to check certificate " + fp, e);
				return AuthorizationStatus.ERROR;
			}
		}
		return agent.getAuthorizationStatus();
	}


	@Override
	@Transactional(value="ControlDB")
	public void delete(PKIXCertificate certificate) {
		String fp = certificate.getFingerprint();
		
		AuthorizedAgent agent = getAuthorizedAgentDao().loadByFingerprint(fp);
		if ( agent != null ) {
			getAuthorizedAgentDao().delete(agent);
		} else {
			log.warn("Unable to find AuthorizedAgent to delete with fingerprint " + fp);
		}
	}

	
	public AuthorizedAgentDao getAuthorizedAgentDao() {
		return authorizedAgentDao;
	}


	public void setAuthorizedAgentDao(AuthorizedAgentDao authorizedAgentDao) {
		this.authorizedAgentDao = authorizedAgentDao;
	}


}
