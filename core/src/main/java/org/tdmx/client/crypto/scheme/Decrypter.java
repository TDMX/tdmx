/*
 * TDMX - Trusted Domain Messaging eXcimport java.io.InputStream; messaging between separate corporations via
 * interoperable cloud service providers.
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

import java.io.InputStream;

/**
 * The Decrypter provides an InputStream which provides plaintext stream to the caller of
 * {@link #getInputStream(InputStream, byte[])}.
 * 
 * A Decrypter can be used once only.
 * 
 * The InputStream must be read fully until {@link InputStream#read()} gives -1. Only if the stream is fully consumed,
 * are all signatures and data integrity checks performed.
 * 
 * There are no further data integrity checks performed on {@link InputStream#close()}.
 * 
 * @author Peter
 * 
 */
public interface Decrypter {

	/**
	 * The caller should always call {@link InputStream#close()} on the returned plaintext input stream.
	 * 
	 * @param encryptedData
	 * @param encryptionContext
	 * @return plaintext inputstream
	 * @throws CryptoException
	 */
	public InputStream getInputStream(InputStream encryptedData, byte[] encryptionContext) throws CryptoException;

}
