/*
 * LogbackCustomConsoleFilter.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogbackCustomConsoleFilter extends Filter<ILoggingEvent> {

	// Filter<ILoggingEvent> interface ----------------------------------------

	@Override
	public FilterReply decide(final ILoggingEvent event) {
		assert event != null;

		FilterReply result;

		if (event.getFormattedMessage().contains("HHH000437"))
			result = FilterReply.DENY;
		else
			result = FilterReply.NEUTRAL;

		return result;
	}

}
