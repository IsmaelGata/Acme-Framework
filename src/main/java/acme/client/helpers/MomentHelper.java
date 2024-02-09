/*
 * MomentHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.helpers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import acme.internals.components.clocks.AbstractClock;
import acme.internals.components.clocks.DefaultClockProvider;
import acme.internals.components.clocks.RealTimeClock;
import acme.internals.components.clocks.SimulatedClock;
import acme.internals.helpers.EnvironmentHelper;

public abstract class MomentHelper {

	// Constructors -----------------------------------------------------------

	protected MomentHelper() {
	}


	static {
		MomentHelper.clock = (AbstractClock) DefaultClockProvider.INSTANCE.getClock();
	}

	// Internal state ---------------------------------------------------------

	private static AbstractClock clock;

	// Clock properties -------------------------------------------------------


	public static AbstractClock getClock() {
		return MomentHelper.clock;
	}

	public static boolean isRealTimeClock() {
		boolean result;

		result = MomentHelper.clock instanceof RealTimeClock;
		return result;
	}

	public static boolean isSimulatedClock() {
		boolean result;

		result = MomentHelper.clock instanceof SimulatedClock;
		return result;
	}

	// Base moment properties -------------------------------------------------

	public static Date getBaseMoment() {
		return MomentHelper.clock.getBaseMoment();
	}

	public static long getBaseMillis() {
		return MomentHelper.clock.getBaseMillis();
	}

	public static void setBaseMoment(final Date baseMoment) {
		MomentHelper.clock.setBaseMoment(baseMoment);
	}

	// Current moment properties ----------------------------------------------

	public static Date getCurrentMoment() {
		return MomentHelper.clock.getCurrentMoment();
	}

	public static long getCurrentMillis() {
		return MomentHelper.clock.getCurrentMillis();
	}

	public static void setCurrentMoment(final Date moment) {
		assert moment != null;
		assert MomentHelper.isSimulatedClock();

		MomentHelper.clock.setCurrentMoment(moment);
	}

	// Ticking methods --------------------------------------------------------

	public static void initialise() {
		AbstractClock clock;
		String value;
		Date moment;

		clock = (AbstractClock) DefaultClockProvider.INSTANCE.getClock();
		if (clock instanceof SimulatedClock) {
			value = EnvironmentHelper.getRequiredProperty("acme.runtime.base-moment", String.class);
			moment = MomentHelper.parse(value, "yyyy/MM/dd HH:mm");
			clock.setBaseMoment(moment);
			clock.reset();
		}
	}

	public static void reset() {
		assert MomentHelper.isSimulatedClock();

		MomentHelper.clock.reset();
	}
	public static void tick(final long amount, final ChronoUnit unit) {
		assert unit != null;
		assert MomentHelper.isSimulatedClock();

		MomentHelper.clock.tick(amount, unit);
	}

	public static void tick(final int days, final int hours, final int minutes, final int seconds) {
		assert MomentHelper.isSimulatedClock();

		MomentHelper.clock.tick(days, hours, minutes, seconds);
	}

	// Delta methods ----------------------------------------------------------

	public static Date deltaFromBaseMoment(final long amount, final ChronoUnit unit) {
		assert unit != null;

		Date result;
		long base, offset;

		base = MomentHelper.getBaseMillis();
		offset = amount * unit.getDuration().toMillis();
		result = new Date(base + offset);

		return result;
	}

	public static Date deltaFromCurrentMoment(final long amount, final ChronoUnit unit) {
		assert unit != null;

		Date result;
		long current, offset;

		current = MomentHelper.clock.getCurrentMillis();
		offset = amount * unit.getDuration().toMillis();
		result = new Date(current + offset);

		return result;
	}

	public static Date deltaFromMoment(final Date moment, final long amount, final ChronoUnit unit) {
		assert moment != null;
		assert unit != null;

		Date result;
		long delta, millis;

		delta = amount * unit.getDuration().toMillis();
		millis = moment.getTime() + delta;
		result = new Date(millis);

		return result;
	}

	// Comparison methods -----------------------------------------------------

	public static boolean isPast(final Date moment) {
		assert moment != null;

		boolean result;

		result = moment.getTime() < MomentHelper.clock.getCurrentMillis();

		return result;
	}

	public static boolean isPresentOrPast(final Date moment) {
		assert moment != null;

		boolean result;

		result = moment.getTime() <= MomentHelper.clock.getCurrentMillis();

		return result;
	}

	public static boolean isPresent(final Date moment) {
		assert moment != null;

		boolean result;

		result = moment.getTime() == MomentHelper.clock.getCurrentMillis();

		return result;
	}

	public static boolean isPresentOrFuture(final Date moment) {
		assert moment != null;

		boolean result;

		result = moment.getTime() >= MomentHelper.clock.getCurrentMillis();

		return result;
	}

	public static boolean isFuture(final Date moment) {
		assert moment != null;

		boolean result;

		result = moment.getTime() > MomentHelper.clock.getCurrentMillis();

		return result;
	}

	public static int compare(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		int result;

		result = moment1.compareTo(moment2);

		return result;
	}

	public static boolean isBefore(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		final boolean result;

		result = moment1.compareTo(moment2) < 0;

		return result;
	}

	public static boolean isBeforeOrEqual(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		final boolean result;

		result = moment1.compareTo(moment2) <= 0;

		return result;
	}

	public static boolean isEqual(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		final boolean result;

		result = moment1.compareTo(moment2) == 0;

		return result;
	}

	public static boolean isAfterOrEqual(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		final boolean result;

		result = moment1.compareTo(moment2) >= 0;

		return result;
	}

	public static boolean isAfter(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		final boolean result;

		result = moment1.compareTo(moment2) > 0;

		return result;
	}

	// Interval methods -------------------------------------------------------

	public static Duration computeDuration(final Date moment1, final Date moment2) {
		assert moment1 != null;
		assert moment2 != null;

		final Duration result;
		long millis1, millis2, length;

		millis1 = moment1.getTime();
		millis2 = moment2.getTime();
		length = millis2 - millis1;

		result = Duration.ofMillis(length);

		return result;
	}

	public static boolean isLongEnough(final Date moment1, final Date moment2, final long amount, final ChronoUnit unit) {
		assert moment1 != null;
		assert moment2 != null;
		assert unit != null;

		final boolean result;
		long millis1, millis2, length, threshold;

		millis1 = moment1.getTime();
		millis2 = moment2.getTime();
		length = millis2 - millis1;
		threshold = amount * unit.getDuration().toMillis();

		result = Math.abs(length) >= Math.abs(threshold);

		return result;
	}

	// Parsing methods --------------------------------------------------------

	public static Date parse(final String moment, final String format) {
		assert !StringHelper.isBlank(format);
		assert !StringHelper.isBlank(moment);

		Date result;
		SimpleDateFormat formatter;
		ParsePosition position;

		formatter = new SimpleDateFormat(format);
		formatter.setLenient(false);
		position = new ParsePosition(0);
		result = formatter.parse(moment, position);
		assert result != null : String.format("Cannot parse '%s' with format '%s'.", moment, format);

		return result;
	}

	public static String format(final Date moment, final String format) {
		assert !StringHelper.isBlank(format);
		assert moment != null;

		String result;
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat(format);
		formatter.setLenient(false);
		result = formatter.format(moment);

		return result;
	}

	// Real-time methods ------------------------------------------------------

	public static void sleep(final int millis) {
		assert millis >= 0;

		try {
			Thread.sleep(millis);
		} catch (final InterruptedException oops) {
			;
		}
	}

}
