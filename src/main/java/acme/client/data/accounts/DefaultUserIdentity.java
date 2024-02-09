/*
 * DefaultUserIdentity.java
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

import java.beans.Transient;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import acme.client.data.AbstractDatatype;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DefaultUserIdentity extends AbstractDatatype {

	// HINT: this datatype is not @Embeddable since this would prevent user 
	// HINT+ identities from being extended in user projects.  Neither can it be 
	// HINT+ abstract because it needs to be serialised and deserialised.

	// Serialisation identifier -----------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@NotBlank
	private String				name;

	@NotBlank
	private String				surname;

	@NotBlank
	@Email
	private String				email;

	// Derived attributes -----------------------------------------------------


	@Transient
	public String getFullName() {
		StringBuilder result;

		result = new StringBuilder();
		result.append(this.surname);
		result.append(", ");
		result.append(this.name);

		return result.toString();
	}

}
