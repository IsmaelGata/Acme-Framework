/*
 * EnumerationIterator.java
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
import java.util.NoSuchElementException;

import org.apache.commons.lang3.NotImplementedException;

public class EnumerationIterator<E> implements Iterator<E> {

	// Internal state ---------------------------------------------------------

	private Enumeration<E> target;

	// Constructors -----------------------------------------------------------


	public EnumerationIterator(final Enumeration<E> target) {
		assert target != null;

		this.target = target;
	}

	// Iterator interface -----------------------------------------------------

	@Override
	public E next() {
		E result;

		if (!this.target.hasMoreElements())
			throw new NoSuchElementException();
		result = this.target.nextElement();

		return result;
	}

	@Override
	public boolean hasNext() {
		boolean result;

		result = this.target.hasMoreElements();

		return result;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

}
