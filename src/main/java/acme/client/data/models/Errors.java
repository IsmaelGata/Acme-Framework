/*
 * Errors.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.data.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import acme.client.helpers.MessageHelper;
import acme.client.helpers.StringHelper;
import lombok.ToString;

@ToString(of = {
	"map"
})
public class Errors implements Iterable<Entry<String, List<String>>> {

	// Internal state ---------------------------------------------------------

	private Map<String, List<String>> map;

	// Constructors -----------------------------------------------------------


	public Errors() {
		this.map = new LinkedHashMap<String, List<String>>();
	}

	// Iterable interface -----------------------------------------------------

	@Override
	public Iterator<Entry<String, List<String>>> iterator() {
		Iterator<Entry<String, List<String>>> result;

		result = this.map.entrySet().iterator();

		return result;
	}

	// Business methods -------------------------------------------------------

	public void state(final Request request, final boolean condition, final String attributeName, final String code, final Object... arguments) {
		assert request != null;
		assert !StringHelper.isBlank(attributeName);
		assert !StringHelper.isBlank(code);
		assert arguments != null;  // HINT: there can be null arguments

		String message;

		if (!condition) {
			message = MessageHelper.getMessage(code, arguments, code, request.getLocale());
			this.add(attributeName, message);
		}
	}

	public void add(final String attributeName, final String message) {
		assert !StringHelper.isBlank(attributeName);
		assert !StringHelper.isBlank(message);

		List<String> currentErrors;

		if (this.map.containsKey(attributeName))
			currentErrors = this.map.get(attributeName);
		else {
			currentErrors = new ArrayList<String>();
			this.map.put(attributeName, currentErrors);
		}

		currentErrors.add(message);
		currentErrors.sort(String::compareToIgnoreCase);
	}

	public boolean hasErrors() {
		boolean result;

		result = !this.map.isEmpty();

		return result;
	}

	public Collection<String> getErroneousAttributes() {
		Collection<String> result;

		result = this.map.keySet();

		return result;
	}

	public boolean hasErrors(final String attributeName) {
		assert !StringHelper.isBlank(attributeName);

		boolean result;

		result = this.map.containsKey(attributeName);

		return result;
	}

	public String getFirstError(final String attributeName) {
		assert !StringHelper.isBlank(attributeName);
		assert this.hasErrors(attributeName);

		String result;

		result = this.map.get(attributeName).get(0);

		return result;
	}

	public List<String> getAllErrors(final String attributeName) {
		assert !StringHelper.isBlank(attributeName);
		assert this.hasErrors(attributeName);

		List<String> result;

		result = this.map.get(attributeName);

		return result;
	}

}
