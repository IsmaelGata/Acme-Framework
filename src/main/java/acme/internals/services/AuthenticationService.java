/*
 * AuthenticationService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.services;

import java.util.ArrayList;
import java.util.Collection;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import acme.client.data.AbstractRole;
import acme.client.data.accounts.Anonymous;
import acme.client.data.accounts.Any;
import acme.client.data.accounts.Authenticated;
import acme.client.data.accounts.Principal;
import acme.client.data.accounts.UserAccount;
import acme.internals.repositories.AuthenticationRepository;;

@Service
@Transactional(TxType.SUPPORTS)
public class AuthenticationService implements UserDetailsService {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuthenticationRepository authenticationRepository;

	// Business methods -------------------------------------------------------


	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		// HINT: username can be null

		Principal result;
		UserAccount userAccount;
		Collection<AbstractRole> userRoles;
		Any anyRole;
		Authenticated authenticatedRole;

		userAccount = this.authenticationRepository.findByUsername(username);
		if (userAccount == null)
			throw new UsernameNotFoundException(username);

		userRoles = new ArrayList<AbstractRole>();
		userRoles.addAll(userAccount.getUserRoles());

		anyRole = new Any();
		anyRole.setUserAccount(userAccount);
		userRoles.add(anyRole);

		if (userAccount.isAuthenticated()) {
			assert !userAccount.hasRole(Authenticated.class);
			authenticatedRole = new Authenticated();
			authenticatedRole.setUserAccount(userAccount);
			userRoles.add(authenticatedRole);
		}

		result = new Principal();
		result.setUsername(userAccount.getUsername());
		result.setPassword(userAccount.getPassword());
		result.setEnabled(userAccount.isEnabled());
		result.setAuthorities(userRoles);
		if (userAccount.isAnonymous())
			result.setActiveRole(Anonymous.class);
		else
			result.setActiveRole(Authenticated.class);
		result.setAccountId(userAccount.getId());

		return result;
	}

}
