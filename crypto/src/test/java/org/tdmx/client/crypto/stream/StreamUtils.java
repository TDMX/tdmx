package org.tdmx.client.crypto.stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

import org.tdmx.client.crypto.entropy.EntropySource;

public class StreamUtils {

	public static byte[] createRndArray( int length ) {
		byte[] bytes = EntropySource.getRandomBytes(length);
		return bytes;
	}
	
	public static void writeArrayAsArray( OutputStream os, byte[] bytes, int repetitions ) throws IOException {
		for( int i = 0; i < repetitions; i++) {
			os.write(bytes);
		}
	}
	
	public static void signArray( Signature s, byte[] bytes, int repetitions ) throws SignatureException {
		for( int i = 0; i < repetitions; i++) {
			s.update(bytes);
		}
	}

	public static void transfer( InputStream is, OutputStream os ) throws IOException {
		byte[] buffer = new byte[2048];
		int read = -1;
		while( (read = is.read(buffer )) != -1 ) {
			os.write(buffer, 0, read);
		}
	}
	
	public static void readArrayAsArray( InputStream is, byte[] buffer, byte[] expected, int repetitions ) throws IOException {
		for( int i = 0; i < repetitions; i++) {
			int totalRead = 0;
			int read = 0;
			do {
				read = is.read(buffer, totalRead, buffer.length - totalRead);
				if ( read != -1 ) {
					totalRead += read;
				}
			} while ( read != -1 && totalRead != buffer.length );
			
			if ( totalRead == buffer.length ) {
				assertArrayEquals(expected, buffer);
			} else {
				fail("["+i+"] Read only " + totalRead +" bytes instead of "+buffer.length);
			}
		}
	}
	

}
