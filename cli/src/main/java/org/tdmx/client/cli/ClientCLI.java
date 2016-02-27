package org.tdmx.client.cli;
/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */

import java.io.InputStreamReader;

import org.tdmx.client.cli.domain.ActivateDomainAdministratorCredentials;
import org.tdmx.client.cli.domain.ActivateUserCredentials;
import org.tdmx.client.cli.domain.AuthorizeChannel;
import org.tdmx.client.cli.domain.CreateAddress;
import org.tdmx.client.cli.domain.CreateDomain;
import org.tdmx.client.cli.domain.CreateDomainAdministratorCredentials;
import org.tdmx.client.cli.domain.CreateService;
import org.tdmx.client.cli.domain.CreateUserCredentials;
import org.tdmx.client.cli.domain.DeactivateDomainAdministratorCredentials;
import org.tdmx.client.cli.domain.DeactivateUserCredentials;
import org.tdmx.client.cli.domain.DeleteAddress;
import org.tdmx.client.cli.domain.DeleteDomain;
import org.tdmx.client.cli.domain.DeleteService;
import org.tdmx.client.cli.domain.SearchAddress;
import org.tdmx.client.cli.domain.SearchChannel;
import org.tdmx.client.cli.domain.SearchDomain;
import org.tdmx.client.cli.domain.SearchDomainAdministratorCredentials;
import org.tdmx.client.cli.domain.SearchService;
import org.tdmx.client.cli.domain.SearchUserCredentials;
import org.tdmx.client.cli.domain.SuspendDomainAdministratorCredentials;
import org.tdmx.client.cli.domain.SuspendUserCredentials;
import org.tdmx.client.cli.trust.AddDistrust;
import org.tdmx.client.cli.trust.AddTrust;
import org.tdmx.client.cli.trust.CollectUntrust;
import org.tdmx.client.cli.trust.DeleteDistrust;
import org.tdmx.client.cli.trust.DeleteTrust;
import org.tdmx.client.cli.trust.DeleteUntrust;
import org.tdmx.client.cli.trust.SearchDistrust;
import org.tdmx.client.cli.trust.SearchTrust;
import org.tdmx.client.cli.trust.SearchUntrust;
import org.tdmx.client.cli.zone.CheckScs;
import org.tdmx.client.cli.zone.CreateZone;
import org.tdmx.client.cli.zone.CreateZoneAdministratorCredentials;
import org.tdmx.client.cli.zone.DeleteZone;
import org.tdmx.client.cli.zone.DescribeDns;
import org.tdmx.client.cli.zone.DescribeZone;
import org.tdmx.client.cli.zone.DescribeZoneAdministratorCredentials;
import org.tdmx.client.cli.zone.DownloadScs;
import org.tdmx.client.cli.zone.LookupDns;
import org.tdmx.client.cli.zone.ModifyZone;
import org.tdmx.client.cli.zone.Route53Dns;
import org.tdmx.core.cli.CliParser;
import org.tdmx.core.cli.CliRunnerImpl;
import org.tdmx.core.cli.CommandDescriptor;
import org.tdmx.core.cli.CommandDescriptorFactory;
import org.tdmx.core.cli.CommandDescriptorFactoryImpl;
import org.tdmx.core.cli.InputStreamTokenizer;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.cli.runtime.CommandExecutableFactory;

/**
 * A CLI for local client administration.
 * 
 * @author Peter
 *
 */
public class ClientCLI {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	//@formatter:off
	@SuppressWarnings("unchecked")
	private static final Class<? extends CommandExecutable>[] commandClasses = new Class[] { 
			CreateZone.class, ModifyZone.class, DeleteZone.class, DescribeZone.class,
			DescribeDns.class, Route53Dns.class, LookupDns.class,
			CheckScs.class, DownloadScs.class,
			CreateZoneAdministratorCredentials.class, DescribeZoneAdministratorCredentials.class,  
			CreateDomain.class, SearchDomain.class, DeleteDomain.class,
			SearchDomainAdministratorCredentials.class, CreateDomainAdministratorCredentials.class, ActivateDomainAdministratorCredentials.class, SuspendDomainAdministratorCredentials.class, DeactivateDomainAdministratorCredentials.class,
			CreateService.class, SearchService.class, DeleteService.class,
			CreateAddress.class, SearchAddress.class, DeleteAddress.class,
			CollectUntrust.class, SearchUntrust.class, DeleteUntrust.class,
			SearchTrust.class, DeleteTrust.class, AddTrust.class,
			SearchDistrust.class, DeleteDistrust.class, AddDistrust.class,
			AuthorizeChannel.class, SearchChannel.class,
			SearchUserCredentials.class, CreateUserCredentials.class, ActivateUserCredentials.class, SuspendUserCredentials.class, DeactivateUserCredentials.class};
	//@formatter:on
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private ClientCLI() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static void main(String[] args) {
		final CommandDescriptorFactory commandDescriptorFactory = new CommandDescriptorFactoryImpl(commandClasses);

		final CommandExecutableFactory commandExecutableFactory = new CommandExecutableFactory() {

			@Override
			public CommandExecutable getCommandExecutable(String cmdName) {
				CommandDescriptor cmd = commandDescriptorFactory.getCommand(cmdName);
				if (cmd == null) {
					throw new IllegalArgumentException("No cmd " + cmdName);
				}
				Class<? extends CommandExecutable> cmdClazz = cmd.getClazz();
				try {
					return cmdClazz.newInstance();
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		};
		CliRunnerImpl runner = new CliRunnerImpl();
		runner.setCommandExecutableFactory(commandExecutableFactory);

		CliParser cliparser = new CliParser();
		cliparser.setCommandDescriptorFactory(commandDescriptorFactory);
		cliparser.setCliRunner(runner);

		InputStreamTokenizer tokenizer = args.length > 0 ? new InputStreamTokenizer(args)
				: new InputStreamTokenizer(new InputStreamReader(System.in));

		cliparser.process(tokenizer, System.out, System.err);

	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
