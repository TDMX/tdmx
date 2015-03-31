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
package org.tdmx.server.ws.mds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.mds.Download;
import org.tdmx.core.api.v01.mds.DownloadResponse;
import org.tdmx.core.api.v01.mds.GetDestinationAddress;
import org.tdmx.core.api.v01.mds.GetDestinationAddressResponse;
import org.tdmx.core.api.v01.mds.GetFlowTarget;
import org.tdmx.core.api.v01.mds.GetFlowTargetResponse;
import org.tdmx.core.api.v01.mds.ListAuthorizedChannelOrigin;
import org.tdmx.core.api.v01.mds.ListAuthorizedChannelOriginResponse;
import org.tdmx.core.api.v01.mds.ListAuthorizedService;
import org.tdmx.core.api.v01.mds.ListAuthorizedServiceResponse;
import org.tdmx.core.api.v01.mds.ListFlow;
import org.tdmx.core.api.v01.mds.ListFlowResponse;
import org.tdmx.core.api.v01.mds.Receive;
import org.tdmx.core.api.v01.mds.ReceiveResponse;
import org.tdmx.core.api.v01.mds.SetTargetSession;
import org.tdmx.core.api.v01.mds.SetTargetSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.tx.Commit;
import org.tdmx.core.api.v01.tx.CommitResponse;
import org.tdmx.core.api.v01.tx.Forget;
import org.tdmx.core.api.v01.tx.ForgetResponse;
import org.tdmx.core.api.v01.tx.Prepare;
import org.tdmx.core.api.v01.tx.PrepareResponse;
import org.tdmx.core.api.v01.tx.Recover;
import org.tdmx.core.api.v01.tx.RecoverResponse;
import org.tdmx.core.api.v01.tx.Rollback;
import org.tdmx.core.api.v01.tx.RollbackResponse;

public class MDSImpl implements MDS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MDSImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public ForgetResponse forget(Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAuthorizedServiceResponse listAuthorizedService(ListAuthorizedService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetFlowTargetResponse getFlowTarget(GetFlowTarget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListFlowResponse listFlow(ListFlow parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetDestinationAddressResponse getDestinationAddress(GetDestinationAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DownloadResponse download(Download parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReceiveResponse receive(Receive parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAuthorizedChannelOriginResponse listAuthorizedChannelOrigin(ListAuthorizedChannelOrigin parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetTargetSessionResponse setTargetSession(SetTargetSession parameters) {
		// TODO Auto-generated method stub
		return null;
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
