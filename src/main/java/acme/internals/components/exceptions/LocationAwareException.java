/*
 * LocationAwareException.java
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

import java.io.File;

public class LocationAwareException extends RuntimeException {

	// Serialisation identifier -----------------------------------------------

	private static final long serialVersionUID = 1L;

	// Constructors -----------------------------------------------------------


	public LocationAwareException(final File source, final Throwable oops) {
		super(String.format("%s: %s", source.getPath(), oops.getLocalizedMessage()), oops.getCause());

		assert source != null;
		assert oops != null;
	}

}
