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
package org.tdmx.console.service;

import java.util.List;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.console.domain.Certificate;
import org.tdmx.console.domain.CertificateAuthority;
import org.tdmx.console.domain.CertificateAuthorityRequest;
import org.tdmx.console.domain.DnsResolverList;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Job;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.User;
import org.tdmx.console.domain.validation.OperationError;

public interface UIService {

	// TODO JUCI - change to "search(input)"->List<Object> (ui domain object)
	public List<Domain> listDomains();

	// Global application calls
	//
	public List<Job> getJobs();

	// Problem related calls
	//
	public boolean hasProblems();

	public int getNumberOfProblems();

	public List<Problem> getProblems();

	public void deleteAllProblems();

	public void deleteProblem(String id);

	public Problem getMostRecentProblem();

	// User related calls
	//
	public User authenticate(String login, String password);

	// DNS resolver list
	public OperationError deleteDnsResolverList(String id);

	public OperationError createOrUpdateDnsResolverList(DnsResolverList object);

	public DnsResolverList getDnsResolverList(String id);

	public List<DnsResolverList> searchDnsResolverList(String criteria);

	// CA operations
	public List<AsymmetricEncryptionAlgorithm> getValidCSRKeyAlgorithms();

	public List<SignatureAlgorithm> getValidCSRSignatureAlgorithms(AsymmetricEncryptionAlgorithm keyAlgorithm);

	public OperationError createCertificateAuthority(CertificateAuthorityRequest request);

	public OperationError deleteCertificateAuthority(String id);

	public OperationError updateCertificateAuthority(CertificateAuthority object);

	public CertificateAuthority getCertificateAuthority(String id);

	public List<CertificateAuthority> searchCertificateAuthority(String criteria);

	// Certificate operations
	public Certificate getCertificate(String id);

	public List<Certificate> searchCertificate(String criteria);

}