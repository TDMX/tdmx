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
 * A Database.
 * 
 * @author Peter Klauser
 *
 */
@Entity
@Table(name="Database")
public class Database implements Serializable {

	public static final int MAX_PARTITIONID_LEN = 16;
	public static final int MAX_URL_LEN = 255;
	
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_PARTITIONID_LEN)
	private String partitionId;

	@Column(length = MAX_URL_LEN)
	private String url;
	
	@Enumerated(EnumType.STRING)
	@Column(length = DatabaseType.MAX_DBTYPE_LEN, nullable = false)
	private DatabaseType dbType;
	
	@Column(length = AccountZone.MAX_SEGMENT_LEN, nullable = false)
	private String segment;

	private String username;
	private String driverClassname;
	private String obfuscatedPassword;
	
	private int sizeFactor;
	
	private Date activationTimestamp;
	private Date deactivationTimestamp;
	
	
	
	
	
	
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

}
