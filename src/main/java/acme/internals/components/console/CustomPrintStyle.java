/*
 * CustomPrintStyle.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.console;

import org.apache.commons.lang3.builder.ToStringStyle;

import acme.client.data.AbstractEntity;
import acme.client.helpers.StringHelper;

public class CustomPrintStyle extends ToStringStyle {

	// Serialisation identifier -----------------------------------------------

	private static final long serialVersionUID = 1L;

	// Constructors -----------------------------------------------------------


	public CustomPrintStyle() {
		super();

		this.setUseShortClassName(false);
		this.setUseIdentityHashCode(false);
		this.setArraySeparator(", ");
		this.setContentStart("{");
		this.setFieldSeparator(System.lineSeparator() + "\t");
		this.setFieldSeparatorAtStart(true);
		this.setContentEnd(System.lineSeparator() + "}");
		this.setArrayContentDetail(true);
		this.setDefaultFullDetail(true);
		this.setNullText("null");
	}

	// ToStringStyle interface ------------------------------------------------

	@Override
	protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object object) {
		assert buffer != null;
		assert !StringHelper.isBlank(fieldName);
		// HINT: object can be null

		String left, right;

		if (object == null)
			left = right = "";
		else if (object instanceof String)
			left = right = "\"";
		else if (object instanceof Character)
			left = right = "\'";
		else if (!(object instanceof AbstractEntity) && !(object instanceof Number)) {
			left = "<<";
			right = ">>";
		} else
			left = right = "";

		buffer.append(left);
		buffer.append(object);
		buffer.append(right);
	}

	// Business methods -------------------------------------------------------

	public void appendObject(final StringBuffer buffer, final Object object) {
		assert buffer != null;
		// HINT: value can be null

		this.appendDetail(buffer, null, object);
	}

}
