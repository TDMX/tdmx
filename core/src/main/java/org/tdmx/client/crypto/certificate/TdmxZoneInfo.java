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
package org.tdmx.client.crypto.certificate;

import java.math.BigInteger;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;

/**
 * a TDMX zone info object.
 */
public class TdmxZoneInfo extends ASN1Object {
	// TODO get official ASN1OID for TdmxZoneInfo

	public static final ASN1ObjectIdentifier tdmxZoneInfo = new ASN1ObjectIdentifier("1.2.3.4.5.6.7.8.9");

	private final ASN1Integer version;
	private final DERIA5String zoneRoot;
	private final DERIA5String mrsUrl;

	/**
	 * Create a TdmxZoneInfo descriptor which describes a versioned TDMX relay interface for a normalized domain name
	 * (zone apex).
	 * 
	 * @param version
	 *            currently only 1
	 * @param zoneRoot
	 *            must be uppercase
	 * @param mrsUrl
	 *            the MRS relay URL, ie. http://mrs.serviceprovider.com/api/v01/mrs
	 */
	public TdmxZoneInfo(int version, String zoneRoot, String mrsUrl) {
		this.version = new ASN1Integer(version);
		this.zoneRoot = new DERIA5String(zoneRoot);
		this.mrsUrl = new DERIA5String(mrsUrl);
	}

	public TdmxZoneInfo(ASN1Sequence seq) {
		Enumeration<?> e = seq.getObjects();

		version = (ASN1Integer) e.nextElement();
		zoneRoot = DERIA5String.getInstance(e.nextElement());
		mrsUrl = DERIA5String.getInstance(e.nextElement());
	}

	public static TdmxZoneInfo getInstance(Object o) {
		if (o instanceof TdmxZoneInfo) {
			return (TdmxZoneInfo) o;
		} else if (o instanceof ASN1Sequence) {
			return new TdmxZoneInfo((ASN1Sequence) o);
		}

		throw new IllegalArgumentException("unknown object in factory: " + o.getClass().getName());
	}

	/**
	 * Produce an object suitable for an ASN1OutputStream.
	 * 
	 * <pre>
	 *  TdmxZoneInfo ::= SEQUENCE {
	 *      version Version,
	 *      zoneRoot IA5String,
	 *      mrsUrl IA5String
	 *  }
	 * </pre>
	 */
	@Override
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector v = new ASN1EncodableVector();

		v.add(version);
		v.add(zoneRoot);
		v.add(mrsUrl);

		return new DERSequence(v);
	}

	public int getVersion() {
		BigInteger bi = version.getValue();
		return bi.intValue();
	}

	public String getZoneRoot() {
		return zoneRoot.getString();
	}

	public String getMrsUrl() {
		return mrsUrl.getString();
	}

}
