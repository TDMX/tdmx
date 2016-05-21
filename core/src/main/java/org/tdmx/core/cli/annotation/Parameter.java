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
package org.tdmx.core.cli.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter of command
 *
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
	/**
	 * @return Default value which is set as the parameter value if no value is supplied.
	 */
	String defaultValue() default "";

	/**
	 * @return Default value of the parameter, which is null at runtime if no value is supplied.
	 */
	String defaultValueText() default "";

	/**
	 * @return String description of option which is displayed in usage
	 */
	String description() default "";

	/**
	 * @return Parameter name.
	 */
	String name();

	/**
	 * @return True if parameter has to be specified.
	 */
	boolean required() default false;

	/**
	 * @return True if default parameter binding is not performed.
	 */
	boolean noDefault() default false;

	/**
	 * @return whether the parameter is to be handled as a password and masked in all output.
	 */
	boolean masked() default false;
}
