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
package org.tdmx.lib.control.job;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.JaxbMarshaller;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.service.control.task.dao.ExceptionType;

public class JobExceptionConverterImpl implements JobExceptionConverter {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(JobExceptionConverterImpl.class);

	private final JaxbMarshaller<ExceptionType> marshaller = new JaxbMarshaller<>(ExceptionType.class, new QName(
			"urn:dao.task.control.service.tdmx.org", "exception"));

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public JobExceptionConverterImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public ExceptionType getException(Job job) {
		if (job.getException() != null) {
			try {
				return marshaller.unmarshal(job.getException());
			} catch (JAXBException e) {
				log.warn("Problem unmarshalling exception message jobId=" + job.getJobId());
			}
		}
		return null;
	}

	@Override
	public void setException(Job job, Throwable t) {
		if (t != null) {
			ExceptionType et = new ExceptionType();
			et.setMessage(t.getMessage());
			et.setType(t.getClass().getName());
			StackTraceElement[] st = t.getStackTrace();
			for (StackTraceElement e : st) {
				et.getStack().add(e.toString());
			}
			try {
				job.setException(marshaller.marshal(et));
			} catch (JAXBException e1) {
				log.warn("Problem marshalling exception message jobId=" + job.getJobId(), t);
			}
		}
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
