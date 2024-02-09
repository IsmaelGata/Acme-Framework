/*
 * ConversionHelper.java
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

import org.springframework.core.convert.ConversionService;

import acme.internals.helpers.FactoryHelper;

public abstract class ConversionHelper {

	// Constructors -----------------------------------------------------------

	protected ConversionHelper() {
	}

	// Business methods -------------------------------------------------------

	public static boolean canConvert(final Object object, final Class<?> clazz) {
		// HINT: object can be null
		assert clazz != null;

		boolean result;
		ConversionService conversionService;

		if (object == null)
			result = true;
		else {
			conversionService = FactoryHelper.getConversionService();
			result = conversionService.canConvert(object.getClass(), clazz);
		}

		return result;
	}

	public static <T> T convert(final Object object, final Class<T> clazz) {
		// HINT: object can be null
		assert clazz != null;
		assert ConversionHelper.canConvert(object, clazz);

		T result;
		ConversionService conversionService;
		Integer intValue;

		if (object == null)
			result = null;
		else {
			conversionService = FactoryHelper.getConversionService();
			try {
				intValue = conversionService.convert(object, Integer.class);
			} catch (Throwable oops) {
				intValue = null;
			}
			if (clazz.isEnum() && intValue != null && intValue.intValue() == 0)
				result = null;
			else
				result = conversionService.convert(object, clazz);
		}

		return result;
	}

	public static String toString(final Object object) {
		// HINT: object can be null
		assert ConversionHelper.canConvert(object, String.class);

		String result;
		ConversionService conversionService;

		conversionService = FactoryHelper.getConversionService();
		result = conversionService.convert(object, String.class);

		return result;
	}

}
