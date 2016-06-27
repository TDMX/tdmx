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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.tdmx.core.system.env.ObfuscationSupport;

/**
 * A DatabasePartition.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "DatabasePartition")
public class DatabasePartition implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final int MAX_PARTITIONID_LEN = 255;
	public static final int MAX_URL_LEN = 255;
	public static final int MAX_USERNAME_LEN = 255;
	public static final int MAX_PASSWORD_LEN = 255;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = MAX_PARTITIONID_LEN, unique = true, nullable = false)
	private String partitionId;

	@Enumerated(EnumType.STRING)
	@Column(length = DatabaseType.MAX_DBTYPE_LEN, nullable = false)
	private DatabaseType dbType;

	@Column(length = Segment.MAX_SEGMENT_LEN, nullable = false)
	private String segment;

	@Column(length = MAX_URL_LEN)
	private String url;

	@Column(length = MAX_USERNAME_LEN)
	private String username;

	@Column(length = MAX_PASSWORD_LEN)
	/**
	 * use {@link #setPassword()} and {@link #getPassword()}
	 */
	private String obfuscatedPassword;
	@Transient
	private String password;

	/**
	 * The sizeFactor determines the space in a DB partition relative to other partitions. The calculation must remain
	 * consistent like being the number of GB in the DB.
	 */
	@Column(nullable = false)
	private int sizeFactor; // immutable
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date activationTimestamp; // immutable
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date deactivationTimestamp; // immutable

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatabasePartition [id=");
		builder.append(id);
		builder.append(", partitionId=");
		builder.append(partitionId);
		builder.append(", dbType=");
		builder.append(dbType);
		builder.append(", segment=");
		builder.append(segment);
		builder.append(", url=");
		builder.append(url);
		builder.append(", username=");
		builder.append(username);
		builder.append(", activationTimestamp=");
		builder.append(activationTimestamp);
		builder.append(", deactivationTimestamp=");
		builder.append(deactivationTimestamp);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * The database partition is active.
	 * 
	 * @return whether the database partition is active.
	 */
	public boolean isActive() {
		return activationTimestamp != null && activationTimestamp.getTime() < System.currentTimeMillis()
				&& deactivationTimestamp == null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPartitionId() {
		return partitionId;
	}

	public void setPartitionId(String partitionId) {
		this.partitionId = partitionId;
	}

	public String getPassword() {
		if (password == null) {
			password = ObfuscationSupport.deobfuscate(getObfuscatedPassword());
		}
		return password;
	}

	public void setPassword(String text) {
		password = text;
		setObfuscatedPassword(ObfuscationSupport.obfuscate(text));
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

	private String getObfuscatedPassword() {
		return obfuscatedPassword;
	}

	private void setObfuscatedPassword(String obfuscatedPassword) {
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
