/*
 * DatatypeFormatter.java
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

import java.util.Locale;

import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;

import acme.client.data.AbstractDatatype;
import acme.internals.helpers.SerialisationHelper;
import acme.internals.helpers.SerialisationHelper.Format;

public class DatatypeFormatter implements Formatter<AbstractDatatype> {

	// Formatter<Object> interface --------------------------------------------

	@Override
	public String print(final AbstractDatatype object, final Locale locale) {
		assert object != null;
		assert locale != null;

		String result;

		result = SerialisationHelper.write(Format.JSON, object);

		return result;
	}

	@Override
	public AbstractDatatype parse(final String text, final Locale locale) throws ParseException {
		assert text != null;
		assert locale != null;

		AbstractDatatype result;

		result = SerialisationHelper.read(Format.JSON, text, AbstractDatatype.class);

		return result;
	}

}
