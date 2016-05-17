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
package org.tdmx.client.crypto.algorithm;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CRCAlgorithm {

	/**
	 * Get 4byte CRC32 value for the input bytes
	 * 
	 * @param bytes
	 * @return
	 */
	public byte[] getCRC32(byte[] bytes) {
		CRC32 c = new CRC32();
		c.update(bytes);
		long trueCRCvalue = c.getValue();
		int intCRCvalue = (int) (trueCRCvalue & 0xffffffff);
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(intCRCvalue);
		return bb.array();
	}

	/**
	 * Get 4byte CRC32 value for the input first and second byte[] concatenated.
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	public byte[] getCRC32(byte[] first, byte[] second) {
		CRC32 c = new CRC32();
		c.update(first);
		c.update(second);
		long trueCRCvalue = c.getValue();
		int intCRCvalue = (int) (trueCRCvalue & 0xffffffff);
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(intCRCvalue);
		return bb.array();
	}

}
