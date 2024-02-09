/*
 * AdministratorUserAccountListService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.features.administrator.userAccount;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.accounts.Administrator;
import acme.client.data.accounts.UserAccount;
import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;

@Service
public class AdministratorUserAccountListService extends AbstractService<Administrator, UserAccount> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AdministratorUserAccountRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Collection<UserAccount> userAccounts;

		userAccounts = this.repository.findAllUserAccounts();
		super.getBuffer().addData(userAccounts);
	}

	@Override
	public void unbind(final UserAccount object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbind(object, "username", "identity.name", "identity.surname", "identity.email");
		dataset.put("roleList", object.getAuthorityString());
		dataset.put("status", object.isEnabled());

		super.getResponse().addData(dataset);
	}

}
