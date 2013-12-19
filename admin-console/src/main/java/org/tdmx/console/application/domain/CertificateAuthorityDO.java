package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.OperationError.ERROR;
import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;



/**
 * A CertificateAuthority is an internal self-signed X509Certificate and 
 * asymmetric keypair which can be used to sign one's own DomainCertificates.
 * 
 * Active CertificateAuthorities are automatically placed in the ca-trusted RootCAList.
 * Inactive CertificateAuthorities are automatically placed in the ca-distrusted RootCAList. 
 * 
 * The CA is represented by a single X509Certificate because it shall be used as a RootCA
 * without intermediate CAs.
 * 
 * X509 fields:
 * Subject { CN = "name", O = "organization", C = "country" } == Issuer
 * Validity <= 10yrs
 * BasicConstraints: { Subject Type=CA, Path Length Constraint=none(0) or 1 }
 * KeyUage {Certificate Signing, Digital Signature }
 * SubjectKeyIdentifier { }, AuthorityKeyIdentifier { KeyID=... }
 * 
 * NOTE: no crl list is used - the "revocation" takes place by removal from DNS
 * 
 * @author Peter
 *
 */
public class CertificateAuthorityDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_ACTIVE 			= new DomainObjectField("active", DomainObjectType.CertificateAuthority);
	public static final DomainObjectField F_CERTIFICATE_ID	= new DomainObjectField("x509certificate-id", DomainObjectType.CertificateAuthority);
	
	public static final class CertificateAuthoritySO {
		public static final FieldDescriptor STATE		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "state", FieldType.Token);
		
		public static final FieldDescriptor FINGERPRINT 	= new FieldDescriptor(DomainObjectType.CertificateAuthority, "fingerprint", FieldType.String);
		public static final FieldDescriptor SUBJECT 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "subject", FieldType.String);
		public static final FieldDescriptor INFO	 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "info", FieldType.Text);
		public static final FieldDescriptor FROM	 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "from", FieldType.Date);
		public static final FieldDescriptor TO		 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "to", FieldType.Date);
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Boolean active = Boolean.TRUE;
	private String name;
	private String x509certificateId;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public CertificateAuthorityDO() {
		super();
	}
	
	public CertificateAuthorityDO( CertificateAuthorityDO original ) {
		setId(original.getId());
		setActive(original.isActive());
		setName(original.getName());
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends DomainObject> E copy() {
		return (E) new CertificateAuthorityDO(this);
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public DomainObjectType getType() {
		return DomainObjectType.DnsResolverList;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		CertificateAuthorityDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE, holder));
		setX509certificateId(conditionalSet(getX509certificateId(), o.getX509certificateId(), F_CERTIFICATE_ID,holder));
		return holder;
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryField(isActive(), F_ACTIVE, ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getX509certificateId(), F_CERTIFICATE_ID, ERROR.MISSING, errors);
		return errors;
	}

	@Override
	public void gatherSearchFields(ObjectSearchContext ctx, ObjectRegistry registry) {
		ctx.sof(this, CertificateAuthoritySO.STATE, isActive() ? SearchableObjectField.TOKEN_TRUSTED : SearchableObjectField.TOKEN_REVOKED);

		X509CertificateDO cert = registry.getX509Certificate(getX509certificateId());
		if ( cert != null ) {
			PKIXCertificate c = cert.getCertificate();
			ctx.sof(this, CertificateAuthoritySO.FINGERPRINT, c.getFingerprint());
			ctx.sof(this, CertificateAuthoritySO.FROM, c.getNotBefore());
			ctx.sof(this, CertificateAuthoritySO.TO, c.getNotAfter());
			ctx.sof(this, CertificateAuthoritySO.INFO, c.getInfo());
		}
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private <E extends DomainObject> CertificateAuthorityDO narrow( E other ) {
		return (CertificateAuthorityDO)other;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getX509certificateId() {
		return x509certificateId;
	}

	public void setX509certificateId(String x509certificateId) {
		this.x509certificateId = x509certificateId;
	}

}
