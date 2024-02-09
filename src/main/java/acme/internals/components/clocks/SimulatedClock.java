/*
 * SimulatedClock.java
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

import lombok.CustomLog;

@CustomLog
public class SimulatedClock extends AbstractClock {

	// Constructors -----------------------------------------------------------

	public SimulatedClock() {
		super();

		this.currentMillis = super.getBaseMillis();
	}

	// Internal state ---------------------------------------------------------


	private long currentMillis;

	// Clock interface --------------------------------------------------------


	@Override
	public ZoneId getZone() {
		ZoneId result;

		result = ZoneId.of("UTC");

		return result;
	}

	@Override
	public Clock withZone(final ZoneId zoneId) {
		assert zoneId != null;

		Clock result;

		result = new SimulatedClock();

		return result;
	}

	@Override
	public Instant instant() {
		Instant result;
		long millis;

		millis = this.currentMillis;
		result = Instant.ofEpochMilli(millis);

		return result;
	}

	// Business methods -------------------------------------------------------

	@Override
	public void reset() {
		long millis;

		millis = this.getBaseMillis();
		this.setCurrentMillis(millis);
	}

	@Override
	public void tick(final long amount, final ChronoUnit unit) {
		assert unit != null;

		long millis;

		millis = this.currentMillis + amount * unit.getDuration().toMillis();
		this.setCurrentMillis(millis);
	}

	@Override
	public void tick(final int days, final int hours, final int minutes, final int seconds) {
		long millis;

		millis = this.currentMillis + //
			days * ChronoUnit.DAYS.getDuration().toMillis() + //
			hours * ChronoUnit.HOURS.getDuration().toMillis() + //
			minutes * ChronoUnit.MINUTES.getDuration().toMillis() + //
			seconds * ChronoUnit.SECONDS.getDuration().toMillis();
		this.setCurrentMillis(millis);
	}

	@Override
	public long getCurrentMillis() {
		return this.currentMillis;
	}

	@Override
	public void setCurrentMillis(final long millis) {
		assert millis >= 0;

		Date moment;

		this.currentMillis = millis;

		moment = new Date(millis);
		SimulatedClock.logger.debug("Changing current moment to '{}'.", moment);
	}

	@Override
	public Date getCurrentMoment() {
		Date result;

		result = new Date(this.currentMillis);

		return result;
	}

	@Override
	public void setCurrentMoment(final Date moment) {
		assert moment != null;

		this.currentMillis = moment.getTime();

		SimulatedClock.logger.debug("Changing current moment to '{}'.", moment);
	}

}
