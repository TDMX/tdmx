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
package org.tdmx.client.crypto.scheme;

import java.io.OutputStream;

/**
 * Encryption is performed by calling {@link #getOutputStream()} and writing all plaintext to this stream. The output
 * stream closed before calling {@link #getResult()}.
 * 
 * An Encrypter can be used once only.
 * 
 * The result's {@link CryptoContext} method must eventually be called after the caller has finished using the encrypted
 * data and context.
 * 
 * @author Peter
 * 
 */
public interface Encrypter {

	public OutputStream getOutputStream() throws CryptoException;

	public CryptoContext getResult() throws CryptoException;
}
