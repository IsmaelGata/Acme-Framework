/*
 * AuthenticatedUserAccountUpdateService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.features.authenticated.userAccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.accounts.Authenticated;
import acme.client.data.accounts.UserAccount;
import acme.client.data.models.Dataset;
import acme.client.helpers.PrincipalHelper;
import acme.client.services.AbstractService;
import acme.internals.helpers.UserIdentityHelper;

@Service
public class AuthenticatedUserAccountUpdateService extends AbstractService<Authenticated, UserAccount> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuthenticatedUserAccountRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		UserAccount userAccount;
		int principalId;

		principalId = super.getRequest().getPrincipal().getAccountId();
		userAccount = this.repository.findOneUserAccountById(principalId);
		// HINT: the following instruction forces the roles to be loaded.
		userAccount.getUserRoles().size(); // NOSONAR

		super.getBuffer().addData(userAccount);
	}

	@Override
	public void bind(final UserAccount object) {
		assert object != null;

		String password;
		String[] properties;

		properties = UserIdentityHelper.computeProperties();
		super.bind(object, properties);
		password = this.getRequest().getData("password", String.class);
		if (!password.equals("[MASKED-PASWORD]"))
			object.setPassword(password);
	}

	@Override
	public void validate(final UserAccount object) {
		assert object != null;

		int passwordLength;
		String password, confirmation;
		boolean isMatching;

		passwordLength = super.getRequest().getData("password", String.class).length();
		super.state(passwordLength >= 5 && passwordLength <= 60, "password", "acme.validation.length", 6, 60);

		password = super.getRequest().getData("password", String.class);
		confirmation = super.getRequest().getData("confirmation", String.class);
		isMatching = password.equals(confirmation);
		super.state(isMatching, "confirmation", "authenticated.user-account.form.error.confirmation-no-match");
	}

	@Override
	public void perform(final UserAccount object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final UserAccount object) {
		assert object != null;

		Dataset dataset;
		String[] properties;

		properties = UserIdentityHelper.computeProperties("username");
		dataset = super.unbind(object, properties);

		if (super.getRequest().getMethod().equals("GET")) {
			dataset.put("password", "[MASKED-PASWORD]");
			dataset.put("confirmation", "[MASKED-PASWORD]");
		} else {
			dataset.put("password", super.getRequest().getData("password", String.class));
			dataset.put("confirmation", super.getRequest().getData("confirmation", String.class));
		}

		super.getResponse().addData(dataset);
	}

	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}

}
