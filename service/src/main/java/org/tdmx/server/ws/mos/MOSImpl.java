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
package org.tdmx.server.ws.mos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.sp.mos.GetAddress;
import org.tdmx.core.api.v01.sp.mos.GetAddressResponse;
import org.tdmx.core.api.v01.sp.mos.GetMessageDeliveryStatus;
import org.tdmx.core.api.v01.sp.mos.GetMessageDeliveryStatusResponse;
import org.tdmx.core.api.v01.sp.mos.ListAuthorizedChannelDestination;
import org.tdmx.core.api.v01.sp.mos.ListAuthorizedChannelDestinationResponse;
import org.tdmx.core.api.v01.sp.mos.ListFlow;
import org.tdmx.core.api.v01.sp.mos.ListFlowResponse;
import org.tdmx.core.api.v01.sp.mos.ListService;
import org.tdmx.core.api.v01.sp.mos.ListServiceResponse;
import org.tdmx.core.api.v01.sp.mos.Submit;
import org.tdmx.core.api.v01.sp.mos.SubmitResponse;
import org.tdmx.core.api.v01.sp.mos.Upload;
import org.tdmx.core.api.v01.sp.mos.UploadResponse;
import org.tdmx.core.api.v01.sp.mos.tx.Commit;
import org.tdmx.core.api.v01.sp.mos.tx.CommitResponse;
import org.tdmx.core.api.v01.sp.mos.tx.Forget;
import org.tdmx.core.api.v01.sp.mos.tx.ForgetResponse;
import org.tdmx.core.api.v01.sp.mos.tx.Prepare;
import org.tdmx.core.api.v01.sp.mos.tx.PrepareResponse;
import org.tdmx.core.api.v01.sp.mos.tx.Recover;
import org.tdmx.core.api.v01.sp.mos.tx.RecoverResponse;
import org.tdmx.core.api.v01.sp.mos.tx.Rollback;
import org.tdmx.core.api.v01.sp.mos.tx.RollbackResponse;
import org.tdmx.core.api.v01.sp.mos.ws.MOS;

public class MOSImpl implements MOS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MOSImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public ListAuthorizedChannelDestinationResponse listAuthorizedChannelDestination(
			ListAuthorizedChannelDestination parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListServiceResponse listService(ListService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetAddressResponse getAddress(GetAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubmitResponse submit(Submit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetMessageDeliveryStatusResponse getMessageDeliveryStatus(GetMessageDeliveryStatus parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListFlowResponse listFlow(ListFlow parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UploadResponse upload(Upload parameters) {
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
