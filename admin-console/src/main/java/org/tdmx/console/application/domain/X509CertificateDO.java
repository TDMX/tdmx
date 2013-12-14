package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
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
	private X509Certificate certificate;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public X509CertificateDO( X509Certificate certificate ) throws CryptoCertificateException {
		setId(CertificateIOUtils.getSha1FingerprintAsHex(certificate));
		this.certificate = certificate;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public DomainObjectType getType() {
		return DomainObjectType.X509Certificate;
	}

	public String getInfo() {
		return certificate.toString();
	}
	
	public Calendar getValidFrom() {
		return CalendarUtils.getDate(certificate.getNotBefore());
	}
	
	public Calendar getValidTo() {
		return CalendarUtils.getDate(certificate.getNotAfter());
	}
	
	@Override
	public void gatherSearchFields(ObjectSearchContext ctx, ObjectRegistry registry) {
		ctx.sof(this, X509CertificateSO.FINGERPRINT, getId());
		ctx.sof(this, X509CertificateSO.FROM, getValidFrom());
		ctx.sof(this, X509CertificateSO.TO, getValidTo());
		ctx.sof(this, X509CertificateSO.INFO, getInfo());
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

	public X509Certificate getCertificate() {
		return certificate; 
	}

}
