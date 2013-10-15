package org.tdmx.console.pages.domain;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.tdmx.console.base.IProtectedPage;
import org.tdmx.console.layout.BasePage;
import org.tdmx.console.layout.FixBootstrapStylesCssResourceReference;


public final class DomainPage extends BasePage implements IProtectedPage {
	
	public DomainPage() {
		createComponents();
	}
	
	private void createComponents() {
		AjaxLazyLoadPanel domainList = new AjaxLazyLoadPanel("domainList") {
				@Override
				public Component getLazyLoadComponent(String id) {
				        return new DomainListPanel(id);
				}
				
				@Override
				public Component getLoadingComponent(String markupId) {
					IRequestHandler handler = new ResourceReferenceRequestHandler(
							FixBootstrapStylesCssResourceReference.INDICATOR);
						return new Label(markupId, "<img alt=\"Loading...\" src=\"" +
							RequestCycle.get().urlFor(handler) + "\"/>").setEscapeModelStrings(false);
				}
			};
		domainList.setOutputMarkupId(true);
		
		addOrReplace(domainList);
	}
	


}
