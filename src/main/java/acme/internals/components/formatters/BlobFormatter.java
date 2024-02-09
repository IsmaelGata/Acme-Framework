/*
 * BlobFormatter.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.formatters;

import java.sql.Clob;
import java.util.Locale;

import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;

import acme.internals.helpers.SerialisationHelper;
import acme.internals.helpers.SerialisationHelper.Format;

public class BlobFormatter implements Formatter<Clob> {

	// Formatter<Clob> interface --------------------------------------------

	@Override
	public String print(final Clob object, final Locale locale) {
		assert object != null;
		assert locale != null;

		String result;

		result = SerialisationHelper.write(Format.BASE64, object);

		return result;
	}

	@Override
	public Clob parse(final String text, final Locale locale) throws ParseException {
		assert text != null;
		assert locale != null;

		Clob result;

		result = SerialisationHelper.read(Format.BASE64, text, Clob.class);

		return result;
	}

}
