/*
 * PrincipalHelper.java
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

import java.util.Collection;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import acme.client.data.accounts.Principal;
import acme.internals.helpers.FactoryHelper;
import acme.internals.services.AuthenticationService;

public class PrincipalHelper {

	// Constructors -----------------------------------------------------------

	protected PrincipalHelper() {
	}


	static {
		PrincipalHelper.STRONG_KEY = "\\/3ry-$tr0ng-|Or1nc1p@L-K3y!";
		PrincipalHelper.ANONYMOUS = "anonymous";
	}

	// Internal state ---------------------------------------------------------

	private static String	STRONG_KEY;
	private static String	ANONYMOUS;

	// Business methods -------------------------------------------------------


	@Transactional(TxType.SUPPORTS)
	public static Principal get() {
		Principal result;
		SecurityContext context;
		Authentication token;
		AuthenticationService service;
		Collection<GrantedAuthority> authorities;

		context = SecurityContextHolder.getContext();
		token = context.getAuthentication();
		service = FactoryHelper.getAuthenticationService();
		assert token instanceof RememberMeAuthenticationToken || //
			token instanceof UsernamePasswordAuthenticationToken || //
			token instanceof TestingAuthenticationToken || //
			token instanceof AnonymousAuthenticationToken;

		if (token instanceof RememberMeAuthenticationToken || //
			token instanceof UsernamePasswordAuthenticationToken || //
			token instanceof TestingAuthenticationToken)
			result = (Principal) token.getPrincipal();
		else {
			result = (Principal) service.loadUserByUsername(PrincipalHelper.ANONYMOUS);
			authorities = result.getAuthorities();
			token = new AnonymousAuthenticationToken(PrincipalHelper.STRONG_KEY, result, authorities);
			context.setAuthentication(token);
		}

		return result;
	}

	@Transactional(TxType.SUPPORTS)
	public static void handleUpdate() {
		assert PrincipalHelper.get().isAuthenticated();

		SecurityContext context;
		AuthenticationService service;
		Authentication currentToken, newToken;
		Principal currentPrincipal, newPrincipal;
		Collection<GrantedAuthority> newAuthorities;

		context = SecurityContextHolder.getContext();
		currentToken = context.getAuthentication();
		assert currentToken instanceof RememberMeAuthenticationToken || currentToken instanceof UsernamePasswordAuthenticationToken;
		currentPrincipal = (Principal) currentToken.getPrincipal();
		service = FactoryHelper.getAuthenticationService();
		newPrincipal = (Principal) service.loadUserByUsername(currentPrincipal.getUsername());
		newAuthorities = newPrincipal.getAuthorities();

		if (currentToken instanceof RememberMeAuthenticationToken)
			newToken = new RememberMeAuthenticationToken(PrincipalHelper.STRONG_KEY, newPrincipal, newAuthorities);
		else {
			assert currentToken instanceof UsernamePasswordAuthenticationToken;
			newToken = new UsernamePasswordAuthenticationToken(newPrincipal, null, newAuthorities);
		}

		context.setAuthentication(newToken);
	}

	@Transactional(TxType.SUPPORTS)
	public static void handleSignOut() {
		SecurityContext context;
		AuthenticationService service;
		Principal principal;
		Collection<GrantedAuthority> authorities;
		Authentication authentication;

		context = SecurityContextHolder.getContext();
		service = FactoryHelper.getAuthenticationService();
		principal = (Principal) service.loadUserByUsername(PrincipalHelper.ANONYMOUS);
		authorities = principal.getAuthorities();
		authentication = new AnonymousAuthenticationToken(PrincipalHelper.STRONG_KEY, principal, authorities);
		context.setAuthentication(authentication);
	}

}
