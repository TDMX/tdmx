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
	public byte[] getCRC32( byte[] bytes ) {
		CRC32 c = new CRC32();
		c.update(bytes);
		long trueCRCvalue = c.getValue();
		int intCRCvalue = (int)(trueCRCvalue & 0xffffffff);
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
	public byte[] getCRC32( byte[] first, byte[] second ) {
		CRC32 c = new CRC32();
		c.update(first);
		c.update(second);
		long trueCRCvalue = c.getValue();
		int intCRCvalue = (int)(trueCRCvalue & 0xffffffff);
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(intCRCvalue);
		return bb.array();
	}
	
	
}
