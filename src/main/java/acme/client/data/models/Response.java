/*
 * Response.java
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

import javax.servlet.http.HttpServletResponse;

import acme.client.helpers.StringHelper;

public class Response extends WorkArea {

	// Constructors -----------------------------------------------------------

	public Response() {
		super.addGlobal("$authorised", false);
		super.addGlobal("$checked", false);
		super.addGlobal("$oops", null);
		super.addGlobal("$view", "master/panic");
		super.addGlobal("$errors", new Errors());
	}

	public static Response from(final HttpServletResponse httpServletResponse) {
		assert httpServletResponse != null;

		Response result;

		result = new Response();

		return result;
	}

	// Properties -------------------------------------------------------------

	public boolean isAuthorised() {
		boolean result;

		result = super.getGlobal("$authorised", boolean.class);

		return result;
	}

	public void setAuthorised(final boolean status) {
		super.addGlobal("$authorised", status);
	}

	public boolean isChecked() {
		boolean result;

		result = super.getGlobal("$checked", boolean.class);

		return result;
	}

	public void setChecked(final boolean status) {
		super.addGlobal("$checked", status);
	}

	public Throwable getOops() {
		Throwable result;

		result = super.getGlobal("$oops", Throwable.class);

		return result;
	}

	public void setOops(final Throwable oops) {
		assert oops != null;

		super.addGlobal("$oops", oops);
	}

	public String getView() {
		String result;

		result = super.getGlobal("$view", String.class);

		return result;
	}

	public void setView(final String view) {
		assert !StringHelper.isBlank(view);

		super.addGlobal("$view", view);
	}

	public Errors getErrors() {
		Errors errors;

		errors = super.getGlobal("$errors", Errors.class);

		return errors;
	}

}
