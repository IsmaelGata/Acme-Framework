/*
 * TraceLoggerHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.helpers;

import acme.client.helpers.StringHelper;
import acme.client.testing.Oracle;
import acme.internals.helpers.SerialisationHelper.Format;
import lombok.CustomLog;

@CustomLog
public abstract class TraceLoggerHelper {

	// Constructors -----------------------------------------------------------

	protected TraceLoggerHelper() {
	}


	static {
		String header;

		header = SerialisationHelper.computeHeader(Format.CSV, Oracle.class);
		TraceLoggerHelper.log("{}", header);
	}

	// Business methods -------------------------------------------------------


	public static void log(final String format, final Object... arguments) {
		assert !StringHelper.isBlank(format);
		assert arguments != null;

		TraceLoggerHelper.logger.debug(format, arguments);
	}

}
