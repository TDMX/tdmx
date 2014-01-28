package org.tdmx.console.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.console.application.Administration;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.X509CertificateDO;
import org.tdmx.console.application.service.OperationResultHolder;
import org.tdmx.console.domain.Certificate;
import org.tdmx.console.domain.CertificateAuthority;
import org.tdmx.console.domain.CertificateAuthorityRequest;
import org.tdmx.console.domain.DnsResolverList;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Job;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.User;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError;

public class UIServiceImpl implements UIService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Administration admin;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public List<Domain> listDomains( ) {
		return getAdmin().getObjectRegistry().getDomains();
	}

	@Override
	public List<Job> getJobs() {
		List<Job> list = new ArrayList<>();
		for( org.tdmx.console.application.job.BackgroundJob j : getAdmin().getBackgroundJobRegistry().getJobs()) {
			list.add(new Job(j));
		}
		return list;
	}

	@Override
	public List<Problem> getProblems() {
		List<Problem> list = new ArrayList<>();
		for( org.tdmx.console.application.domain.ProblemDO p : getAdmin().getProblemRegistry().getProblems()) {
			list.add(new Problem(p));
		}
		return list;
	}

	@Override
	public boolean hasProblems() {
		return getAdmin().getProblemRegistry().getProblems().size() > 0;
	}

	@Override
	public void deleteAllProblems() {
		 getAdmin().getProblemRegistry().deleteAllProblems();
	}

	@Override
	public void deleteProblem(String id) {
		 getAdmin().getProblemRegistry().deleteProblem(id);
	}

	@Override
	public int getNumberOfProblems() {
		return  getAdmin().getProblemRegistry().getProblems().size();
	}

	@Override
	public Problem getMostRecentProblem() {
		org.tdmx.console.application.domain.ProblemDO p = getAdmin().getProblemRegistry().getLastProblem();
		return p != null ? new Problem(p) : null;
	}

	@Override
	public User authenticate(String login, String password) {
		if ("secret".equals(password) ) {
			User user = new User(login, "George", "Bush", "noreply@mycompany.com", new Date());
			return user;
		}
		return null;
	}
	
	@Override
	public DnsResolverList getDnsResolverList(String id) {
		DnsResolverListDO dom = getAdmin().getDnsResolverService().lookup(id);
		return dom != null ? new DnsResolverList(dom) : null;
	}

	@Override
	public List<DnsResolverList> searchDnsResolverList(String criteria) {
		List<DnsResolverList> list = new ArrayList<>();
		List<DnsResolverListDO> domList = getAdmin().getDnsResolverService().search(criteria);
		for( DnsResolverListDO dom : domList ) {
			list.add(new DnsResolverList(dom));
		}
		return list;
	}

	@Override
	public OperationError deleteDnsResolverList(String id) {
		return getAdmin().getDnsResolverService().delete(id);
	}
	
	@Override
	public OperationError createOrUpdateDnsResolverList(DnsResolverList object) {
		DnsResolverListDO d = object.domain();
		return getAdmin().getDnsResolverService().createOrUpdate(d);
	}

	@Override
	public List<AsymmetricEncryptionAlgorithm> getValidCSRKeyAlgorithms() {
		return getAdmin().getCertificateAuthorityService().getKeyTypes();
	}

	@Override
	public List<SignatureAlgorithm> getValidCSRSignatureAlgorithms(
			AsymmetricEncryptionAlgorithm keyAlgorithm) {
		return getAdmin().getCertificateAuthorityService().getSignatureTypes(keyAlgorithm);
	}

	@Override
	public OperationError createCertificateAuthority(
			CertificateAuthorityRequest request) {
		List<FieldError> errors = request.validate();
		if ( errors.size() > 0 ) {
			return new OperationError(errors);
		}
		ZoneAdministrationCredentialSpecifier csr = request.domain();
		// on success, the request's certificateAuthorityId is set.
		OperationResultHolder<String> result = new OperationResultHolder<>();
		getAdmin().getCertificateAuthorityService().create(csr, result);
		if  ( result.getError() != null ) {
			return result.getError();
		}
		request.setCertificateAuthorityId(result.getResult());
		return null;
	}

	@Override
	public OperationError deleteCertificateAuthority(String id) {
		return getAdmin().getCertificateAuthorityService().delete(id);
	}

	@Override
	public OperationError updateCertificateAuthority(CertificateAuthority object) {
		CertificateAuthorityDO d = object.domain();
		return getAdmin().getCertificateAuthorityService().update(d);
	}

	@Override
	public CertificateAuthority getCertificateAuthority(String id) {
		CertificateAuthorityDO dom = getAdmin().getCertificateAuthorityService().lookup(id);
		return dom != null ? new CertificateAuthority(dom) : null;
	}

	@Override
	public List<CertificateAuthority> searchCertificateAuthority(String criteria) {
		List<CertificateAuthority> list = new ArrayList<>();
		List<CertificateAuthorityDO> domList = getAdmin().getCertificateAuthorityService().search(criteria);
		for( CertificateAuthorityDO dom : domList ) {
			list.add(new CertificateAuthority(dom));
		}
		return list;
	}

	@Override
	public Certificate getCertificate(String id) {
		X509CertificateDO dom = getAdmin().getCertificateService().lookup(id);
		return dom != null ? new Certificate(dom) : null;
	}

	@Override
	public List<Certificate> searchCertificate(String criteria) {
		List<Certificate> list = new ArrayList<>();
		List<X509CertificateDO> domList = getAdmin().getCertificateService().search(criteria);
		for( X509CertificateDO dom : domList ) {
			list.add(new Certificate(dom));
		}
		return list;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public Administration getAdmin() {
		return admin;
	}

	public void setAdmin(Administration admin) {
		this.admin = admin;
	}

}
