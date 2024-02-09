/*
 * CollectionHelper.java
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import acme.internals.components.adts.EnumerationIterable;

public abstract class CollectionHelper {

	// Constructors -----------------------------------------------------------

	protected CollectionHelper() {
	}

	// Business methods -------------------------------------------------------

	public static boolean someNull(final Object[] array) {
		// HINT: array can be null and can contain nulls

		boolean result;

		result = array == null;
		for (int index = 0; !result && index < array.length; index++)
			result = array[index] == null;

		return result;
	}

	public static boolean someNull(final Iterable<?> collection) {
		// HINT: collection can be null and can contain nulls

		boolean result;
		Iterator<?> iterator;

		result = collection == null;
		iterator = collection.iterator();
		while (!result && iterator.hasNext()) {
			Object object;
			object = iterator.next();
			result = object == null;
		}

		return result;
	}

	public static <E> Iterable<E> toIterable(final Enumeration<E> target) {
		assert target != null;

		Iterable<E> result;

		result = new EnumerationIterable<E>(target);

		return result;
	}

	public static <E extends Enum<?>> Collection<E> toCollection(final Class<E> target) {
		assert target != null;

		Collection<E> result;
		E[] values;

		values = target.getEnumConstants();
		result = Arrays.asList(values);

		return result;
	}

	public static <T> double computeSimpsonIndex(final Collection<T> data) {
		assert data != null;

		double result;
		Bag<T> bag;
		int n, ni, d;

		n = data.size();
		if (n < 2)
			result = 0;
		else {
			bag = new HashBag<T>();
			bag.addAll(data);
			d = 0;
			for (T s : bag.uniqueSet()) {
				ni = bag.getCount(s);
				d += ni * (ni - 1);
			}
			result = 1.00 - d / (double) (n * (n - 1));
		}

		return result;
	}

}
