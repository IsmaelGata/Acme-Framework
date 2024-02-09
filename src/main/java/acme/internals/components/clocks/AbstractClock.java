/*
 * AbstractClock.java
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
public abstract class AbstractClock extends Clock {

	// Constructors -----------------------------------------------------------

	protected AbstractClock() {
		Date currentMoment;

		// HINT: note that calling MomentHelper.getCurrentMoment() would result in an infinite loop!
		currentMoment = new Date();
		this.setBaseMoment(currentMoment);
	}

	// Internal state ---------------------------------------------------------


	private Date baseMoment;

	// Clock interface --------------------------------------------------------


	@Override
	public abstract ZoneId getZone();

	@Override
	public abstract Clock withZone(final ZoneId zoneId);

	@Override
	public abstract Instant instant();

	// Hook methods -----------------------------------------------------------

	public abstract void reset();

	public abstract void tick(final long amount, final ChronoUnit unit);

	public abstract void tick(final int days, final int hours, final int minutes, final int seconds);

	public abstract long getCurrentMillis();

	public abstract void setCurrentMillis(final long millis);

	public abstract Date getCurrentMoment();

	public abstract void setCurrentMoment(final Date moment);

	// Business methods -------------------------------------------------------

	public long getBaseMillis() {
		return this.baseMoment.getTime();
	}

	public void setBaseMillis(final long millis) {
		assert millis >= 0;

		Date newBaseMoment;

		newBaseMoment = new Date(millis);
		this.setBaseMoment(newBaseMoment);
	}

	public Date getBaseMoment() {
		return this.baseMoment;
	}

	public void setBaseMoment(final Date moment) {
		assert moment != null;

		this.baseMoment = moment;
		AbstractClock.logger.debug("Changing base moment to '{}'.", moment);
	}

}
