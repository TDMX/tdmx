package org.tdmx.client.crypto.converters;

import java.nio.ByteBuffer;

public class NumberToOctetString {

	public static byte[] intToByte(int x) {
		return new byte[] {(byte)x};
	}

	public static int byteToInt(byte[] bytes) {
		return (int)bytes[0];
	}

	public static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
}
