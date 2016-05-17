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

package org.tdmx.lib.control.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.tdmx.lib.control.domain.MaxValue;

/**
 * The implementation of {@link UniqueIdService}.
 * 
 * @author Peter Klauser
 * 
 */
public class UniqueIdServiceImpl implements UniqueIdService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	private static final int DIGIT = 10;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(UniqueIdServiceImpl.class);

	private MaxValueService maxValueService;
	private int batchSize;
	private String maxValueKey;
	private long initialLowValue;

	private final Object syncObject = new Object();
	private long cachedMaxValue = 0;
	private long lastCachedMaxValue = 0;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void init() {
		MaxValue mv = getMaxValueService().findById(getMaxValueKey());
		if (mv == null) {
			mv = new MaxValue();
			mv.setKey(getMaxValueKey());
			mv.setValue(getInitialLowValue());
			getMaxValueService().createOrUpdate(mv);
		}
	}

	@Override
	public String getNextId() {
		long result = 0;
		synchronized (syncObject) {
			if (cachedMaxValue == 0 || cachedMaxValue >= lastCachedMaxValue) {
				MaxValue mv = getMaxValueService().increment(getMaxValueKey(), getBatchSize());
				lastCachedMaxValue = mv.getValue();
				cachedMaxValue = mv.getValue() - getBatchSize() + 1;
			} else {
				cachedMaxValue += 1;
			}
			result = cachedMaxValue; // assign
		}
		int checkDigit = calculateCheckDigit(result);
		Long oid = (result * DIGIT) + checkDigit;

		String id = oid.toString();
		log.info("Generated " + getMaxValueKey() + "=" + id);
		return id;
	}

	@Override
	public boolean isValid(String accountId) {
		if (!StringUtils.hasText(accountId)) {
			throw new IllegalArgumentException("missing accountId");
		}
		String oid = accountId;
		char[] chars = oid.toCharArray();
		if (chars.length <= 1) {
			return false;
		}
		int digit = calculateCheckDigit(chars);
		int providedDigit = Character.digit(chars[chars.length - 1], DIGIT);
		return providedDigit == digit;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private int calculateCheckDigit(long value) {
		String oid = "" + value + " ";
		// note space needed because it is the place of the check digit when we check.
		char[] chars = oid.toCharArray();
		return calculateCheckDigit(chars);
	}

	private int calculateCheckDigit(char[] chars) {
		int oddSum = 0;
		for (int i = 1; i < chars.length - 1; i += 2) {
			oddSum += Character.digit(chars[i], DIGIT);
		}
		int evenSum = 0;
		for (int i = 0; i < chars.length - 1; i += 2) {
			evenSum += Character.digit(chars[i], DIGIT);
		}
		int totalSum = oddSum + evenSum;
		return (DIGIT - (totalSum % DIGIT)) % DIGIT;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public MaxValueService getMaxValueService() {
		return maxValueService;
	}

	public void setMaxValueService(MaxValueService maxValueService) {
		this.maxValueService = maxValueService;
	}

	public String getMaxValueKey() {
		return maxValueKey;
	}

	public void setMaxValueKey(String maxValueKey) {
		this.maxValueKey = maxValueKey;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public long getInitialLowValue() {
		return initialLowValue;
	}

	public void setInitialLowValue(long initialLowValue) {
		this.initialLowValue = initialLowValue;
	}

}
