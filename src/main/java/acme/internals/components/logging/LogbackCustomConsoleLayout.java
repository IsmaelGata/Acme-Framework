/*
 * LogbackCustomConsoleLayout.java
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

import acme.internals.helpers.LoggerHelper;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class LogbackCustomConsoleLayout extends LayoutBase<ILoggingEvent> {

	// LayoutBase<ILoggingEvent> interface ------------------------------------

	@Override
	public String doLayout(final ILoggingEvent event) {
		assert event != null;

		String result;
		String format;

		format = event.getLevel().equals(Level.INFO) ? "!D !E" : "!L !D !E";
		result = LoggerHelper.formatEvent(event, format);
		result = result.replace("\\n", System.lineSeparator());

		return result;
	}

}
