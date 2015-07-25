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
import java.util.Hashtable;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.util.Integers;

/**
 * The TdmxCertificateType enumeration. Done in Bouncycastle style - like CRLReason.
 * 
 * <pre>
 * TdmxCertificateType ::= ENUMERATED {
 *  ZAC           (0),
 *  DAC           (1),
 *  UC            (2)
 * }
 * </pre>
 */
public class TdmxCertificateType extends ASN1Object {
	public static final int ZAC = 0;
	public static final int DAC = 1;
	public static final int UC = 2;

	private static final String[] nameString = { "ZAC", "DAC", "UC" };

	private static final Hashtable<Integer, TdmxCertificateType> table = new Hashtable<>();

	private final ASN1Enumerated value;

	public static TdmxCertificateType getInstance(Object o) {
		if (o instanceof TdmxCertificateType) {
			return (TdmxCertificateType) o;
		} else if (o != null) {
			return lookup(ASN1Enumerated.getInstance(o).getValue().intValue());
		}
		return null;
	}

	private TdmxCertificateType(int type) {
		value = new ASN1Enumerated(type);
	}

	@Override
	public String toString() {
		String str;
		int type = getValue().intValue();
		if (type < 0 || type > 2) {
			str = "invalid";
		} else {
			str = nameString[type];
		}
		return "TdmxCertificateType: " + str;
	}

	public BigInteger getValue() {
		return value.getValue();
	}

	@Override
	public ASN1Primitive toASN1Primitive() {
		return value;
	}

	public static TdmxCertificateType lookup(int value) {
		Integer idx = Integers.valueOf(value);

		if (!table.containsKey(idx)) {
			table.put(idx, new TdmxCertificateType(value));
		}

		return table.get(idx);
	}
}