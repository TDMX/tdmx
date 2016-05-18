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
package org.tdmx.core.cli;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Parse a Character stream into a Token stream. {Token}*<-{char}*
 * 
 * Case 1 Token := '=' ( not escaped with preceding \ )
 * 
 * Case 2 Token := '"' + WhitespaceText + '"' ( where " not escaped with preceding \ )
 * 
 * Case 3 Token := NonWhitespaceText
 * 
 * so --paramName="value 1" becomes 3 tokens "--paramName", "=", "value 1"
 * 
 * @author peter.klauser
 * 
 */
public class InputStreamTokenizer {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private Reader r;
	private String prevToken = null;
	boolean inQuote = false;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public InputStreamTokenizer(String[] args) {
		StringBuilder buf = new StringBuilder();
		for (String arg : args) {
			buf.append(requoteArg(arg)).append(" ");
		}
		this.r = new StringReader(buf.toString());
	}

	public InputStreamTokenizer(Reader r) {
		this.r = r;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Return the next Token or null if the end of the input stream is reached.
	 * 
	 * @return
	 */
	public String getNextToken() {
		// we have read a token already
		if (prevToken != null) {
			String result = prevToken;
			prevToken = null;
			return result;
		}
		char prev = '-';
		int cur;

		StringBuffer buf = new StringBuffer();

		try {
			while ((cur = r.read()) != -1) {
				char ch = (char) cur;
				// ...
				if (inQuote) {
					// if we've reached the end of the quoted string
					if (ch == '\\' && prev != '\\') {
						// consume the escaping character
					} else if (ch == '"' && prev != '\\') {
						inQuote = false;
						break;
					} else {
						buf.append(ch);
					}

				} else {
					// we are not in the middle of a quote, so = or " start quote or whitespace ends token
					// if we have an un-escaped equals we make this a token immediately

					if (ch == '\\' && prev != '\\') {
						// consume the escaping character
					} else if (ch == '"' && prev != '\\') {
						if (buf.length() > 0) {
							inQuote = true;
							break;
						} else {
							inQuote = true;
						}
					} else if (ch == '=' && prev != '\\') {
						if (buf.length() > 0) {
							prevToken = "" + ch;
						} else {
							buf.append(ch);
						}
						break;
					} else if (Character.isWhitespace(ch)) {
						if (buf.length() == 0) {
							// just ignore whitespace if we have no token started

						} else {
							// the buffer up to the space char is the token
							break;
						}
					} else {
						buf.append(ch);
					}

				}
				prev = ch;
			}
		} catch (IOException e) {
			return null;
		}

		return cur == -1 && buf.length() == 0 ? null : buf.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String requoteArg(String arg) {
		if (arg.contains(" ")) {
			return "\"" + arg + "\"";
		}
		return arg;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
