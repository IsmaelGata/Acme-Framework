/*
 * ThrowableHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.helpers;

import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;

import acme.client.data.AbstractEntity;
import acme.client.data.models.Errors;
import acme.client.helpers.CollectionHelper;
import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.PassThroughException;

public abstract class ThrowableHelper {

	// Constructors -----------------------------------------------------------

	protected ThrowableHelper() {
	}

	// Business methods -------------------------------------------------------

	public static void print(final PrintStream writer, final Throwable oops) {
		assert writer != null;
		assert oops != null;

		String text;

		text = ThrowableHelper.toString(oops);
		writer.append(text);
		writer.append(System.lineSeparator());
	}

	public static String toString(final Throwable oops) {
		assert oops != null;

		StringBuilder result;
		String title, description, text, separator;
		Throwable iterator;

		result = new StringBuilder();
		iterator = oops;
		separator = "";
		while (iterator != null) {
			while (iterator instanceof PassThroughException)
				iterator = iterator.getCause();
			if (iterator != null) {
				title = iterator.getStackTrace()[0].toString();
				description = iterator.getMessage();
				description = !StringHelper.isBlank(description) ? description : iterator.getClass().getName();
				text = ThrowableHelper.formatSection(title, description);
				result.append(separator);
				separator = System.lineSeparator();
				result.append(text);
				iterator = iterator.getCause();
			}
		}

		return result.toString();
	}

	public static String toString(final String entityName, final Set<ConstraintViolation<AbstractEntity>> violations) {
		assert !StringHelper.isBlank(entityName);
		assert !CollectionHelper.someNull(violations);

		StringBuilder result;
		String comma, property, message;

		result = new StringBuilder();
		result.append(String.format("Violated constraints on entity %s: ", entityName));
		comma = "";
		for (final ConstraintViolation<AbstractEntity> violation : violations) {
			property = violation.getPropertyPath().toString();
			message = violation.getMessage();
			result.append(String.format("%s'%s' %s", comma, property, message));
			comma = ", ";
		}

		return result.toString();
	}

	public static String toString(final String entityName, final Errors errors) {
		assert !StringHelper.isBlank(entityName);
		assert errors != null;

		StringBuilder result;
		String comma, property, message;

		result = new StringBuilder();
		result.append(String.format("Errors on entity %s: ", entityName));
		comma = "";
		for (final Entry<String, List<String>> entry : errors) {
			property = entry.getKey();
			message = String.join(", ", entry.getValue().toArray(new String[0]));
			result.append(String.format("%s'%s' - %s", comma, property, message));
			comma = "; ";
		}

		return result.toString();
	}

	public static String formatText(final String text) {
		assert text != null;

		String result;

		result = ThrowableHelper.formatText(text, System.lineSeparator());

		return result;
	}

	public static String formatText(final String text, final String lineSeparator) {
		assert text != null;
		assert lineSeparator != null;

		String result;

		result = text.replace("Stacktrace:", "");
		result = result.replace("&#47;", "/");
		result = result.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&");
		result = result.replaceAll("[ \\t\\v]+$", "");
		result = result.replaceAll("^[ \\t\\v]+", "");
		result = result.replaceAll("[\\n\\r\\f]+", lineSeparator);

		return result;
	}

	public static String formatSection(final String title, final String text) {
		assert !StringHelper.isBlank(title);
		assert text != null;

		String result;

		result = ThrowableHelper.formatSection(title, text, System.lineSeparator());

		return result;

	}

	public static String formatSection(final String title, final String text, final String lineSeparator) {
		assert !StringHelper.isBlank(title);
		assert !StringHelper.isBlank(text);
		assert lineSeparator != null;

		String result;
		String formattedTitle, formattedText;

		formattedTitle = ThrowableHelper.formatText(title, lineSeparator);
		formattedText = ThrowableHelper.formatText(text, lineSeparator);

		result = String.format("%s: %s", formattedTitle, formattedText);

		return result;
	}

}
