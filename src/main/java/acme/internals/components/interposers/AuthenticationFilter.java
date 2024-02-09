/*
 * AuthenticationFilter.java
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import acme.client.helpers.StringHelper;
import acme.internals.helpers.LoggerHelper;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	// Constructor ------------------------------------------------------------

	public AuthenticationFilter(final String method, final String path, final AuthenticationManager authenticationManager) {
		assert StringHelper.anyOf(method, "GET|POST");
		assert !StringHelper.isBlank(path);
		assert authenticationManager != null;

		AntPathRequestMatcher matcher;

		matcher = new AntPathRequestMatcher(path, method.toUpperCase());
		this.setRequiresAuthenticationRequestMatcher(matcher);

		this.setAuthenticationManager(authenticationManager);
		this.setAuthenticationFailureHandler( //
			(request, response, exception) -> { //	
				response.setStatus(302);
				response.sendRedirect(String.format("%s?error", request.getRequestURI())); //
			});
	}

	// UsernamePasswordAuthenticationFilter interface -------------------------

	@Override
	public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticationException {
		assert request instanceof HttpServletRequest;
		assert response instanceof HttpServletResponse;

		Authentication result;
		AuthenticationException auch;

		LoggerHelper.preHandle(request, response, this);
		try {
			result = super.attemptAuthentication(request, response);
			auch = null;
		} catch (final AuthenticationException oops) {
			result = null;
			auch = oops;
		}
		LoggerHelper.postHandle(request, response, this, null);
		LoggerHelper.afterCompletion(request, response, this, auch);

		if (auch != null)
			throw auch;
		else
			return result;
	}
}
