package org.tdmx.client.crypto.scheme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.stream.StreamUtils;

public class SchemeTester {

	public static void testFixedSizeSmallTransfer( Encrypter e, Decrypter d, int length  ) throws CryptoException, IOException {
		byte[] plaintext = EntropySource.getRandomBytes(length);
		
		OutputStream os = e.getOutputStream();
		os.write(plaintext);
		os.close();
		
		CryptoContext result = e.getResult();

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(plaintext.length);
		try {
			StreamUtils.transfer(is, baos);
		} finally {
			is.close();
		}
		byte[] decryptedPlaintext = baos.toByteArray();
		assertArrayEquals(plaintext, decryptedPlaintext);
	}

	public static void testLargeCompressable( Encrypter e, Decrypter d  ) throws CryptoException, IOException {
		OutputStream os = e.getOutputStream();
		PrintWriter pw = new PrintWriter(os);
		int repeats = 10000000;
		for( int i = 0; i < repeats; i++) {
			String contentLine = "NUM"+i+"\n";
			pw.write(contentLine);
		}
		pw.flush();
		pw.close();
		
		CryptoContext result = e.getResult();

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String strLine;
			int i = 0;
			while ((strLine = br.readLine()) != null)   {
				String contentLine = "NUM"+i;
			  
				assertEquals(contentLine, strLine);
				i++;
			}
			assertEquals( repeats, i);
			
			assertEquals(-1, is.read());
		} finally {
			is.close();
		}
	}
}
