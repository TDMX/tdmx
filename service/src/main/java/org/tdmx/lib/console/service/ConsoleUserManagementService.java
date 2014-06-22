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
package org.tdmx.lib.console.service;

import org.tdmx.lib.console.domain.ConsoleUserStatus;
import org.tdmx.lib.console.domain.UserDetails;

public interface ConsoleUserManagementService {

	public void createUser(UserDetails user, String password);

	public UserDetails findUser(String loginName);

	public void modifyInfo(UserDetails user);

	public void modifyPassword(String loginName, String newPassword);

	public void modifyState(String loginName, ConsoleUserStatus newStatus);

}
