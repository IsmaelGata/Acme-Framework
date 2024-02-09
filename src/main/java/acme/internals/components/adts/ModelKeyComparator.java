/*
 * ModelKeyComparator.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.adts;

import java.util.Comparator;

import acme.client.helpers.StringHelper;

public class ModelKeyComparator implements Comparator<String> {

	// Comparator<String> interface -------------------------------------------

	@Override
	public int compare(final String key1, final String key2) {
		assert !StringHelper.isBlank(key1);
		assert !StringHelper.isBlank(key2);

		int result;
		int i1, j1, i2, j2;
		int index1, index2;

		i1 = key1.indexOf("[");
		j1 = key1.indexOf("]");

		i2 = key2.indexOf("[");
		j2 = key2.indexOf("]");

		result = 0;
		if (i1 != -1 && j1 != -1 && i2 != -1 && j2 != -1) {
			index1 = Integer.parseInt(key1.substring(i1 + 1, j1));
			index2 = Integer.parseInt(key2.substring(i2 + 1, j2));
			assert index1 != -1 && index2 != -1;
			result = index1 - index2;
		}
		if (result == 0)
			result = key1.compareTo(key2);

		return result;
	}

}
