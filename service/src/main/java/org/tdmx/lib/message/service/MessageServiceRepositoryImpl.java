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

package org.tdmx.lib.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.message.dao.MessageDao;
import org.tdmx.lib.message.domain.Message;

/**
 * Transactional CRUD Services for Message Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class MessageServiceRepositoryImpl implements MessageService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MessageServiceRepositoryImpl.class);

	private MessageDao messageDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "MessageDB")
	public void createOrUpdate(Message message) {
		if (message.getId() != null) {
			Message storedMessage = getMessageDao().loadById(message.getId());
			if (storedMessage != null) {
				getMessageDao().merge(message);
			} else {
				log.warn("Unable to find Message with id " + message.getId());
			}
		} else {
			getMessageDao().persist(message);
		}
	}

	@Override
	@Transactional(value = "MessageDB")
	public void delete(Message message) {
		Message storedMessage = getMessageDao().loadById(message.getId());
		if (storedMessage != null) {
			getMessageDao().delete(storedMessage);
		} else {
			log.warn("Unable to find Message to delete with id " + message.getId());
		}
	}

	@Override
	@Transactional(value = "MessageDB", readOnly = true)
	public Message findByMsgId(String msgId) {
		return getMessageDao().loadByMsgId(msgId);
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

	public MessageDao getMessageDao() {
		return messageDao;
	}

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}

}
