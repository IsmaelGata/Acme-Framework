/*
 * UserAccount.java
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
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import acme.client.data.AbstractEntity;
import acme.client.data.AbstractRole;
import acme.client.helpers.StringHelper;
import acme.internals.helpers.PasswordHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(of = {
	"username"
})
@Table(indexes = {
	@Index(columnList = "username", unique = true)
})
public class UserAccount extends AbstractEntity {

	// Serialisation identifier -----------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@NotBlank
	@Length(min = 5, max = 60)
	@Column(nullable = false)
	private String				username;

	@NotBlank
	@Length(min = 5, max = 60)
	@Column(nullable = false)
	private String				password;


	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		assert StringHelper.isBlank(password) || !PasswordHelper.isEncoded(password);

		if (!StringHelper.isBlank(password))
			this.password = PasswordHelper.encode(password);
	}


	private boolean				enabled;

	// HINT: the user identity must be serialised as a blob to allow developers to 
	// HINT+ redefine it in projects that use the framework.
	@Valid
	@Lob
	@Column(columnDefinition = "blob")
	private DefaultUserIdentity	identity;

	// Derived attributes -----------------------------------------------------


	@Transient
	public boolean isAnonymous() {
		boolean result;

		result = this.username.equals("anonymous");

		return result;
	}

	@Transient
	public boolean isAuthenticated() {
		boolean result;

		result = !this.username.equals("anonymous");

		return result;
	}

	// Relationships ----------------------------------------------------------


	// HINT: keep the "@NotEmpty" annotation commented until the populator can handle 
	// HINT+ this attribute properly. The check is currently implemented using a 
	// HINT+ custom procedure.
	// @NotEmpty
	@OneToMany(mappedBy = "userAccount")
	private Collection<AbstractRole> userRoles;

	// Business methods -------------------------------------------------------


	@Transient
	public String getAuthorityString() {
		String result;
		String comma;
		StringBuilder buffer;

		comma = "";
		buffer = new StringBuilder();
		for (final AbstractRole role : this.userRoles) {
			buffer.append(comma);
			buffer.append(role.getAuthorityName());
			comma = ", ";
		}
		result = buffer.toString();

		return result;
	}

	@Transient
	public boolean hasRole(final AbstractRole role) {
		assert role != null;

		boolean result;

		result = this.userRoles != null && this.userRoles.contains(role);

		return result;
	}

	@Transient
	public boolean hasRole(final Class<? extends AbstractRole> clazz) {
		assert clazz != null;

		boolean result;

		result = this.userRoles != null && this.getRole(clazz) != null;

		return result;
	}

	@Transient
	@SuppressWarnings("unchecked")
	public <T extends AbstractRole> T getRole(final Class<? extends AbstractRole> clazz) {
		assert clazz != null;

		T result;
		Iterator<AbstractRole> iterator;
		AbstractRole role;

		result = null;
		if (this.userRoles != null) {
			iterator = this.userRoles.iterator();
			while (result == null && iterator.hasNext()) {
				role = iterator.next();
				result = role.getClass().equals(clazz) ? (T) role : null;
			}
		}

		return result;
	}

	@Transient
	@SuppressWarnings("unchecked")
	public <T extends AbstractRole> T getRole(final String name) {
		assert !StringHelper.isBlank(name);

		T result;
		Iterator<AbstractRole> iterator;
		AbstractRole role;

		result = null;
		if (this.userRoles != null) {
			iterator = this.userRoles.iterator();
			while (result == null && iterator.hasNext()) {
				role = iterator.next();
				result = role.getAuthorityName().equals(name) ? (T) role : null;
			}
		}

		return result;
	}

	public void addRole(final AbstractRole role) {
		assert role != null;
		assert !this.hasRole(role.getClass());

		if (this.userRoles == null)
			this.userRoles = new ArrayList<AbstractRole>();

		this.userRoles.add(role);
	}

	public void removeRole(final AbstractRole role) {
		assert role != null;
		assert this.hasRole(role);

		this.userRoles.remove(role);
	}

}
