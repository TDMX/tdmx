package org.tdmx.client.crypto.certificate;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.NameConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.scheme.CryptoException;


public class CertificateAuthorityUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param req
	 * @return
	 * @throws CryptoCertificateException 
	 */
	public static PKIXCredential createCertificateAuthority( PKIXCertificateAuthorityRequest req ) throws CryptoCertificateException {
		KeyPair kp = null;
		try {
			kp = req.getKeyAlgorithm().generateNewKeyPair();
		} catch (CryptoException e1) {
			// TODO 
		}
		
		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();
		
		X500NameBuilder subjectBuilder = new X500NameBuilder();
		subjectBuilder.addRDN(BCStyle.C, req.getCountry());
		subjectBuilder.addRDN(BCStyle.O, req.getOrg());
		subjectBuilder.addRDN(BCStyle.CN, req.getCn());
		X500Name subject = subjectBuilder.build();
		X500Name issuer = subject;
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
				issuer, 
				new BigInteger("1"), 
				req.getNotBefore().getTime(),
				req.getNotAfter().getTime(), 
				subject, 
				publicKey);

		try {
			BasicConstraints cA = new BasicConstraints(0);
			certBuilder.addExtension(Extension.basicConstraints, true, cA);

			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
			certBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(publicKey));
			certBuilder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(publicKey));

			KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature|KeyUsage.keyCertSign);
			certBuilder.addExtension(Extension.keyUsage, false, ku);
			
			//RFC5280 http://tools.ietf.org/html/rfc5280#section-4.2.1.10
			List<GeneralSubtree> nameConstraints = new ArrayList<>();
			if ( req.isSubjectNameContraint() ) {
				GeneralName snc = new GeneralName(GeneralName.directoryName, subject);
				GeneralSubtree snSubtree = new GeneralSubtree(snc,new BigInteger("0"),null);
				nameConstraints.add(snSubtree);
			}
			if ( req.getDnsNameConstraints() != null ) {
				for( int i = 0; i < req.getDnsNameConstraints().size(); i++) {
					String dnsName = req.getDnsNameConstraints().get(i);
					GeneralName gn = new GeneralName(GeneralName.dNSName, dnsName);
					GeneralSubtree domainSubtree = new GeneralSubtree(gn,new BigInteger("0"),null); 
					nameConstraints.add(domainSubtree);
				}
			}
			if ( nameConstraints.size() > 0) {
				GeneralSubtree[] ncs = nameConstraints.toArray(new GeneralSubtree[0]);
				NameConstraints nc = new NameConstraints(ncs, null);
				certBuilder.addExtension(Extension.nameConstraints, true, nc);
			}
			
			ContentSigner signer = SignatureAlgorithm.getContentSigner(privateKey, SignatureAlgorithm.SHA_256_RSA);
			byte[] certBytes = certBuilder.build(signer).getEncoded();
			
			PKIXCertificate c = CertificateIOUtils.decodeCertificate(certBytes);
			
			return new PKIXCredential(c, privateKey);
		} catch (CertIOException e) {
			//TODO
		} catch (NoSuchAlgorithmException e) {
			//TODO
		} catch (IOException e) {
			//TODO
		}
		
		return null;
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

}
