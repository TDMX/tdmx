package org.tdmx.console.layout;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.tdmx.console.pages.domain.DomainPage;
import org.tdmx.console.pages.profile.ProfilePage;


public class LeftNavigation extends Panel {

	public LeftNavigation(String id) {
        super(id);
        
        add(new BookmarkablePageLink("profilePageLink", ProfilePage.class));
        add(new BookmarkablePageLink("domainPageLink", DomainPage.class));
    }
	
	@Override
	public boolean isVisible() {
		return false;
	}

}
