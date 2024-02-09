/*
 * JspHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.helpers;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;

import acme.client.data.accounts.Principal;
import acme.client.data.datatypes.Money;
import acme.client.data.models.Dataset;
import acme.client.data.models.WorkArea;
import acme.internals.helpers.ReflectionHelper;
import acme.internals.helpers.ServletHelper;
import acme.internals.helpers.ThrowableHelper;

public class JspHelper {

	// HINT: unfortunately, JSP does not allow to invoke static methods on abstract 
	// HINT+ classes, which is the reason why this helper must not be abstract.

	// Internal state ---------------------------------------------------------

	private static Pattern	REGULAR_NAME_ALLOWED_PATTERN;
	private static Pattern	REGULAR_NAME_DISALLOWED_PATTERN;
	private static Pattern	SPECIAL_NAME_PATTERN;

	// Constructors -----------------------------------------------------------


	protected JspHelper() {
	}


	static {
		JspHelper.REGULAR_NAME_ALLOWED_PATTERN = Pattern.compile("^([\\w\\$\\[\\]])+(\\.([\\w\\$\\[\\]])+)*$", Pattern.UNICODE_CASE);
		JspHelper.REGULAR_NAME_DISALLOWED_PATTERN = Pattern.compile("^.*(\\.[A-Z]{2,}|[sS][pP][rR][iI][nN][gG]|\\.[^.]+\\.).*$", Pattern.UNICODE_CASE);
		JspHelper.SPECIAL_NAME_PATTERN = Pattern.compile("^.*(__|\\.[A-Z]{2,}|[sS][pP][rR][iI][nN][gG]|\\.[^.]+\\.).*$", Pattern.UNICODE_CASE);
	}

	// Business methods -------------------------------------------------------


	public static String format(final Throwable oops) {
		assert oops != null;

		String result;

		result = ThrowableHelper.toString(oops);

		return result;
	}

	public static String format(final String text) {
		assert text != null;

		String result;

		result = ThrowableHelper.formatText(text);

		return result;
	}

	public static boolean matches(final String text, final String regex) {
		assert text != null;
		assert !StringHelper.isBlank(regex);

		boolean result;

		result = StringHelper.matches(text, regex);

		return result;
	}

	public static boolean anyOf(final String text, final String choices) {
		assert text != null;
		assert !StringHelper.isBlank(choices);

		boolean result;

		result = StringHelper.anyOf(text, choices);

		return result;
	}

	public static boolean isBlank(final String text) {
		// HINT: text can be null

		boolean result;

		result = StringHelper.isBlank(text);

		return result;
	}

	public static boolean isRegularName(final String name) {
		assert !StringHelper.isBlank(name);

		boolean result;
		Matcher matcher1, matcher2;

		matcher1 = JspHelper.REGULAR_NAME_ALLOWED_PATTERN.matcher(name);
		matcher2 = JspHelper.REGULAR_NAME_DISALLOWED_PATTERN.matcher(name);
		result = matcher1.matches() && !matcher2.matches();

		return result;
	}

	public static boolean isSpecialName(final String name) {
		assert !StringHelper.isBlank(name);

		boolean result;
		Matcher matcher;

		matcher = JspHelper.SPECIAL_NAME_PATTERN.matcher(name);
		result = matcher.matches();

		return result;
	}

	public static String getRequestUrl(final HttpServletRequest request) {
		assert request != null;

		String result;
		String scheme, server, port, uri, query;

		scheme = request.getScheme();
		server = request.getServerName();
		port = String.valueOf(request.getServerPort());
		uri = (String) request.getAttribute("javax.servlet.forward.request_uri");
		query = (String) request.getAttribute("javax.servlet.forward.query_string");
		result = String.format("%s://%s:%s%s%s", scheme, server, port, uri, StringHelper.isBlank(query) ? "" : "?" + query);

		return result;
	}

	public static String getRequestMethod(final HttpServletRequest request) {
		assert request != null;

		String result;

		result = request.getMethod();

		return result;
	}

	public static String getRequestLocale(final HttpServletRequest request) {
		assert request != null;

		String result;
		Cookie language;

		language = WebUtils.getCookie(request, "language");
		result = language != null && language.getValue() != null ? language.getValue() : "en";

		return result;
	}

	public static String getBaseUrl(final HttpServletRequest request) {
		assert request != null;

		String result;
		String scheme, server, port, context;

		scheme = request.getHeader("X-Forwarded-Proto");
		if (scheme == null)
			scheme = request.getScheme();
		server = request.getHeader("X-Forwarded-Host");
		if (server == null)
			server = request.getServerName();
		port = request.getHeader("X-Forwarded-Port");
		if (port == null)
			port = String.valueOf(request.getServerPort());
		context = request.getContextPath();
		result = String.format("%s://%s:%s%s/", scheme, server, port, context);

		return result;
	}

	public static String getRequestQuery(final HttpServletRequest request, final HttpServletResponse response) {
		assert request != null;
		assert response != null;

		String result;

		result = ServletHelper.getRequestQuery(request);

		return result;
	}

	public static String getRequestPayload(final HttpServletRequest request, final HttpServletResponse response) {
		assert request != null;
		assert response != null;

		String result;

		result = ServletHelper.getRequestPayload(request);

		return result;
	}

	public static String getResponsePayload(final HttpServletRequest request, final HttpServletResponse response) {
		assert request != null;
		assert response != null;

		String result;

		result = ServletHelper.getResponsePayload(request, response);

		return result;
	}

	public static String computeDataSort(final Object object) {
		// HINT: object can be null

		String result;
		SimpleDateFormat simpleDateFormat;
		Date date;
		String criteria;

		if (object instanceof Date) {
			simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			criteria = simpleDateFormat.format((Date) object);
		} else if (object instanceof Timestamp) {
			date = new Date(((Timestamp) object).getTime());
			simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			criteria = simpleDateFormat.format(date);
		} else if (object instanceof Money)
			criteria = String.format("%s %010.2f", ((Money) object).getCurrency(), ((Money) object).getAmount());
		else if (object instanceof Double)
			criteria = String.format("%010.2f", (Double) object);
		else if (object instanceof Integer)
			criteria = String.format("%010d", (Integer) object);
		else
			criteria = null;

		result = criteria == null ? "" : String.format("data-sort=\"%s\"", criteria);

		return result;
	}

	public static String computeDataText(final Object object, final String format) {
		// HINT: value can be null
		assert !StringHelper.isBlank(format);

		String result;
		MessageFormat formatter;

		if (format.equals("{0}") && ConversionHelper.canConvert(object, String.class))
			result = ConversionHelper.convert(object, String.class);
		else {
			formatter = new MessageFormat(format);
			result = formatter.format(object);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static void updateDatatableColumns(final HttpServletRequest request, final Map<String, Object> column) {
		assert request != null;
		assert column != null;

		Collection<Map<String, Object>> columns;

		columns = (Collection<Map<String, Object>>) request.getAttribute("$data$table$columns");
		columns.add(column);
	}

	public static Map<String, Object[]> computeHighlightData(final HttpServletRequest request, final HttpServletResponse response) {
		assert request != null;
		assert response != null;

		Map<String, Object[]> result;
		Principal principal;
		String principalDisplay;

		result = new LinkedHashMap<String, Object[]>();
		principal = PrincipalHelper.get();
		principalDisplay = String.format("%s / %s", principal.getUsername(), principal.getActiveRole().getSimpleName());

		JspHelper.appendDebugPanelData(result, "Server moment", MomentHelper.getCurrentMoment());
		JspHelper.appendDebugPanelData(result, "Principal", principalDisplay);
		JspHelper.appendDebugPanelData(result, "Request locale", JspHelper.getRequestLocale(request));
		JspHelper.appendDebugPanelData(result, "Request method", JspHelper.getRequestMethod(request));
		JspHelper.appendDebugPanelData(result, "Request URL", JspHelper.getRequestUrl(request));
		JspHelper.appendDebugPanelData(result, "Request payload", JspHelper.getRequestPayload(request, response));
		JspHelper.appendDebugPanelData(result, "Response payload", JspHelper.getResponsePayload(request, response));

		return result;
	}

	public static void appendDebugPanelData(final Map<String, Object[]> map, final String key, final Object value) {
		assert map != null;
		assert !StringHelper.isBlank(key);
		// HINT: value can be null

		Object object;
		List<String> buffer;
		String line;
		String clazzName;
		Object[] pair;

		if (!ReflectionHelper.isAssignable(WorkArea.class, value))
			object = PrinterHelper.printObject(value, true);
		else {
			buffer = new ArrayList<String>();
			for (final Entry<String, Dataset> entry : ((WorkArea) value).getEntries()) {
				line = String.format("%s = %s", entry.getKey(), PrinterHelper.printObject(entry.getValue(), true));
				buffer.add(line);
			}
			object = buffer;
		}

		clazzName = value == null ? Object.class.getName() : value.getClass().getName();
		pair = new Object[] {
			object, clazzName
		};

		map.put(key, pair);
	}

}
