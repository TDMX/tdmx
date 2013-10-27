package org.tdmx.console.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.console.application.Administration;
import org.tdmx.console.application.service.ProxyService;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Job;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.Proxy;
import org.tdmx.console.domain.User;

public class UIServiceImpl implements UIService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Administration admin;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public List<Domain> listDomains( ) {
		return getAdmin().getObjectRegistry().getDomains();
	}

	@Override
	public List<Job> getJobs() {
		List<Job> list = new ArrayList<>();
		for( org.tdmx.console.application.job.BackgroundJob j : getAdmin().getBackgroundJobs()) {
			list.add(new Job(j));
		}
		return list;
	}

	@Override
	public List<Problem> getProblems() {
		List<Problem> list = new ArrayList<>();
		for( org.tdmx.console.application.domain.ProblemDO p : getAdmin().getProblemRegistry().getProblems()) {
			list.add(new Problem(p));
		}
		return list;
	}

	@Override
	public boolean hasProblems() {
		return getAdmin().getProblemRegistry().getProblems().size() > 0;
	}

	@Override
	public void deleteAllProblems() {
		 getAdmin().getProblemRegistry().deleteAllProblems();
	}

	@Override
	public void deleteProblem(String id) {
		 getAdmin().getProblemRegistry().deleteProblem(id);
	}

	@Override
	public int getNumberOfProblems() {
		return  getAdmin().getProblemRegistry().getProblems().size();
	}

	@Override
	public Problem getMostRecentProblem() {
		org.tdmx.console.application.domain.ProblemDO p = getAdmin().getProblemRegistry().getLastProblem();
		return p != null ? new Problem(p) : null;
	}

	@Override
	public User authenticate(String login, String password) {
		if ("secret".equals(password) ) {
			User user = new User(login, "George", "Bush", "noreply@mycompany.com", new Date());
			return user;
		}
		return null;
	}

	@Override
	public List<String> getProxyTypes() {
		return getAdmin().getProxyService().getProxyTypes();
	}

	@Override
	public List<Proxy> getProxies() {
		List<Proxy> list = new ArrayList<>();
		for( org.tdmx.console.application.domain.HttpProxyDO p : getAdmin().getObjectRegistry().getHttpProxies()) {
			list.add(new Proxy(p, getAdmin().getProxyService()));
		}
		return list;
	}

	@Override
	public Proxy lookupProxy(String id) {
		org.tdmx.console.application.domain.HttpProxyDO existing = getAdmin().getObjectRegistry().getProxy(id);
		if ( existing != null ) {
			return new Proxy(existing, getAdmin().getProxyService());
		}
		return null;
	}

	@Override
	public List<ProxyService.ERROR> save(Proxy proxy) {
		org.tdmx.console.application.domain.HttpProxyDO existing = getAdmin().getObjectRegistry().getProxy(proxy.getId());
		if ( existing != null ) {
			return getAdmin().getProxyService().modify(proxy.domain(), existing);
		}
		return getAdmin().getProxyService().create(proxy.domain());
	}

	@Override
	public void deleteProxy(String id) {
		org.tdmx.console.application.domain.HttpProxyDO existing = getAdmin().getObjectRegistry().getProxy(id);
		if ( existing != null ) {
			getAdmin().getProxyService().delete(existing);
		}
		return;
	}


    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public Administration getAdmin() {
		return admin;
	}

	public void setAdmin(Administration admin) {
		this.admin = admin;
	}

}
