/*
 * ExtendedSecurityExpressionRoot.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.extensions;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import acme.client.data.AbstractRole;
import acme.client.helpers.PrincipalHelper;
import acme.client.helpers.StringHelper;

public class ExtendedSecurityExpressionRoot extends SecurityExpressionRoot {

	// Internal state ---------------------------------------------------------

	private HttpServletRequest request;

	// Constructors -----------------------------------------------------------


	public ExtendedSecurityExpressionRoot(final Authentication authentication) {
		super(authentication);

		assert authentication != null;
	}

	public ExtendedSecurityExpressionRoot(final Authentication authentication, final FilterInvocation filterInvocation) {
		super(authentication);

		assert authentication != null;
		assert filterInvocation != null;

		this.request = filterInvocation.getRequest();
	}

	public final boolean isActingLike(final String role) {
		assert !StringHelper.isBlank(role);

		boolean result;
		Class<? extends AbstractRole> activeRole;

		activeRole = PrincipalHelper.get().getActiveRole();
		result = activeRole != null && activeRole.getName().equals(role);

		return result;
	}

	public final boolean hasIpAddress(final String ipAddress) {
		assert !StringHelper.isBlank(ipAddress);

		boolean result;
		IpAddressMatcher matcher;

		matcher = new IpAddressMatcher(ipAddress);
		result = matcher.matches(this.request);

		return result;
	}

}
