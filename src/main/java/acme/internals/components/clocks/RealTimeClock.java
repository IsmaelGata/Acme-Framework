/*
 * RealTimeClock.java
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class RealTimeClock extends AbstractClock {

	// Constructors -----------------------------------------------------------

	public RealTimeClock() {
		this(ZoneId.of("UTC"));
	}

	public RealTimeClock(final ZoneId zoneId) {
		super();

		assert zoneId != null;

		this.systemClock = Clock.system(zoneId);
	}

	// Internal state ---------------------------------------------------------


	private Clock systemClock;

	// Clock interface --------------------------------------------------------


	@Override
	public ZoneId getZone() {
		ZoneId result;

		result = this.systemClock.getZone();

		return result;
	}

	@Override
	public Clock withZone(final ZoneId zoneId) {
		assert zoneId != null;

		Clock result;

		result = new RealTimeClock(zoneId);

		return result;
	}

	@Override
	public Instant instant() {
		Instant result;
		long millis;

		millis = this.systemClock.millis();
		result = Instant.ofEpochMilli(millis);

		return result;
	}

	// AbstractClock interface ------------------------------------------------

	@Override
	public void reset() {
		assert false : "Cannot reset a real-time clock!";

		throw new UnsupportedOperationException();
	}

	@Override
	public void tick(final long amount, final ChronoUnit unit) {
		assert unit != null;

		assert false : "Cannot tick a real-time clock!";

		throw new UnsupportedOperationException();
	}

	@Override
	public void tick(final int days, final int hours, final int minutes, final int seconds) {
		assert false : "Cannot tick a real-time clock!";

		throw new UnsupportedOperationException();
	}

	@Override
	public long getCurrentMillis() {
		return this.systemClock.millis();
	}

	@Override
	public void setCurrentMillis(final long millis) {
		assert millis >= 0;

		assert false : "Cannot reset a real-time clock!";
	}

	@Override
	public Date getCurrentMoment() {
		Date result;
		long currentMillis;

		currentMillis = this.systemClock.millis();
		result = new Date(currentMillis);

		return result;
	}

	@Override
	public void setCurrentMoment(final Date moment) {
		assert moment != null;

		assert false : "Cannot reset a real-time clock!";
	}

}
