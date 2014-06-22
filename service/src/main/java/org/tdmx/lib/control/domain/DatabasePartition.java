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
package org.tdmx.lib.control.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A DatabasePartition.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "DatabasePartition")
public class DatabasePartition implements Serializable {

	public static final int MAX_PARTITIONID_LEN = 16;
	public static final int MAX_URL_LEN = 255;
	public static final int MAX_USERNAME_LEN = 255;
	public static final int MAX_PASSWORD_LEN = 255;

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_PARTITIONID_LEN)
	private String partitionId;

	@Enumerated(EnumType.STRING)
	@Column(length = DatabaseType.MAX_DBTYPE_LEN, nullable = false)
	private DatabaseType dbType;

	@Column(length = AccountZone.MAX_SEGMENT_LEN, nullable = false)
	private String segment;

	@Column(length = MAX_URL_LEN, nullable = false)
	private String url;

	@Column(length = MAX_USERNAME_LEN, nullable = false)
	private String username;
	@Column(length = MAX_PASSWORD_LEN, nullable = false)
	private String obfuscatedPassword;

	@Column(nullable = false)
	private int sizeFactor; // immutable
	@Column
	private Date activationTimestamp; // immutable
	@Column
	private Date deactivationTimestamp; // immutable

	public String getPartitionId() {
		return partitionId;
	}

	public void setPartitionId(String partitionId) {
		this.partitionId = partitionId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public DatabaseType getDbType() {
		return dbType;
	}

	public void setDbType(DatabaseType dbType) {
		this.dbType = dbType;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getObfuscatedPassword() {
		return obfuscatedPassword;
	}

	public void setObfuscatedPassword(String obfuscatedPassword) {
		this.obfuscatedPassword = obfuscatedPassword;
	}

	public int getSizeFactor() {
		return sizeFactor;
	}

	public void setSizeFactor(int sizeFactor) {
		this.sizeFactor = sizeFactor;
	}

	public Date getActivationTimestamp() {
		return activationTimestamp;
	}

	public void setActivationTimestamp(Date activationTimestamp) {
		this.activationTimestamp = activationTimestamp;
	}

	public Date getDeactivationTimestamp() {
		return deactivationTimestamp;
	}

	public void setDeactivationTimestamp(Date deactivationTimestamp) {
		this.deactivationTimestamp = deactivationTimestamp;
	}

}