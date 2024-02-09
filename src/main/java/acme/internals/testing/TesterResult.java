/*
 * TesterResult.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.testing;

import acme.client.helpers.StringHelper;

public class TesterResult {

	// Constructors -----------------------------------------------------------

	public TesterResult() {

	}

	// Internal state ---------------------------------------------------------


	private String	requestId;

	private String	method;

	private String	path;

	private long	elapsedTime;

	private String	input;

	private String	output;

	private String	oops;

	// Properties -------------------------------------------------------------


	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(final String requestId) {
		assert !StringHelper.isBlank(requestId);

		this.requestId = requestId;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(final String method) {
		assert method != null && StringHelper.anyOf(method, "GET|POST");

		this.method = method;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		assert !StringHelper.isBlank(path);

		this.path = path;
	}

	public long getElapsedTime() {
		return this.elapsedTime;
	}

	public void setElapsedTime(final long elapsedTime) {
		assert elapsedTime >= 0;

		this.elapsedTime = elapsedTime;
	}

	public String getInput() {
		return this.input;
	}

	public void setInput(final String input) {
		assert input != null;

		this.input = input;
	}

	public String getOutput() {
		return this.output;
	}

	public void setOutput(final String output) {
		assert output != null;

		this.output = output;
	}

	public String getOops() {
		return this.oops;
	}

	public void setOops(final String oops) {
		assert oops != null;

		this.oops = oops;
	}

	public void accumulateOops(final String oops) {
		assert !StringHelper.isBlank(oops);

		String separator;

		if (this.oops == null)
			this.oops = oops;
		else {
			separator = this.oops.endsWith(".") ? " " : ". ";
			this.oops = String.format("%s%s%s", this.oops, separator, oops);
		}

	}

}
