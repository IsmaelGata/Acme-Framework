/*
 * DebugInterceptor.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.interposers;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

import acme.client.helpers.StringHelper;
import acme.internals.helpers.ServletHelper;

public class DebugInterceptor implements HandlerInterceptor {

	// HandlerInterceptor interface -------------------------------------------

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws ServletException {
		assert request != null;
		assert response != null;
		assert handler != null;

		String flag;
		Cookie cookie;

		flag = request.getParameter("debug");
		if (flag == null) {
			cookie = WebUtils.getCookie(request, "debug");
			flag = cookie != null ? cookie.getValue() : "true";
		}
		flag = StringHelper.anyOf(flag, "true|false") ? flag : "true";

		// TODO: how can the "sameSite" property be set to "Strict"?
		cookie = new Cookie("debug", flag);
		cookie.setPath(ServletHelper.getRequestContextPath(request));
		cookie.setMaxAge(3600);

		response.addCookie(cookie);

		return true;
	}

}
