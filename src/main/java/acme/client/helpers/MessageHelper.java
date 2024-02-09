/*
 * MessageHelper.java
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

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import acme.internals.helpers.FactoryHelper;

public abstract class MessageHelper {

	// Constructors -----------------------------------------------------------

	protected MessageHelper() {
	}

	// Business methods -------------------------------------------------------

	public static String getMessage(final String code, final Object[] arguments, final String defaultMessage, final Locale locale) {
		assert !StringHelper.isBlank(code);
		// HINT: arguments can be null
		assert !StringHelper.isBlank(defaultMessage);
		assert locale != null;

		String result;
		MessageSource messageSource;

		messageSource = FactoryHelper.getBean(MessageSource.class);
		result = messageSource.getMessage(code, arguments, defaultMessage, locale);
		if (result != null)
			result = result.trim();

		return result;
	}

	public static String getMessage(final String code, final Object[] arguments) {
		assert !StringHelper.isBlank(code);
		// HINT: arguments can be null

		String result;
		MessageSource messageSource;
		Locale locale;

		messageSource = FactoryHelper.getBean(MessageSource.class);
		locale = LocaleContextHolder.getLocale();
		result = messageSource.getMessage(code, arguments, code, locale);
		if (result != null)
			result = result.trim();

		return result;
	}

	public static String getMessage(final String code) {
		assert !StringHelper.isBlank(code);

		String result;
		MessageSource messageSource;
		Locale locale;

		messageSource = FactoryHelper.getBean(MessageSource.class);
		locale = LocaleContextHolder.getLocale();
		result = messageSource.getMessage(code, null, code, locale);
		if (result != null)
			result = result.trim();

		return result;
	}

}
