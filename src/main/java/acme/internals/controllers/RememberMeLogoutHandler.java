/*
 * RememberMeLogoutHandler.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import acme.client.helpers.PrincipalHelper;
import acme.internals.components.exceptions.PassThroughException;
import acme.internals.helpers.LoggerHelper;

public final class RememberMeLogoutHandler implements LogoutHandler {

	// LogoutHandler interface ------------------------------------------------

	@Override
	public void logout(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) {
		assert request != null;
		assert response != null;
		// HINT: authentication can be null

		PassThroughException auch;

		LoggerHelper.preHandle(request, response, this);
		try {
			PrincipalHelper.handleSignOut();
			auch = null;
		} catch (final Throwable oops) {
			auch = new PassThroughException(oops);
		}
		LoggerHelper.postHandle(request, response, this, null);
		LoggerHelper.afterCompletion(request, response, this, auch);

		if (auch != null)
			throw auch;
	}
}
