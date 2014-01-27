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
@Table(name="ConsoleUser")
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
	private int  numConsecutiveFailures;

}
