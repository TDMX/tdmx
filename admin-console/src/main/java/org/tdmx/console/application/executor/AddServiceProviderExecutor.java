package org.tdmx.console.application.executor;

import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.service.command.AddServiceProviderCommand;

/**
 * @author Peter
 *
 */
public class AddServiceProviderExecutor extends AbstractExecutor<AddServiceProviderCommand> {

	public AddServiceProviderExecutor(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}
	
	@Override
	public void execute( AddServiceProviderCommand cmd ) {
		
	}
}
