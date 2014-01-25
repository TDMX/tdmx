package org.tdmx.core.system.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class StringUtils {

	public static boolean hasText( String text ) {
		return ( text != null && text.length() > 0);
	}

	public static String truncateToMaxLen( String input, int len ) {
		if ( input != null && input.length() > 0 ) {
			if ( input.length() > len ) {
				return input.substring(0, len);
			}
		}
		return input;
	}
	
	public static String inputStreamAsString( InputStream stream, Charset cs) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream,cs));
		StringBuilder sb = new StringBuilder();
		String line = null;
	
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}

		br.close();
		return sb.toString();
	}
}
