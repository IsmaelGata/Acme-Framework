/*
 * AcmeBrowser.java
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

import java.net.CookieManager;
import java.net.CookieStore;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;

import acme.client.helpers.StringHelper;
import acme.internals.helpers.EnvironmentHelper;

import org.jsoup.Jsoup;

public class AcmeBrowser {

	// Constructors -----------------------------------------------------------

	public AcmeBrowser() {
		this.cookieManager = new CookieManager();
		this.cookieStore = this.cookieManager.getCookieStore();
		this.csrfToken = null;
	}

	// Internal state ---------------------------------------------------------


	private CookieManager	cookieManager;
	private CookieStore		cookieStore;
	private String			csrfToken;

	// Business methods -------------------------------------------------------


	public void reset() {
		this.cookieStore.removeAll();
		this.csrfToken = null;
	}

	public Response request(final String method, final String path, final String query, final Map<String, String> data) {
		assert StringHelper.anyOf(method, "GET|POST");
		assert !StringHelper.isBlank(path);
		assert query != null;
		// HINT: data can be null		

		Response result;
		String protocol, host, port, context, separator, url;
		Connection connection;
		Throwable auch;

		try {
			protocol = "http";
			host = "localhost";
			port = EnvironmentHelper.getRequiredProperty("server.port", String.class);
			context = EnvironmentHelper.getRequiredProperty("server.servlet.contextPath", String.class);
			separator = StringHelper.isBlank(query) ? "" : query.startsWith("?") ? "" : "?";
			url = String.format("%s://%s:%s%s%s%s%s", protocol, host, port, context, path, separator, query);

			connection = Jsoup.connect(url);
			connection.cookieStore(this.cookieStore);
			connection.timeout((int) TimeUnit.MINUTES.toMillis(5));
			connection.method(Connection.Method.valueOf(method));
			connection.followRedirects(false);
			connection.header("Accept", "*/*");
			connection.ignoreContentType(true);
			connection.ignoreHttpErrors(true);

			if (method.equals("POST")) {
				if (data != null)
					connection.data(data);
				if (this.csrfToken != null) {
					connection.data("_csrf", this.csrfToken);
					connection.header("X-CSRF-TOKEN", this.csrfToken);
				}
			}

			result = connection.execute();
			this.csrfToken = result.header("X-CSRF-TOKEN");
			auch = null;
		} catch (final Throwable oops) {
			auch = oops;
			result = null;
		}

		assert auch != null ? result == null : result != null;

		return result;
	}

}
