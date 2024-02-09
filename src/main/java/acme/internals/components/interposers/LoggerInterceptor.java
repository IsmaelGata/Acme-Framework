/*
 * LoggerInterceptor.java
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import acme.internals.helpers.LoggerHelper;

public class LoggerInterceptor implements HandlerInterceptor {

	// HandlerInterceptor interface -------------------------------------------

	// HINT: note that requests to /any/system/sign-in or /authenticated/system/sign-out cannot be 
	// HINT+ intercepted here!  Please, take a look at AuthenticationFilter and RememberMeLogoutHandler.

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws ServletException {
		assert request != null;
		assert response != null;
		assert handler != null;

		LoggerHelper.preHandle(request, response, handler);

		return true;
	}

	@Override
	public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView) throws Exception {
		assert request != null;
		assert response != null;
		assert handler != null;
		// HINT: modelAndView can be null

		LoggerHelper.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception oops) throws Exception {
		assert request != null;
		assert response != null;
		assert handler != null;
		// HINT: oops can be null

		LoggerHelper.afterCompletion(request, response, handler, oops);
	}

}
