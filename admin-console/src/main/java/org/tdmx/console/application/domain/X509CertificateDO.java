package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.CalendarUtils;


/**
 * An X509Certificate.
 * 
 * @author Peter
 *
 */
public class X509CertificateDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_CERTIFICATE	= new DomainObjectField("certificate", DomainObjectType.X509Certificate);

	public static final class X509CertificateSO {
		public static final FieldDescriptor FINGERPRINT 	= new FieldDescriptor(DomainObjectType.X509Certificate, "fingerprint", FieldType.String);
		public static final FieldDescriptor INFO	 		= new FieldDescriptor(DomainObjectType.X509Certificate, "info", FieldType.Text);
		public static final FieldDescriptor FROM	 		= new FieldDescriptor(DomainObjectType.X509Certificate, "from", FieldType.Date);
		public static final FieldDescriptor TO		 		= new FieldDescriptor(DomainObjectType.X509Certificate, "to", FieldType.Date);
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private PKIXCertificate certificate;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public X509CertificateDO( PKIXCertificate certificate ) throws CryptoCertificateException {
		setId(certificate.getFingerprint());
		this.certificate = certificate;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public DomainObjectType getType() {
		return DomainObjectType.X509Certificate;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		ctx.sof(this, X509CertificateSO.FINGERPRINT, getId());
		ctx.sof(this, X509CertificateSO.FROM, certificate.getNotBefore());
		ctx.sof(this, X509CertificateSO.TO, certificate.getNotAfter());
		ctx.sof(this, X509CertificateSO.INFO, certificate.getInfo());
		setSearchFields(ctx.getSearchFields());
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

	public PKIXCertificate getCertificate() {
		return certificate; 
	}

}
