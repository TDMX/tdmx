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

import static org.junit.Assert.assertArrayEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tdmx.core.cli.InputStreamTokenizer;

public class InputStreamTokenizerTest {

	private String[] getTokens(String text) {
		InputStreamTokenizer tokenizer = new InputStreamTokenizer(new StringReader(text));
		List<String> tokens = new ArrayList<>();
		String token = null;
		while ((token = tokenizer.getNextToken()) != null) {
			tokens.add(token);
		}
		return tokens.toArray(new String[0]);
	}

	@Test
	public void testMain() {
		assertArrayEquals(new String[] { "hello", "there" }, getTokens("hello there"));
		assertArrayEquals(new String[] { "cmd", "p1", "=", "v1", "p2", "=", "v2" }, getTokens("cmd p1=v1 p2=v2"));
	}

}
