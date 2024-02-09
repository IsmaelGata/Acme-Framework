/*
 * EnumerationIterable.java
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

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterable<E> implements Iterable<E> {

	// Internal state ---------------------------------------------------------

	private Enumeration<E> target;

	// Constructors -----------------------------------------------------------


	public EnumerationIterable(final Enumeration<E> target) {
		assert target != null;

		this.target = target;
	}

	// Iterable interface -----------------------------------------------------

	@Override
	public Iterator<E> iterator() {
		Iterator<E> result;

		result = new EnumerationIterator<E>(this.target);

		return result;
	}

}
