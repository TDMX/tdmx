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
package org.tdmx.lib.console.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An ConsoleUser is a person who uses the Console to administer a TDMX zone.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ConsoleUser")
public class ConsoleUser implements Serializable {

	public static final int MAX_LOGINNAME_LEN = 255;

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_LOGINNAME_LEN)
	private String loginName;

	private ConsoleUserStatus status;

	private String passwordHash;

	private String firstName;
	private String lastName;
	private String email;

	private Date lastSuccessfulLogin;
	private Date lastFailureAttempt;
	private int numConsecutiveFailures;

}
