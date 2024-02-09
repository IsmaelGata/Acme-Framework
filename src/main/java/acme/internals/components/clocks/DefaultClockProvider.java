/*
 * DefaultClockProvider.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.clocks;

import java.time.Clock;

import javax.validation.ClockProvider;

import org.springframework.stereotype.Component;

import acme.internals.helpers.EnvironmentHelper;

@Component
public class DefaultClockProvider implements ClockProvider {

	// Constructors -----------------------------------------------------------

	protected DefaultClockProvider() {
	}

	// Singleton --------------------------------------------------------------


	public static final DefaultClockProvider	INSTANCE	= new DefaultClockProvider();

	// Internal state ---------------------------------------------------------

	private static AbstractClock				backbone;

	// ClockProvider interface ------------------------------------------------


	@Override
	public Clock getClock() {
		Clock result;
		String profiles;

		if (DefaultClockProvider.backbone == null) {
			profiles = EnvironmentHelper.getRequiredProperty("spring.profiles.active", String.class);
			if (!profiles.contains("production"))
				DefaultClockProvider.backbone = new SimulatedClock();
			else
				DefaultClockProvider.backbone = new RealTimeClock();
		}
		result = DefaultClockProvider.backbone;

		return result;
	}

}
