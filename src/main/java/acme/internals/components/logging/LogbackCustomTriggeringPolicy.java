/*
 * LogbackCustomTriggeringPolicy.java
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

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RolloverFailure;

@NoAutoStart
public class LogbackCustomTriggeringPolicy<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E> {

	// DefaultTimeBasedFileNamingAndTriggeringPolicy<E> interface -------------

	@Override
	public void start() {
		assert super.tbrp != null;

		System.out.printf("[Logging to '%s']%n", super.tbrp.getActiveFileName());

		super.start();
		super.nextCheck = 0L;
		this.isTriggeringEvent(null, null);

		try {
			super.tbrp.rollover();
		} catch (final RolloverFailure oops) {
			;
		}
	}

}
