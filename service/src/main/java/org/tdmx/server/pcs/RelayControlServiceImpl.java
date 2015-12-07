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
package org.tdmx.server.pcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.pcs.protobuf.Broadcast.Channel;
import org.tdmx.server.pcs.protobuf.Broadcast.RelayMessage;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

/**
 * The PCS implementation of the {@link RelayControlServiceListener}
 * 
 * @author Peter
 *
 */
public class RelayControlServiceImpl implements RelayControlServiceListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayControlServiceImpl.class);

	// internal

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void registerRelayServer(RpcClientChannel ros) {
		// TODO Auto-generated method stub
		log.info("registerRelayServer " + ros.getPeerInfo());
	}

	@Override
	public void unregisterRelayServer(RpcClientChannel ros) {
		// TODO Auto-generated method stub
		log.info("unregisterRelayServer " + ros.getPeerInfo());

	}

	@Override
	public void notifyIdleSession(RpcClientChannel ros, Channel channel, String mrsSessionId) {
		// TODO Auto-generated method stub
		log.info("notifyIdleSession " + ros.getPeerInfo());

	}

	@Override
	public void relayMessage(RelayMessage msg) {
		// TODO Auto-generated method stub
		log.info("relayMessage " + msg.getChannel());

	}

	@Override
	public void notifyLoad(RpcClientChannel ros, int currentLoad) {
		// TODO Auto-generated method stub
		log.info("notifyLoad " + ros.getPeerInfo() + " is " + currentLoad);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
