package org.tdmx.client.crypto.converters;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.Arrays;

public class ByteArray {

	public static boolean equals( byte[] a, byte[] b ) {
		return Arrays.areEqual(a, b);
	}
	
	public static byte[] clone( byte[] b ) {
		byte[] copy = new byte[b.length];
		System.arraycopy(b, 0, copy, 0, b.length);
		return copy;
	}
	
	public static byte[] append( byte[] first, byte[] second, byte[] third, byte[] forth, byte[] fifth ) {
		byte[] array = new byte[first.length+second.length+third.length+forth.length];
		System.arraycopy(first, 0, array, 0, first.length);
		System.arraycopy(second, 0, array, first.length, second.length);
		System.arraycopy(third, 0, array, first.length+second.length, third.length);
		System.arraycopy(forth, 0, array, first.length+second.length+third.length, forth.length);
		System.arraycopy(fifth, 0, array, first.length+second.length+third.length+forth.length, fifth.length);
		return array;
	}
	
	public static byte[] append( byte[] first, byte[] second, byte[] third, byte[] forth ) {
		byte[] array = new byte[first.length+second.length+third.length+forth.length];
		System.arraycopy(first, 0, array, 0, first.length);
		System.arraycopy(second, 0, array, first.length, second.length);
		System.arraycopy(third, 0, array, first.length+second.length, third.length);
		System.arraycopy(forth, 0, array, first.length+second.length+third.length, forth.length);
		return array;
	}
	
	public static byte[] append( byte[] first, byte[] second, byte[] third ) {
		byte[] array = new byte[first.length+second.length+third.length];
		System.arraycopy(first, 0, array, 0, first.length);
		System.arraycopy(second, 0, array, first.length, second.length);
		System.arraycopy(third, 0, array, first.length+second.length, third.length);
		return array;
	}
	
	public static byte[] append( byte[] first, byte[] second ) {
		byte[] array = new byte[first.length+second.length];
		System.arraycopy(first, 0, array, 0, first.length);
		System.arraycopy(second, 0, array, first.length, second.length);
		return array;
	}
	
	public static byte[] interleave( byte[] first, byte[] second ) {
		byte[] array = new byte[first.length+second.length];
		for( int i = 0; i < first.length && i < second.length; i++) {
			array[i*2] = first[i];
			array[i*2+1] = second[i];
		}
		return array;
	}
	
	public static byte[] subArray( byte[] src, int offset, int len ) {
		byte[] result = new byte[len];
		System.arraycopy(src, offset, result, 0, len);
		return result;
	}
	
	public static byte[] subArray( byte[] src, int offset ) {
		int len = src.length - offset;
		byte[] result = new byte[len];
		System.arraycopy(src, offset, result, 0, len);
		return result;
	}
	
	public static String asHex( byte[] b ) {
		return new String(Hex.encodeHex(b));
	}
	
	public static byte[] fromHex( char[] h ) {
		try {
			return Hex.decodeHex(h);
		} catch (DecoderException e) {
			throw new RuntimeException(e);
		}
	}
}
