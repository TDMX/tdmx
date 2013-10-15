package org.tdmx.console.base;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.tdmx.console.domain.User;



/**
 * Custom session implementation
 * 
 * @author Tomas Jucius
 *
 */
public class CustomSession extends WebSession {
   	
	private org.tdmx.console.domain.User user;
	
	public CustomSession(WebApplication application, Request request) {
      super(request);
	}
	
	public boolean isLoggedIn() {
		return user != null;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public void logOut() {
	    this.user = null;
	    invalidate();
	}
	
}
