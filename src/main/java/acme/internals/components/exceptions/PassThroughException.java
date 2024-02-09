/*
 * PassThroughException.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.exceptions;

import acme.client.helpers.StringHelper;

public class PassThroughException extends RuntimeException {

	// Serialisation identifier -----------------------------------------------

	private static final long serialVersionUID = 1L;

	// Constructors -----------------------------------------------------------


	public PassThroughException(final String message, final Throwable oops) {
		super(message, oops);

		assert !StringHelper.isBlank(message);
		assert oops != null;
	}

	public PassThroughException(final Throwable oops) {
		super(oops);

		assert oops != null;
	}

}
