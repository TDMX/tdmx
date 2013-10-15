package org.tdmx.console.layout;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.tdmx.console.base.CustomSession;

public final class TopHeader extends Panel {

	public TopHeader(String id, String title, WebPage page) {
		super(id);

		add(new Label("userName", ((CustomSession) getSession()).getUser().getFirstName()
				+ " "
				+ ((CustomSession) getSession()).getUser().getLastName()
				+ " ("
				+ ((CustomSession) getSession()).getUser().getLoginName() + ")"));

		Link logOutLink = new Link("logOut") {
			@Override
			public void onClick() {
				((CustomSession) getSession()).logOut();
				setResponsePage(getApplication().getHomePage());

			}
		};

		if (!((CustomSession) getSession()).isLoggedIn()) {
			logOutLink.setVisible(false);
		}

		add(logOutLink);
	}
	
	@Override
	public boolean isVisible() {
		return ((CustomSession) getSession()).isLoggedIn();
	}
	
}
