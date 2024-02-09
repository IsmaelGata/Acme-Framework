/*
 * CsrfHeaderFilter.java
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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class CsrfHeaderFilter extends OncePerRequestFilter {

	// OncePerRequestFilter interface -----------------------------------------

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
		assert request != null;
		assert response != null;
		assert filterChain != null;

		final CsrfToken container;
		String token;

		container = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		token = container != null ? container.getToken() : null;

		if (token != null)
			response.addHeader("X-CSRF-TOKEN", token);

		filterChain.doFilter(request, response);
	}
}
