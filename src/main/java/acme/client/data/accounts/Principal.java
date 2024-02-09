/*
 * Principal.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.data.accounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import acme.client.data.AbstractRole;
import acme.client.helpers.CollectionHelper;
import acme.client.helpers.StringHelper;
import acme.internals.helpers.ReflectionHelper;
import lombok.ToString;

@ToString(of = {
	"username", "activeRole", "authorities"
})
public class Principal implements UserDetails {

	// Serialisation identifier -----------------------------------------------

	private static final long							serialVersionUID	= 1L;

	// Internal state ---------------------------------------------------------

	private String										username;
	private String										password;
	private boolean										enabled;
	private Collection<GrantedAuthority>				authorities;
	private Class<? extends AbstractRole>				activeRole;
	private Map<Class<? extends AbstractRole>, Integer>	roleMap;
	private int											accountId;

	// UserDetails interface --------------------------------------------------


	@Override
	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		assert !StringHelper.isBlank(username);

		this.username = username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		assert !StringHelper.isBlank(password);

		this.password = password;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	public void setAuthorities(final Collection<AbstractRole> roles) {
		assert !CollectionHelper.someNull(roles);

		GrantedAuthority authority;

		this.roleMap = new LinkedHashMap<Class<? extends AbstractRole>, Integer>();
		this.authorities = new ArrayList<GrantedAuthority>();
		for (final AbstractRole role : roles) {
			authority = role.getAuthority();
			this.authorities.add(authority);
			this.roleMap.put(role.getClass(), role.getId());
		}
	}

	// Role management --------------------------------------------------------

	public boolean hasRole(final Class<? extends AbstractRole> clazz) {
		assert clazz != null;
		assert this.getAuthorities() != null;

		boolean result;

		if (clazz.equals(Any.class) || clazz.equals(Authenticated.class) && this.isAuthenticated())
			result = true;
		else
			result = this.roleMap.containsKey(clazz);

		return result;
	}

	public boolean hasRole(final AbstractRole role) {
		assert role != null;
		assert this.getAuthorities() != null;

		boolean result;

		if (role.getClass().equals(Any.class) || role.getClass().equals(Authenticated.class) && this.isAuthenticated())
			result = true;
		else
			// HINT: this is a lot faster than iterating over the role instances.
			result = this.accountId == role.getUserAccount().getId();

		return result;
	}

	public boolean hasRole(final String name) {
		assert !StringHelper.isBlank(name);
		assert this.getAuthorities() != null;
		assert ReflectionHelper.existsClass(name, AbstractRole.class);

		boolean result;
		Class<? extends AbstractRole> clazz;

		clazz = ReflectionHelper.getClass(name, AbstractRole.class);
		result = this.hasRole(clazz);

		return result;
	}

	public Class<? extends AbstractRole> getActiveRole() {
		Class<? extends AbstractRole> result;

		result = this.activeRole;

		return result;
	}

	public void setActiveRole(final Class<? extends AbstractRole> roleClazz) {
		assert roleClazz != null && this.hasRole(roleClazz);

		this.activeRole = roleClazz;
	}

	public int getActiveRoleId() {
		int result;

		assert this.roleMap.containsKey(this.activeRole);
		result = this.roleMap.get(this.activeRole);
		assert result != 0 : String.format("Cannot get the id of a virtual role ('%s').", this.activeRole.getName());

		return result;
	}

	public Collection<Class<? extends AbstractRole>> getRoles() {
		Collection<Class<? extends AbstractRole>> result;

		result = this.roleMap.keySet();

		return result;
	}

	public int getAccountId() {
		return this.accountId;
	}

	public void setAccountId(final int accountId) {
		assert accountId != 0;

		this.accountId = accountId;
	}

	// Derived attributes -----------------------------------------------------

	public boolean isAnonymous() {
		boolean result;

		result = this.username.equals("anonymous");

		return result;
	}

	public boolean isAuthenticated() {
		boolean result;

		result = !this.username.equals("anonymous");

		return result;
	}

}
