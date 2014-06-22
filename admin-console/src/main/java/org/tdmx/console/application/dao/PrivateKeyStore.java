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
package org.tdmx.console.application.dao;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface PrivateKeyStore {

	public X509Certificate[] getPrivateKeyCertificate(String certId) throws KeyStoreException;

	public PrivateKey getPrivateKey(String certId) throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException;

	public void setPrivateKey(String certId, X509Certificate[] chain, PrivateKey privateKey) throws KeyStoreException;

	public void delete(String certId) throws KeyStoreException;

	public void save() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException;

	public void load() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException;

}
