/*
 * AbstractController.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.controllers;

import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import acme.client.data.AbstractObject;
import acme.client.data.AbstractRole;
import acme.client.data.accounts.Any;
import acme.client.data.accounts.Authenticated;
import acme.client.data.models.Buffer;
import acme.client.data.models.Dataset;
import acme.client.data.models.Errors;
import acme.client.data.models.Request;
import acme.client.data.models.Response;
import acme.client.helpers.Assert;
import acme.client.helpers.MomentHelper;
import acme.client.helpers.StringHelper;
import acme.client.services.AbstractService;
import acme.internals.components.database.DatabaseManager;
import acme.internals.components.exceptions.PassThroughException;
import acme.internals.controllers.CommandManager;
import acme.internals.controllers.ControllerMetadata;
import acme.internals.helpers.EnvironmentHelper;
import acme.internals.helpers.ErrorsHelper;
import acme.internals.helpers.FactoryHelper;
import acme.internals.helpers.ReflectionHelper;
import acme.internals.helpers.ServletHelper;
import acme.internals.services.PanicService;

@Controller
public abstract class AbstractController<R extends AbstractRole, O extends AbstractObject> {

	// Internal state ---------------------------------------------------------

	private ControllerMetadata<R, O>		metadata;

	private CommandManager<R, O>			commandManager;

	@Autowired
	private PanicService<R, O>				panicService;

	@Autowired
	private DatabaseManager					databaseManager;

	@Autowired
	private RequestMappingHandlerMapping	handlerMapper;

	// Constructors -----------------------------------------------------------


	protected AbstractController() {
		Class<?>[] types;

		// HINT: unfortunately, class ControllerMetadata cannot have access to the classes to 
		// HINT+ which its parameters are bound.  Thus, we have to find them here and pass them
		// HINT+ to its constructor.

		types = GenericTypeResolver.resolveTypeArguments(this.getClass(), AbstractController.class);
		this.metadata = new ControllerMetadata<R, O>(types);

		this.commandManager = new CommandManager<R, O>();
	}

	// Command Management -----------------------------------------------------

	protected void addBasicCommand(final String command, final AbstractService<R, O> service) {
		assert !StringHelper.isBlank(command) && !this.commandManager.isRegistered(command);
		assert service != null;

		this.commandManager.addBasicCommand(command, service);
		this.addHandler(command);
	}

	protected void addCustomCommand(final String command, final String superCommand, final AbstractService<R, O> service) {
		assert !StringHelper.isBlank(command) && !this.commandManager.isRegistered(command);
		assert !StringHelper.isBlank(superCommand) && this.commandManager.isBasic(superCommand);
		assert service != null;

		this.commandManager.addCustomCommand(command, superCommand, service);
		this.addHandler(command);
	}

	// Handler ----------------------------------------------------------------

	public ModelAndView handler(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {
		assert httpServletRequest != null;
		assert httpServletResponse != null;

		ModelAndView result;
		String method, command;
		Locale locale;
		AbstractService<R, O> service;

		// HINT: let us initialise some variables.

		result = null;
		method = null;
		service = null;

		try {
			// HINT: get some stuff from the servlet.

			method = ServletHelper.getRequestMethod(httpServletRequest);
			command = ServletHelper.getFeatureCommand(httpServletRequest);

			// HINT: let us initialise the service.

			service = this.prepareService(httpServletRequest, httpServletResponse);
			locale = service.getRequest().getLocale();
			Assert.state(StringHelper.anyOf(method, "GET|POST"), locale, "default.error.endpoint-unavailable");
			Assert.state(!(service instanceof PanicService), locale, "default.error.endpoint-unavailable");

			// HINT: let us make sure that the principal has the appropriate role.

			if (this.metadata.getRoleClazz().equals(Any.class))
				;
			else if (this.metadata.getRoleClazz().equals(Authenticated.class))
				Assert.state(service.getRequest().getPrincipal().isAuthenticated(), locale, "default.error.not-authorised");
			else
				Assert.state(service.getRequest().getPrincipal().hasRole(this.metadata.getRoleClazz()), locale, "default.error.not-authorised");
			service.getRequest().getPrincipal().setActiveRole(this.metadata.getRoleClazz());

			// HINT: let us start a new transaction.

			this.databaseManager.startTransaction();

			// HINT: let us request authorisation from the service.

			service.authorise();
			Assert.state(service.getResponse().isAuthorised(), locale, "default.error.not-authorised");

			// HINT: let us dispatch the request building on the HTTP method used.

			if (service.getRequest().getMethod().equals("GET")) {
				service.load();
				this.apply(service, service::unbind, service::unbind);
				this.redirect(service);
			} else {
				assert service.getRequest().getMethod().equals("POST");
				service.load();
				this.detachPersistenceContext(); // HINT: prevent J2EE from trying to flush dirty objects when not appropriate!
				this.apply(service, service::bind, service::bind);
				if (!this.commandManager.getSuperCommand(service.getRequest().getCommand()).equals("delete"))
					this.validate(service);
				this.apply(service, service::validate, service::validate);
				if (!service.getBuffer().getErrors().hasErrors())
					this.apply(service, service::perform, service::perform);
				if (this.commandManager.getSuperCommand(command).equals("perform") || service.getBuffer().getErrors().hasErrors())
					this.apply(service, service::unbind, service::unbind);
				this.redirect(service);
			}

			// HINT: let us commit or roll the transaction back depending on whether there are errors or not in the response.

			if (!service.getBuffer().getErrors().hasErrors())
				this.databaseManager.commitTransaction();
			else
				this.databaseManager.rollbackTransaction();

			// HINT: let us finalise the workflow depending on whether there are errors or not in the response.

			this.databaseManager.startTransaction();
			if (!service.getBuffer().getErrors().hasErrors())
				service.onSuccess();
			else
				service.onFailure();
			this.databaseManager.commitTransaction();
		} catch (final Throwable oops) {
			// HINT: let us try to roll the active transaction back, if any.

			try {
				if (this.databaseManager.isTransactionActive())
					this.databaseManager.rollbackTransaction();
			} catch (final Throwable ouch) {
				;
			}

			// HINT: let us try to notify the service of the failure.

			try {
				assert service != null;
				this.databaseManager.startTransaction();
				service.getResponse().setOops(oops);
				service.onFailure();
				this.databaseManager.commitTransaction();
			} catch (final Throwable ouch) {
				;
			}
		} finally {
			// HINT: let us build the resulting model-and-view object, if possible.

			assert service != null;
			result = this.buildResult(service);

			// HINT: and let us finalise the service.

			service.finalise();
		}

		// HINT: let us update the clock, if necessary.

		if (MomentHelper.isSimulatedClock()) {
			String value;
			long time;

			value = EnvironmentHelper.getRequiredProperty("acme.runtime.servicing-time", String.class);
			assert value.matches("\\d+") : "acme.runtime.servicing-time must be a natural number!";
			time = Integer.valueOf(value);
			assert time >= 0 : "acme.runtime.servicing-time must be a natural number!";
			MomentHelper.tick(time, ChronoUnit.SECONDS);
		}

		// HINT: check that there is a result.

		assert result != null;

		// HINT: finally, let us return a result to the servlet.

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	protected void addHandler(final String command) {
		assert !StringHelper.isBlank(command);

		Method handler;
		String path;

		try {
			handler = AbstractController.class.getMethod("handler", HttpServletRequest.class, HttpServletResponse.class);
			path = String.format("%s%s", this.metadata.getRequestPath(), command);
			this.handlerMapper.registerMapping( //
				RequestMappingInfo. //
					paths(path). //
					methods(RequestMethod.GET, RequestMethod.POST). //
					produces(MediaType.TEXT_HTML_VALUE). //
					build(),
				this, //
				handler //
			);

		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}
	}

	protected AbstractService<R, O> prepareService(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {
		assert httpServletRequest != null;
		assert httpServletResponse != null;

		AbstractService<R, O> result;
		Request request;
		Buffer buffer;
		Response response;
		String command;
		String method;

		request = Request.from(httpServletRequest);
		buffer = new Buffer();
		response = Response.from(httpServletResponse);

		command = request.getCommand();
		method = request.getMethod();

		if (this.commandManager.isRegistered(command))
			result = this.commandManager.getService(command, method);
		else
			result = this.panicService;

		result.initialise(request, buffer, response);

		return result;
	}

	protected void redirect(final AbstractService<R, O> service) {
		assert service != null;

		String view, command, superCommand;
		String method;

		view = null;
		command = service.getRequest().getCommand();
		superCommand = this.commandManager.getSuperCommand(command);
		method = service.getRequest().getMethod();

		if (service.getResponse().getOops() != null)
			view = "master/panic";
		else if (method.equals("GET")) {
			if (superCommand.equals("list"))
				view = this.metadata.getListView();
			else if (StringHelper.anyOf(superCommand, "show|create|update|delete|perform"))
				view = this.metadata.getFormView();
			else
				assert false;
		} else {
			assert method.equals("POST");
			if (superCommand.equals("perform") || service.getBuffer().getErrors().hasErrors())
				view = this.metadata.getFormView();
			else if (StringHelper.anyOf(superCommand, "create|update|delete"))
				view = "master/referrer";
			else
				assert false : String.format("Command '%s' does not support method '%s'.", command, method);
		}
		assert view != null;

		service.getResponse().setView(view);
	}

	@SuppressWarnings("unchecked")
	protected void apply(final AbstractService<R, O> service, final Consumer<O> objectMethod, final Consumer<Collection<O>> collectionMethod) {
		assert service != null;
		assert objectMethod != null;
		assert collectionMethod != null;

		Collection<?> objects;

		objects = service.getBuffer().getData().values();

		for (final Object object : objects) {
			assert ReflectionHelper.isAssignable(this.metadata.getObjectClazz(), object) : //
				String.format("Object '%s' has type '%s', but '%s' was expected.", object.toString(), object.getClass().getName(), this.metadata.getObjectClazz().getName());
			objectMethod.accept((O) object);
		}

		// HINT: the assert in the previous loop guarantees that the following 
		// HINT+ type cast is safe.
		collectionMethod.accept((Collection<O>) objects);
	}

	protected void validate(final AbstractService<R, O> service) {
		assert service != null;

		Dataset data;
		Errors errors;
		Validator validator;

		data = service.getBuffer().getData();
		errors = service.getBuffer().getErrors();
		validator = FactoryHelper.getValidator();

		for (final Object datum : data.values()) {
			String key;
			BeanPropertyBindingResult bindingResult;

			key = StringHelper.toIdentity(datum);
			bindingResult = new BeanPropertyBindingResult(datum, key);
			validator.validate(datum, bindingResult);
			ErrorsHelper.transferErrors(bindingResult, errors);
		}
	}

	protected ModelAndView buildResult(final AbstractService<R, O> service) {
		assert service != null;

		ModelAndView result;
		Throwable oops;
		String viewName, viewTitle;
		boolean isVirtualView;

		result = new ModelAndView();
		oops = service.getResponse().getOops();
		if (oops != null) {
			viewName = "master/panic";
			isVirtualView = false;
			result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			result.setViewName(viewName);
			result.addObject("_oops", oops);
		} else {
			result.setStatus(HttpStatus.OK);
			result.setViewName(service.getResponse().getView());
			viewName = result.getViewName();
			isVirtualView = viewName != null && StringHelper.startsWith(viewName, "redirect:|forward:", true);
		}

		if (!isVirtualView) {
			this.transferUserData(service, result);
			this.transferErrors(service, result);

			result.addObject("_command", service.getRequest().getCommand());
			result.addObject("_method", service.getRequest().getMethod());
			result.addObject("_view", result.getViewName());
			result.addObject("_server_moment", MomentHelper.getCurrentMoment());

			result.addObject("$request", service.getRequest());
			result.addObject("$buffer", service.getBuffer());
			result.addObject("$response", service.getResponse());

			if (!result.getModel().containsKey("_view_title")) {
				viewTitle = String.format("%s.title", result.getViewName().replace("/", ".")); // NOSONAR
				result.addObject("_view_title", viewTitle);
			}
		}

		return result;
	}

	protected Map<String, Object> getUserData(final AbstractService<R, O> service) {
		assert service != null;

		Map<String, Object> result;
		Collection<Entry<String, Object>> globals;
		Dataset dataset;
		boolean mustIndex;
		int index;

		result = new LinkedHashMap<String, Object>();

		globals = service.getResponse().getGlobalEntries();
		for (final Entry<String, Object> global : globals) {
			String key;
			Object object;

			key = global.getKey();
			object = global.getValue();
			result.put(key, object);
		}

		dataset = service.getResponse().getData();
		mustIndex = dataset.size() >= 2 || this.commandManager.getSuperCommand(service.getRequest().getCommand()).equals("list");
		index = 0;
		for (final Object datasets : dataset.values()) {
			assert ReflectionHelper.isAssignable(Dataset.class, datasets) : //
				String.format("Your response data contains datum '%s' of type '%s', but it must be of type '%s'.", datasets.toString(), datasets.getClass().getName(), Dataset.class.getName());
			for (final Entry<String, Object> entry : ((Dataset) datasets).entrySet()) {
				String attribute;

				if (mustIndex)
					attribute = String.format("%s[%s]", entry.getKey(), index);
				else
					attribute = String.format("%s", entry.getKey());
				result.put(attribute, entry.getValue());
			}
			index++;
		}

		return result;
	}

	protected void transferMap(final ModelAndView target, final Map<String, Object> source) {
		assert target != null;
		assert source != null;

		for (final Entry<String, Object> entry : source.entrySet()) {
			String key;
			Object object;

			key = entry.getKey();
			object = entry.getValue();
			target.addObject(key, object);
			target.addObject(key, entry.getValue());
		}
	}

	protected void transferUserData(final AbstractService<R, O> service, final ModelAndView target) {
		assert service != null;
		assert target != null;

		Map<String, Object> data;
		Dataset dataset;

		data = this.getUserData(service);
		this.transferMap(target, data);
		dataset = service.getResponse().getData();
		target.addObject("$number$data", dataset.size());
	}

	protected Map<String, Object> getUserErrors(final AbstractService<R, O> service) {
		assert service != null;

		Map<String, Object> result;
		Errors errors;

		result = new LinkedHashMap<String, Object>();
		errors = service.getBuffer().getErrors();
		for (final Entry<String, List<String>> entry : errors) {
			String name;
			List<String> messages;
			String text;

			name = String.format("%s$error", entry.getKey());
			messages = entry.getValue();
			text = StringHelper.toString(messages, ". ", ".");
			result.put(name, text);
		}

		return result;
	}

	protected void transferErrors(final AbstractService<R, O> service, final ModelAndView target) {
		assert service != null;
		assert target != null;

		Map<String, Object> errors;

		errors = this.getUserErrors(service);
		this.transferMap(target, errors);
	}

	protected void detachPersistenceContext() {
		EntityManager entityManager;
		SessionImplementor session;
		org.hibernate.engine.spi.PersistenceContext context;
		Entry<Object, EntityEntry>[] entries;
		Object entity;

		entityManager = FactoryHelper.getEntityManager();
		session = entityManager.unwrap(SessionImplementor.class);
		context = session.getPersistenceContext();
		entries = context.reentrantSafeEntityEntries();

		for (final Entry<Object, EntityEntry> entry : entries) {
			entity = entry.getKey();
			entityManager.detach(entity);
		}
	}

}
