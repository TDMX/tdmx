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
package org.tdmx.client.crypto.stream;

import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoException;

/**
 * A calculator of a MAC of a series of MAC.
 * 
 * MacOfMac[n] := MAC(MAC-chunk-n || MacOfMac[n-1])
 * 
 * MacOfMac[0] := Mac-chunk-0
 * 
 * @author Peter
 * 
 */
public class MacOfMacCalculator {

	private final DigestAlgorithm digestAlgorithm;
	private byte[] macOfMacs = null;

	public MacOfMacCalculator(DigestAlgorithm digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	public void addChunkMac(byte[] mac) {
		if (macOfMacs == null) {
			// initial chunk mac
			macOfMacs = mac;
		} else {
			byte[] nextData = ByteArray.append(mac, macOfMacs);
			try {
				macOfMacs = digestAlgorithm.kdf(nextData);
			} catch (CryptoException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public DigestAlgorithm getDigestAlgorithm() {
		return digestAlgorithm;
	}

	public byte[] getMacOfMacs() {
		return macOfMacs;
	}

	public boolean checkMacOfMacs(byte[] checkMac) {
		return ByteArray.equals(macOfMacs, checkMac);
	}

}
