/*
 * FileRecord.java
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

import acme.client.helpers.StringHelper;

public class FileRecord<T> {

	// Internal state ---------------------------------------------------------

	private T		object;
	private String	comment;

	// Properties -------------------------------------------------------------


	public T getObject() {
		return this.object;
	}

	public boolean hasObject() {
		boolean result;

		result = this.object != null;

		return result;
	}

	public void setObject(final T object) {
		// HINT: object can be null

		this.object = object;
	}

	public String getComment() {
		return this.comment;
	}

	public boolean hasComment() {
		boolean result;

		result = !StringHelper.isBlank(this.comment);

		return result;
	}

	public void setComment(final String comment) {
		// HINT: comment can be blank

		this.comment = comment;
	}

}
