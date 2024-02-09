/*
 * Dataset.java
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

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import acme.client.helpers.PrinterHelper;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class Dataset extends LinkedHashMap<String, Object> {

	// Serialisation interface ------------------------------------------------

	private static final long serialVersionUID = 1L;

	// Object interface -------------------------------------------------------


	@Override
	public String toString() {
		StringBuilder result;

		result = new StringBuilder();
		for (final Entry<String, Object> entry : this.entrySet()) {
			String key;
			Object object;

			key = entry.getKey();
			object = entry.getValue();

			result.append(key);
			result.append(" = ");
			PrinterHelper.printObject(result, object, true);
			result.append(";");
		}

		return result.toString();
	}

}
