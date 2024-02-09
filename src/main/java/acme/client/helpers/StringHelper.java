/*
 * StringHelper.java
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

import java.util.Collection;
import java.util.Iterator;

import org.springframework.web.util.HtmlUtils;

import acme.client.data.AbstractEntity;

public abstract class StringHelper {

	// Constructors -----------------------------------------------------------

	protected StringHelper() {
	}

	// Business methods -------------------------------------------------------

	public static boolean isBlank(final CharSequence text) {
		// HINT: text can be null

		boolean result;
		char c;

		if (text == null)
			result = true;
		else {
			result = true;
			for (int i = 0; result && i < text.length(); i++) {
				c = text.charAt(i);
				result = Character.isWhitespace(c);
			}
		}

		return result;
	}

	public static boolean someBlank(final String[] array) {
		// HINT: array can be null and can contain nulls

		boolean result;

		result = array == null;
		for (int index = 0; !result && index < array.length; index++)
			result = StringHelper.isBlank(array[index]);

		return result;
	}

	public static boolean someBlank(final Iterable<String> collection) {
		// HINT: collection can be null

		boolean result;
		Iterator<String> iterator;
		String text;

		result = collection == null;
		iterator = collection.iterator();
		while (!result && iterator.hasNext()) {
			text = iterator.next();
			result = StringHelper.isBlank(text);
		}

		return result;
	}

	public static boolean matches(final String value, final String regex) {
		assert value != null;
		assert !StringHelper.isBlank(regex);

		boolean result;

		result = value.matches(regex);

		return result;
	}

	public static boolean anyOf(final String value, final String choices) {
		assert value != null;
		assert !StringHelper.isBlank(choices);

		boolean result;
		int index;
		String[] values;

		result = false;
		index = 0;
		values = StringHelper.splitChoices(choices);
		while (!result && index < values.length) {
			result = value.equals(values[index].trim());
			index++;
		}

		return result;
	}

	public static boolean isEqual(final String text1, final String text2, final boolean ignoreCase) {
		// HINT: text1 can be null
		// HINT: text2 can be null

		boolean result;

		if (text1 == null && text2 == null)
			result = true;
		else if (text1 == null && text2 != null || text1 != null && text2 == null)
			result = false;
		else
			result = ignoreCase ? text1.compareToIgnoreCase(text2) == 0 : text1.compareTo(text2) == 0;

		return result;
	}

	public static boolean contains(final String text, final String infix, final boolean ignoreCase) {
		// HINT: text can be null
		assert !StringHelper.isBlank(infix);

		boolean result;
		int len, max;

		if (text == null)
			result = false;
		else {
			len = infix.length();
			max = text.length() - len;
			result = false;
			for (int i = 0; !result && i <= max; i++)
				result = text.regionMatches(ignoreCase, i, infix, 0, len);
		}

		return result;
	}

	public static boolean startsWith(final String text, final String prefix, final boolean ignoreCase) {
		// HINT: text can be null
		assert !StringHelper.isBlank(prefix);

		final boolean result;

		result = text != null && text.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());

		return result;
	}

	public static boolean endsWith(final String text, final String suffix, final boolean ignoreCase) {
		// HINT: text can be null
		assert !StringHelper.isBlank(suffix);

		final boolean result;
		int suffixLength;

		suffixLength = suffix.length();
		result = text != null && text.regionMatches(ignoreCase, text.length() - suffixLength, suffix, 0, suffixLength);

		return result;
	}

	public static String[] splitChoices(final String choices) {
		assert !StringHelper.isBlank(choices);

		String[] result;

		result = choices.split("\\ *[,;\\|]\\ *");

		return result;
	}

	public static String makeIndentation(final int size) {
		assert size >= 0;

		String result;

		result = StringHelper.makeString("    ", size);

		return result;
	}

	public static String makeString(final String text, final int replica) {
		assert text != null;
		assert replica >= 0;

		StringBuilder result;

		result = new StringBuilder();
		for (int i = 0; i < replica; i++)
			result.append(text);

		return result.toString();
	}

	public static String capitaliseInitial(final String text) {
		assert !StringHelper.isBlank(text);

		String result;

		if (text.length() >= 1 && Character.isUpperCase(text.charAt(0)))
			result = text;
		else
			result = String.format("%c%s", Character.toUpperCase(text.charAt(0)), text.substring(1));

		return result;
	}

	public static String smallInitial(final String text) {
		assert !StringHelper.isBlank(text);

		String result;

		if (text.length() >= 1 && Character.isLowerCase(text.charAt(0)))
			result = text;
		else
			result = String.format("%c%s", Character.toLowerCase(text.charAt(0)), text.substring(1));

		return result;
	}

	public static String toPrintable(final char character) {
		String result;

		result = String.valueOf(character);
		result = StringHelper.toPrintable(result);

		return result;
	}

	public static String toPrintable(final String text) {
		assert text != null;

		StringBuilder result;
		char character;
		Character.UnicodeBlock block;
		boolean printable;
		String hexadecimal;

		result = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			character = text.charAt(i);
			block = Character.UnicodeBlock.of(character);
			// HINT: naive: printable = ' ' <= character && character <= '~' || '\u00a1' <= character && character <= '\u024f';
			printable = !Character.isISOControl(character) && block != null && block != Character.UnicodeBlock.SPECIALS;
			if (printable)
				result.append(character);
			else {
				hexadecimal = String.format("\\0x%04x", (int) character);
				result.append(hexadecimal);
			}
		}

		return result.toString();
	}

	public static String toString(final Object[] objects, final String separator, final String finaliser) {
		assert objects != null;  // HINT: some objects can be null
		assert separator != null;
		assert finaliser != null;

		StringBuilder result;
		String space;

		result = new StringBuilder();
		space = "";
		for (final Object object : objects) {
			result.append(space);
			result.append(object == null ? "" : object.toString());
			space = separator;
		}
		result.append(finaliser);

		return result.toString();
	}

	public static String toString(final Collection<?> objects, final String separator, final String finaliser) {
		assert objects != null;  // HINT: some objects can be null
		assert separator != null;
		assert finaliser != null;

		StringBuilder result;
		String space;

		result = new StringBuilder();
		space = "";
		for (final Object object : objects) {
			result.append(space);
			result.append(object == null ? "" : object.toString());
			space = separator;
		}
		result.append(finaliser);

		return result.toString();
	}

	public static String toHtml(final String text) {
		assert text != null;

		String result;

		result = HtmlUtils.htmlEscape(text);

		return result;
	}

	public static String toIdentity(final Object object) {
		// HINT: object can be null

		String result;
		String clazzName;
		int hash;

		if (object == null)
			result = "null";
		else if (!(object instanceof AbstractEntity) || !((AbstractEntity) object).isTransient()) {
			clazzName = object.getClass().getName();
			if (object instanceof AbstractEntity)
				hash = ((AbstractEntity) object).getId();
			else
				hash = System.identityHashCode(object);
			result = String.format("%s@%d", clazzName, hash);
		} else {
			assert object instanceof AbstractEntity && ((AbstractEntity) object).isTransient();
			result = PrinterHelper.printObject(object, true);
		}

		return result;
	}

}
