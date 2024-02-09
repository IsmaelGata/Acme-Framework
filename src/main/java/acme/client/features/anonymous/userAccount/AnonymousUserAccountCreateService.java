/*
 * AnonymousUserAccountCreateService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.features.anonymous.userAccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.AbstractRole;
import acme.client.data.accounts.Anonymous;
import acme.client.data.accounts.Authenticated;
import acme.client.data.accounts.DefaultUserIdentity;
import acme.client.data.accounts.UserAccount;
import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;
import acme.internals.helpers.UserIdentityHelper;

@Service
public class AnonymousUserAccountCreateService extends AbstractService<Anonymous, UserAccount> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AnonymousUserAccountRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		UserAccount userAccount;
		DefaultUserIdentity identity;
		Authenticated authenticatedRole;

		userAccount = new UserAccount();
		userAccount.setEnabled(true);
		identity = UserIdentityHelper.createBlankIdentity();
		userAccount.setIdentity(identity);
		authenticatedRole = new Authenticated();
		userAccount.addRole(authenticatedRole);
		authenticatedRole.setUserAccount(userAccount);

		super.getBuffer().addData(userAccount);
	}

	@Override
	public void bind(final UserAccount object) {
		String[] properties;

		properties = UserIdentityHelper.computeProperties("username", "password");
		super.bind(object, properties);
	}

	@Override
	public void validate(final UserAccount object) {
		assert object != null;

		boolean isDuplicated, isAccepted, isMatching;
		String password, confirmation;
		int passwordLength;

		isDuplicated = this.repository.findOneUserAccountByUsername(object.getUsername()) != null;
		super.state(!isDuplicated, "username", "anonymous.user-account.form.error.duplicated");

		passwordLength = super.getRequest().getData("password", String.class).length();
		super.state(passwordLength >= 5 && passwordLength <= 60, "password", "acme.validation.length", 6, 60);

		isAccepted = this.getRequest().getData("accept", boolean.class);
		super.state(isAccepted, "accept", "anonymous.user-account.form.error.must-accept");

		password = this.getRequest().getData("password", String.class);
		confirmation = this.getRequest().getData("confirmation", String.class);
		isMatching = password.equals(confirmation);
		super.state(isMatching, "confirmation", "anonymous.user-account.form.error.confirmation-no-match");
	}

	@Override
	public void perform(final UserAccount object) {
		assert object != null;

		this.repository.save(object);
		for (final AbstractRole role : object.getUserRoles())
			if (!role.isVirtual())
				this.repository.save(role);
	}

	@Override
	public void unbind(final UserAccount object) {
		assert object != null;

		Dataset dataset;
		String[] properties;

		properties = UserIdentityHelper.computeProperties("username");
		dataset = super.unbind(object, properties);

		if (super.getRequest().getMethod().equals("POST")) {
			dataset.put("password", super.getRequest().getData("password", String.class));
			dataset.put("confirmation", super.getRequest().getData("confirmation", String.class));
			dataset.put("accept", super.getRequest().getData("accept", boolean.class));
		} else {
			dataset.put("password", "");
			dataset.put("confirmation", "");
			dataset.put("accept", "false");
		}

		super.getResponse().addData(dataset);
	}

}
