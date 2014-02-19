package org.tdmx.lib.zone.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An Zone is a domain name incl. all subdomains thereof, managed by a ServiceProvider
 * 
 * @author Peter Klauser
 *
 */
@Entity
@Table(name="Zone")
public class Zone implements Serializable {

	private static final long serialVersionUID = -128859602084626282L;


	public static final int MAX_NAME_LEN = 255;
	

	@Id
	@Column(length = MAX_NAME_LEN)
	private String zoneApex;

	
	
	
	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

}
