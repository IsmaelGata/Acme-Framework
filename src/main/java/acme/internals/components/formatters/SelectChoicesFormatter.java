/*
 * SelectChoicesFormatter.java
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

import acme.client.views.SelectChoice;
import acme.client.views.SelectChoices;
import acme.internals.helpers.SerialisationHelper;
import acme.internals.helpers.SerialisationHelper.Format;

public class SelectChoicesFormatter implements Formatter<SelectChoices> {

	// Formatter<Object> interface --------------------------------------------

	@Override
	public String print(final SelectChoices object, final Locale locale) {
		assert object != null;
		assert locale != null;

		String result;
		StringBuilder buffer;
		String fragment, comma;

		buffer = new StringBuilder();
		buffer.append("[");
		comma = "";
		for (final SelectChoice choice : object) {
			fragment = SerialisationHelper.write(Format.JSON, choice);
			buffer.append(comma);
			buffer.append(fragment);
			comma = ",";
		}
		buffer.append("]");
		result = buffer.toString();

		return result;
	}

	@Override
	public SelectChoices parse(final String text, final Locale locale) throws ParseException {
		assert text != null;
		assert locale != null;

		final SelectChoices result;
		SelectChoice[] choices;

		choices = SerialisationHelper.read(Format.JSON, text, SelectChoice[].class);
		result = SelectChoices.from(choices);

		return result;
	}

}
