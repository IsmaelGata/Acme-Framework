/*
 * RandomHelper.java
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

import java.util.Random;
import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public abstract class RandomHelper {

	// Internal state ---------------------------------------------------------

	private static Random	backbone;
	private static boolean	testing;
	private static int		index;

	// Constructors -----------------------------------------------------------


	protected RandomHelper() {
	}


	static {
		RandomHelper.initialise();
	}

	// Initialisers -----------------------------------------------------------


	public static void initialise() {
		String profiles;
		long seed;

		profiles = System.getProperty("spring.profiles.active");
		assert profiles != null : "Attempting to use random number generator before a profile is set!";

		RandomHelper.testing = profiles.contains("tester") || profiles.contains("recorder");
		seed = RandomHelper.testing ? 1L : System.currentTimeMillis();

		RandomHelper.backbone = new Random();
		RandomHelper.backbone.setSeed(seed);

		RandomHelper.index = -1;
	}

	public static void reset() {
		RandomHelper.initialise();
	}

	// Business methods -------------------------------------------------------

	public static int getIndex() {
		return RandomHelper.index;
	}

	public static UUID nextUUID() {
		UUID result;
		long nibble1, nibble2, nibble3, nibble4, nibble5;
		String text;

		if (!RandomHelper.testing)
			result = UUID.randomUUID();
		else {
			nibble1 = RandomHelper.nextLong(0x0L, 0xFFFFFFFFL);
			nibble2 = RandomHelper.nextLong(0x0L, 0xFFFFL);
			nibble3 = RandomHelper.nextLong(0x0L, 0xFFFFL);
			nibble4 = RandomHelper.nextLong(0x0L, 0xFFFFL);
			nibble5 = RandomHelper.nextLong(0x0L, 0xFFFFFFFFFFFFL);
			text = String.format("%08x-%04x-%04x-%04x-%012x", nibble1, nibble2, nibble3, nibble4, nibble5);
			result = UUID.fromString(text);
		}

		return result;
	}

	public static void setSeed(final long seed) {
		RandomHelper.backbone.setSeed(seed);
	}

	public static void nextBytes(final byte[] bytes) {
		assert bytes != null && bytes.length >= 1;

		RandomHelper.backbone.nextBytes(bytes);
	}

	public static int nextInt() {
		int result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextInt();

		return result;
	}

	public static int nextInt(final int bound) {
		assert bound >= 1;

		int result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextInt(bound);

		return result;
	}

	public static long nextLong() {
		long result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextLong();

		return result;
	}

	public static boolean nextBoolean() {
		boolean result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextBoolean();

		return result;
	}

	public static float nextFloat() {
		float result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextFloat();

		return result;
	}

	public static float nextFloat(final float bound) {
		assert Float.isFinite(bound);
		assert bound > Float.MIN_VALUE;

		float result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextFloat(bound);

		return result;
	}

	public static float nextFloat(final float origin, final float bound) {
		assert Float.isFinite(origin) && Float.isFinite(bound);
		assert origin < bound;

		float result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextFloat(origin, bound);

		return result;
	}

	public static double nextDouble() {
		double result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextDouble();

		return result;
	}

	public static double nextDouble(final double bound) {
		assert Double.isFinite(bound) && bound > Float.MIN_VALUE;

		double result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextDouble(bound);

		return result;
	}

	public static double nextDouble(final double origin, final double bound) {
		assert Double.isFinite(origin) && Double.isFinite(bound);
		assert origin < bound;

		double result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextDouble(origin, bound);

		return result;
	}

	public static double nextGaussian() {
		double result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextGaussian();

		return result;
	}

	public static int nextInt(final int origin, final int bound) {
		assert origin < bound;

		int result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextInt(origin, bound);

		return result;
	}

	public static long nextLong(final long bound) {
		assert bound >= 1;

		long result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextLong(bound);

		return result;
	}

	public static long nextLong(final long origin, final long bound) {
		assert origin < bound;

		long result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextLong(origin, bound);

		return result;
	}

	public static IntStream ints(final long size) {
		IntStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.ints(size);

		return result;
	}

	public static IntStream ints() {
		IntStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.ints();

		return result;
	}

	public static double nextGaussian(final double mean, final double stddev) {
		assert Double.isFinite(mean) && Double.isFinite(stddev);

		double result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextGaussian(mean, stddev);

		return result;
	}

	public static double nextExponential() {
		double result;

		RandomHelper.index++;
		result = RandomHelper.backbone.nextExponential();

		return result;
	}

	public static IntStream ints(final long size, final int origin, final int bound) {
		assert size >= 1;
		assert origin < bound;

		IntStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.ints(size, origin, bound);

		return result;
	}

	public static IntStream ints(final int origin, final int bound) {
		assert origin < bound;

		IntStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.ints(origin, bound);

		return result;
	}

	public static LongStream longs(final long size) {
		assert size >= 0;

		LongStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.longs(size);

		return result;
	}

	public static LongStream longs() {
		LongStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.longs();

		return result;
	}

	public static LongStream longs(final long size, final long origin, final long bound) {
		assert size >= 1;
		assert origin < bound;

		LongStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.longs(size, origin, bound);

		return result;
	}

	public static LongStream longs(final long origin, final long bound) {
		assert origin < bound;

		LongStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.longs(origin, bound);

		return result;
	}

	public static DoubleStream doubles(final long size) {
		assert size >= 1;

		DoubleStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.doubles(size);

		return result;
	}

	public static DoubleStream doubles() {
		DoubleStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.doubles();

		return result;
	}

	public static DoubleStream doubles(final long size, final double origin, final double bound) {
		assert size >= 1;
		assert Double.isFinite(origin) && Double.isFinite(bound);
		assert origin < bound;

		DoubleStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.doubles(size, origin, bound);

		return result;
	}

	public static DoubleStream doubles(final double origin, final double bound) {
		assert Double.isFinite(origin) && Double.isFinite(bound);
		assert origin < bound;

		DoubleStream result;

		RandomHelper.index++;
		result = RandomHelper.backbone.doubles(origin, bound);

		return result;
	}

}
