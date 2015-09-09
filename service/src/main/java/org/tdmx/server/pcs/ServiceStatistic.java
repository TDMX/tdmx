package org.tdmx.server.pcs;

import org.tdmx.server.ws.session.WebServiceApiName;

public class ServiceStatistic {
	private final WebServiceApiName api;
	private final String httpsUrl;
	private int loadValue;

	public ServiceStatistic(WebServiceApiName api, String httpsUrl, int loadValue) {
		this.api = api;
		this.httpsUrl = httpsUrl;
		this.loadValue = loadValue;
	}

	public WebServiceApiName getApi() {
		return api;
	}

	public String getHttpsUrl() {
		return httpsUrl;
	}

	public int getLoadValue() {
		return loadValue;
	}

	public void setLoadValue(int loadValue) {
		this.loadValue = loadValue;
	}

}