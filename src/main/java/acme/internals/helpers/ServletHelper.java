/*
 * ServletHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.helpers;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerMapping;

import acme.client.helpers.CollectionHelper;
import acme.client.helpers.ConversionHelper;
import acme.client.helpers.JspHelper;
import acme.client.helpers.PrinterHelper;
import acme.client.helpers.StringHelper;

public abstract class ServletHelper {

	// Constructors -----------------------------------------------------------

	protected ServletHelper() {
	}

	// Internal state ---------------------------------------------------------


	private static Pattern standardFeaturePattern;

	static {
		ServletHelper.standardFeaturePattern = Pattern.compile( //
			"^\\/(?<ROLE>[\\w\\-]+)\\/(?<OBJECT>[\\w\\-]+)\\/(?<COMMAND>[\\w\\-]+)(\\?(?<QUERY>.*))?$", //
			Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
	}

	// General methods --------------------------------------------------------


	@SuppressWarnings("unchecked")
	public static String getPathVariable(final HttpServletRequest request, final String variable) {
		assert request != null;
		assert !StringHelper.isBlank(variable);

		final String result;
		Object attribute;
		Map<String, String> pathVariables;

		attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		pathVariables = Map.class.cast(attribute);
		assert pathVariables.containsKey(variable);
		result = pathVariables.get(variable);

		return result;
	}

	public static Map<String, Object> extractParameters(final HttpServletRequest request) {
		assert request != null;

		final Map<String, Object> result;
		Enumeration<String> names;

		result = new LinkedHashMap<String, Object>();
		names = request.getParameterNames();
		for (final String name : CollectionHelper.toIterable(names)) {
			Object object;

			if (!name.contains("$proxy")) {
				object = request.getParameter(name);
				result.put(name, object);
			}
		}

		return result;
	}

	public static boolean isStandardFeature(final HttpServletRequest request) {
		assert request != null;

		boolean result;
		String path;

		path = ServletHelper.getRequestPath(request, true);
		result = ServletHelper.isStandardFeature(path);

		return result;
	}

	public static boolean isStandardFeature(final String path) {
		assert !StringHelper.isBlank(path);

		final boolean result;

		Matcher matcher;

		matcher = ServletHelper.standardFeaturePattern.matcher(path);
		result = matcher.find();

		return result;
	}

	public static boolean hasRequestForm(final HttpServletRequest request) {
		assert request != null;

		boolean result;
		final String method, mime;

		method = request.getMethod();
		mime = request.getContentType();
		result = StringHelper.isEqual(method, "POST", true) && StringHelper.startsWith(mime, "application/x-www-form-urlencoded", true);

		return result;
	}

	public static boolean hasResponseHtml(final HttpServletResponse response) {
		assert response != null;

		boolean result;
		final String contentType;

		contentType = response.getContentType();
		result = StringHelper.startsWith(contentType, "text/html", true);

		return result;
	}

	public static String encodeQuery(final Map<String, String> query) {
		assert query != null;

		String result;
		StringBuilder builder;
		String separator;

		builder = new StringBuilder();
		separator = "";
		for (final Entry<String, String> entry : query.entrySet()) {
			String key, value, assignment;

			key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
			value = entry.getValue() == null ? "" : URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
			assignment = String.format("%s=%s", key, value);

			builder.append(separator);
			builder.append(assignment);
			separator = "&";
		}
		result = builder.toString();

		return result;
	}

	public static Map<String, String> decodeQuery(final String query) {
		assert query != null;

		Map<String, String> result;
		String[] params;

		result = new TreeMap<String, String>();
		if (!StringHelper.isBlank(query)) {
			params = query.split("&");
			for (final String param : params) {
				String[] pair;
				String name, value;

				pair = param.split("=");
				name = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
				value = pair.length == 1 ? "" : URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
				result.put(name, value);
			}
		}

		return result;
	}

	// Feature-projection methods ---------------------------------------------

	public static String getFeatureRole(final HttpServletRequest request) {
		assert request != null && ServletHelper.isStandardFeature(request);

		String result;
		String path;

		path = ServletHelper.getRequestPath(request, true);
		result = ServletHelper.getFeatureRole(path);

		return result;
	}

	public static String getFeatureRole(final String path) {
		assert !StringHelper.isBlank(path) && ServletHelper.isStandardFeature(path);

		String result;
		Matcher matcher;

		matcher = ServletHelper.standardFeaturePattern.matcher(path);
		assert matcher.find();
		result = matcher.group("ROLE");

		return result;
	}

	public static String getFeatureObject(final HttpServletRequest request) {
		assert request != null && ServletHelper.isStandardFeature(request);

		String result;
		String path;

		path = ServletHelper.getRequestPath(request, true);
		result = ServletHelper.getFeatureObject(path);

		return result;
	}

	public static String getFeatureObject(final String path) {
		assert !StringHelper.isBlank(path) && ServletHelper.isStandardFeature(path);

		String result;
		Matcher matcher;

		matcher = ServletHelper.standardFeaturePattern.matcher(path);
		assert matcher.find();
		result = matcher.group("OBJECT");

		return result;
	}

	public static String getFeatureCommand(final HttpServletRequest request) {
		assert request != null && ServletHelper.isStandardFeature(request);

		String result;
		String path;

		path = ServletHelper.getRequestPath(request, true);
		result = ServletHelper.getFeatureCommand(path);

		return result;
	}

	public static String getFeatureCommand(final String path) {
		assert !StringHelper.isBlank(path) && ServletHelper.isStandardFeature(path);

		String result;
		Matcher matcher;

		matcher = ServletHelper.standardFeaturePattern.matcher(path);
		assert matcher.find();
		result = matcher.group("COMMAND");

		return result;
	}

	public static String getFeatureQuery(final HttpServletRequest request) {
		assert request != null;

		String result;
		String path;

		path = ServletHelper.getRequestPath(request, true);
		result = ServletHelper.getFeatureQuery(path);

		return result;
	}

	public static String getFeatureQuery(final String path) {
		assert !StringHelper.isBlank(path) && ServletHelper.isStandardFeature(path);

		String result;
		Matcher matcher;

		matcher = ServletHelper.standardFeaturePattern.matcher(path);
		assert matcher.find();
		result = matcher.group("QUERY");
		result = result == null ? "" : result;

		return result;
	}

	// Request-projection methods ---------------------------------------------

	public static String getRequestUrl(final HttpServletRequest request) {
		assert request != null;

		String result;
		String scheme, serverName, contextPath, featurePath;
		int serverPort;
		StringBuilder buffer;

		scheme = ServletHelper.getRequestScheme(request);
		serverName = ServletHelper.getRequestServer(request);
		serverPort = ServletHelper.getRequestPort(request);
		contextPath = ServletHelper.getRequestContextPath(request);
		featurePath = ServletHelper.getRequestPath(request, false);

		assert !StringHelper.isBlank(featurePath);
		assert StringHelper.isBlank(request.getPathInfo());

		buffer = new StringBuilder();
		buffer.append(scheme).append("://").append(serverName);
		buffer.append(":").append(serverPort);
		buffer.append(contextPath);
		buffer.append(featurePath);

		result = buffer.toString();

		return result;
	}

	public static String getRequestContentType(final HttpServletRequest request) {
		assert request != null;

		String result;

		result = request.getContentType();

		return result;
	}

	public static String getRequestMethod(final HttpServletRequest request) {
		assert request != null;

		String result;

		result = request.getMethod();

		return result;
	}

	public static String getRequestScheme(final HttpServletRequest httpServletRequest) {
		assert httpServletRequest != null;

		String result;

		result = httpServletRequest.getScheme();

		return result;
	}

	public static String getRequestServer(final HttpServletRequest request) {
		assert request != null;

		String result;

		result = request.getServerName();

		return result;
	}

	public static int getRequestPort(final HttpServletRequest request) {
		assert request != null;

		int result;

		result = request.getServerPort();

		return result;
	}

	public static String getRequestContextPath(final HttpServletRequest request) {
		assert request != null;

		String result;

		result = request.getContextPath();

		return result;
	}

	public static String getRequestPath(final HttpServletRequest request, final boolean withQuery) {
		assert request != null;

		String result;
		String query;

		result = request.getServletPath();
		if (withQuery) {
			query = ServletHelper.getRequestQuery(request);
			if (!StringHelper.isBlank(query))
				result = String.format("%s?%s", result, query);
		}

		return result;
	}

	public static String getRequestQuery(final HttpServletRequest request) {
		assert request != null;

		String result;

		result = request.getQueryString();
		result = StringHelper.isBlank(result) ? "" : result;

		return result;
	}

	public static String getRequestPayload(final HttpServletRequest request) {
		assert request != null;

		String result;
		Map<String, String> assignments;
		Enumeration<String> names;

		if (!ServletHelper.hasRequestForm(request))
			result = "";
		else {
			assignments = new LinkedHashMap<String, String>();

			names = request.getParameterNames();
			for (final String name : CollectionHelper.toIterable(names)) {
				String value;

				if (JspHelper.isRegularName(name) && !name.contains("$") && !name.startsWith("_")) {
					value = request.getParameter(name);
					assignments.put(name, value);
				}
			}
			result = ServletHelper.encodeQuery(assignments);
		}

		return result;
	}

	// Response-projection methods --------------------------------------------

	public static int getResponseStatus(final HttpServletResponse response) {
		assert response != null;

		int result;

		result = response.getStatus();

		return result;
	}

	public static String getResponseContentType(final HttpServletResponse response) {
		assert response != null;

		String result;

		result = response.getContentType();
		result = result == null ? "" : result;

		return result;
	}

	// HINT: believe it or not, the response data is in the request!
	public static String getResponsePayload(final HttpServletRequest request, final HttpServletResponse response) {
		assert request != null;
		assert response != null;

		String result;
		Map<String, String> assignments;
		Enumeration<String> names;

		if (!ServletHelper.hasResponseHtml(response))
			result = "";
		else {
			assignments = new LinkedHashMap<String, String>();
			names = request.getAttributeNames();
			for (final String name : CollectionHelper.toIterable(names)) {
				String value;
				Object object;

				if (JspHelper.isRegularName(name) && (!name.contains("$") || name.endsWith("$error")) && !name.startsWith("_")) {
					object = request.getAttribute(name);
					if (ConversionHelper.canConvert(object, String.class))
						value = ConversionHelper.convert(object, String.class);
					else
						value = PrinterHelper.printObject(object, true);
					assignments.put(name, value);
				}
			}
			result = ServletHelper.encodeQuery(assignments);
		}

		return result;
	}

	// HINT: believe it or not, the response data is in the request!
	public static String getResponseOops(final HttpServletRequest request, final HttpServletResponse response, final Throwable oops) {
		assert request != null;
		assert response != null;
		// HINT: oops can be null

		String result;
		Enumeration<String> names;

		if (oops != null)
			result = oops.getMessage();
		else {
			result = null;
			names = request.getAttributeNames();
			while (result == null && names.hasMoreElements()) {
				String name;
				Object object;

				name = names.nextElement();
				if (name.equals("_oops")) {
					object = request.getAttribute(name);
					if (object == null)
						result = "";
					else {
						assert object instanceof Throwable;
						result = ((Throwable) object).getMessage();
					}
				}
			}
		}

		return result;
	}

}
