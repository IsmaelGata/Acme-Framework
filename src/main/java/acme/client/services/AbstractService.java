/*
 * AbstractService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.services;

import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import acme.client.data.AbstractObject;
import acme.client.data.AbstractRole;
import acme.client.data.models.Buffer;
import acme.client.data.models.Dataset;
import acme.client.data.models.Errors;
import acme.client.data.models.Request;
import acme.client.data.models.Response;
import acme.client.helpers.StringHelper;
import acme.internals.helpers.BinderHelper;

@Service
public abstract class AbstractService<R extends AbstractRole, O extends AbstractObject> {

	// Internal state ---------------------------------------------------------

	private ThreadLocal<Request>	requests;
	private ThreadLocal<Buffer>		buffers;
	private ThreadLocal<Response>	responses;

	// Constructors -----------------------------------------------------------


	protected AbstractService() {
		this.requests = new ThreadLocal<Request>();
		this.buffers = new ThreadLocal<Buffer>();
		this.responses = new ThreadLocal<Response>();
	}

	public void initialise(final Request request, final Buffer buffer, final Response response) {
		assert request != null;
		assert buffer != null;
		assert response != null;

		this.requests.set(request);
		this.buffers.set(buffer);
		this.responses.set(response);
	}

	public void finalise() {
		this.requests.remove();
		this.buffers.remove();
		this.responses.remove();
	}

	// Properties -------------------------------------------------------------

	public Request getRequest() {
		Request result;

		result = this.requests.get();
		assert result != null;

		return result;
	}

	public Buffer getBuffer() {
		Buffer result;

		result = this.buffers.get();
		assert result != null;

		return result;
	}

	public Response getResponse() {
		Response result;

		result = this.responses.get();
		assert result != null;

		return result;
	}

	// Hook methods -----------------------------------------------------------

	public void authorise() {
		throw new NotImplementedException();
	}

	public void load() {
		throw new NotImplementedException();
	}

	public void bind(final O object) {
		assert object != null;

		throw new NotImplementedException();
	}

	public void bind(final Collection<O> objects) {
		assert objects != null;
	}

	public void validate(final O object) {
		assert object != null;

		throw new NotImplementedException();
	}

	public void validate(final Collection<O> objects) {
		assert objects != null;
	}

	public void perform(final O object) {
		assert object != null;

		throw new NotImplementedException();
	}

	public void perform(final Collection<O> objects) {
		assert objects != null;
	}

	public void unbind(final O object) {
		assert object != null;

		throw new NotImplementedException();
	}

	public void unbind(final Collection<O> objects) {
		assert objects != null;
	}

	public void onSuccess() {
	}

	public void onFailure() {
	}

	// Utility methods --------------------------------------------------------

	protected void state(final boolean condition, final String property, final String code, final Object... arguments) {
		assert !StringHelper.isBlank(property);
		assert !StringHelper.isBlank(code);
		assert arguments != null;

		Errors errors;

		errors = this.getBuffer().getGlobal("$errors", Errors.class);
		errors.state(this.getRequest(), condition, property, code, arguments);
	}

	protected void bind(final O object, final String... properties) {
		assert object != null;
		assert !StringHelper.someBlank(properties);

		BinderHelper.bind(object, this.getRequest(), this.getBuffer(), properties);
	}

	protected Dataset unbind(final O object, final String... properties) {
		assert object != null;
		assert !StringHelper.someBlank(properties);

		Dataset result;

		result = BinderHelper.unbind(object, properties);

		return result;
	}

}
