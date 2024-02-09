/*
 * Request.java
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

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.WebUtils;

import acme.client.data.accounts.Principal;
import acme.client.helpers.PrincipalHelper;
import acme.client.helpers.StringHelper;
import acme.internals.helpers.ServletHelper;

public class Request extends WorkArea {

	// Constructors -----------------------------------------------------------

	public Request() {
		super.addGlobal("$url", null);
		super.addGlobal("$method", null);
		super.addGlobal("$command", null);
		super.addGlobal("$principal", null);
		super.addGlobal("$locale", null);
	}

	public static Request from(final HttpServletRequest httpServletRequest) {
		assert httpServletRequest != null;

		Request result;
		Map<String, Object> parameters;

		result = new Request();

		result.addGlobal("$url", httpServletRequest.getRequestURI());
		result.addGlobal("$method", httpServletRequest.getMethod());
		result.addGlobal("$command", ServletHelper.getFeatureCommand(httpServletRequest));
		result.addGlobal("$principal", PrincipalHelper.get());
		result.addGlobal("$locale", Request.buildLocale(httpServletRequest));

		parameters = ServletHelper.extractParameters(httpServletRequest);
		result.addData(parameters.keySet(), parameters.values());

		return result;
	}

	// Properties -------------------------------------------------------------

	public String getUrl() {
		assert this.hasGlobal("$url", String.class);

		String result;

		result = super.getGlobal("$url", String.class);

		return result;
	}

	public void setUrl(final String url) {
		assert !StringHelper.isBlank(url);

		super.addGlobal("$url", url);
	}

	public String getMethod() {
		assert this.hasGlobal("$method", String.class);

		String result;

		result = super.getGlobal("$method", String.class);

		return result;
	}

	public void setMethod(final String method) {
		assert method != null;

		super.addGlobal("$method", method);
	}

	public String getCommand() {
		assert this.hasGlobal("$command", String.class);

		String result;

		result = super.getGlobal("$command", String.class);

		return result;
	}

	public void setCommand(final String command) {
		assert !StringHelper.isBlank(command);

		super.addGlobal("$command", command);
	}

	public Principal getPrincipal() {
		assert this.hasGlobal("$principal", Principal.class);

		Principal result;

		result = this.getGlobal("$principal", Principal.class);

		return result;
	}

	public void setPrincipal(final Principal principal) {
		assert principal != null;

		this.addGlobal("$principal", principal);
	}

	public Locale getLocale() {
		assert this.hasGlobal("$locale", Locale.class);

		Locale result;

		result = super.getGlobal("$locale", Locale.class);

		return result;
	}

	public void setLocale(final Locale locale) {
		assert locale != null;

		super.addGlobal("$locale", locale);
	}

	// Ancillary methods ------------------------------------------------------

	private static Locale buildLocale(final HttpServletRequest httpServletRequest) {
		assert httpServletRequest != null;

		Locale result;
		Cookie cookie;
		String language;

		cookie = WebUtils.getCookie(httpServletRequest, "locale");
		language = cookie != null ? cookie.getValue() : "en";
		result = Locale.of(language);

		return result;

	}

}
