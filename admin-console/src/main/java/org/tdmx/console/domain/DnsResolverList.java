package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.List;

import org.tdmx.console.application.domain.DnsResolverListDO;

public class DnsResolverList implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String id;
	private String name;
	private Boolean active;
	private List<String> hostnames;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public DnsResolverList( DnsResolverListDO o) {
		setId(o.getId());
		setName(o.getName());
		setActive(o.isActive());
		setHostnames(o.getHostnames());
	}
	
	public DnsResolverListDO domain() {
		DnsResolverListDO o = new DnsResolverListDO();
		o.setId(getId());
		o.setName(getName());
		o.setActive(getActive());
		o.setHostnames(getHostnames());
		return o;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}
}
