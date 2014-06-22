/*
 * TDMX - Trusted Domain Messaging eXchanimport java.util.List;
 * 
 * import org.tdmx.client.crypto.certificate.CryptoCertificateException; import
 * org.tdmx.client.crypto.certificate.TrustStoreEntry; ://tdmx.org)
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

import java.util.List;

import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;

public interface SystemTrustStore {

	public List<TrustStoreEntry> getAllTrustedCAs() throws CryptoCertificateException;

	public List<TrustStoreEntry> getAllDistrustedTrustedCAs() throws CryptoCertificateException;

}
