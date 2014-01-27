/**
 * 
 */
package org.tdmx.lib.control.datasource;

/**
 * 
 * A ValueObject representing a Database Schema and Login.
 *
 */
public class DatabaseConnectionInfo {

	private String username;
	private String password;
	private String url;
	private String driverClassname;
	
	public DatabaseConnectionInfo( String username, String password, String url, String driverClassname ) {
		if ( username == null ) {
			throw new IllegalArgumentException("username");
		}
		if ( password == null ) {
			throw new IllegalArgumentException("password");
		}
		if ( url == null ) {
			throw new IllegalArgumentException("url");
		}
		if ( driverClassname == null ) {
			throw new IllegalArgumentException("driverClassname");
		}
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverClassname = driverClassname;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatabaseConnectionInfo other = (DatabaseConnectionInfo) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the driverclassname
	 */
	public String getDriverClassname() {
		return driverClassname;
	}

}
