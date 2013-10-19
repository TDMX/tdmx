package org.tdmx.console.pages.profile;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.tdmx.console.base.IProtectedPage;
import org.tdmx.console.domain.User;
import org.tdmx.console.layout.BasePage;


public final class ProfilePage extends BasePage implements IProtectedPage {

	
	public ProfilePage() {
		createComponents();
		
	}

	private void createComponents() {
		
		User user = getCustomSession().getUser();
		
		add(new Label("name", Model.of(user.getFirstName() + " " + user.getLastName())));
		add(new Label("userName", Model.of(user.getLoginName())));
		add(new Label("email", Model.of(user.getEmail())));
	}
	
	

}
