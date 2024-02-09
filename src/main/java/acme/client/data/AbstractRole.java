/*
 * AbstractRole.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import acme.client.data.accounts.DefaultUserIdentity;
import acme.client.data.accounts.UserAccount;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(of = {
	"userAccount"
})
public abstract class AbstractRole extends AbstractEntity {

	// Serialisation identifier -----------------------------------------------

	private static final long serialVersionUID = 1L;

	// Attributes -------------------------------------------------------------


	@Transient
	public GrantedAuthority getAuthority() {
		GrantedAuthority result;
		String authority;

		authority = String.format("AUTH_%s", this.getClass().getSimpleName());
		result = new SimpleGrantedAuthority(authority);

		return result;
	}

	@Transient
	public String getAuthorityName() {
		String result;

		result = this.getClass().getSimpleName();

		return result;
	}

	@Transient
	public DefaultUserIdentity getIdentity() {
		DefaultUserIdentity result;

		assert this.userAccount.getIdentity() instanceof DefaultUserIdentity;
		result = this.userAccount.getIdentity();

		return result;
	}

	@Transient
	public boolean isVirtual() {
		boolean result;

		result = this.getId() == 0;

		return result;
	}

	// Relationships ----------------------------------------------------------


	@NotNull
	@Valid
	@ManyToOne()
	private UserAccount userAccount;

}
