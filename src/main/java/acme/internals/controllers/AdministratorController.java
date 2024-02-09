/*
 * AdministratorController.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.controllers;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import acme.Launcher;
import acme.client.data.accounts.Administrator;
import acme.client.helpers.Assert;
import acme.client.helpers.PrincipalHelper;
import acme.internals.helpers.FactoryHelper;

@Controller
public class AdministratorController {

	// Endpoints --------------------------------------------------------------

	@GetMapping("/administrator/system/populate-initial")
	public ModelAndView populateInitial() {
		Assert.state(PrincipalHelper.get().hasRole(Administrator.class), "default.error.not-authorised");

		ModelAndView result;

		result = this.doPopulate(false);

		return result;
	}

	@GetMapping("/administrator/system/populate-sample")
	public ModelAndView populateSample() {
		Assert.state(PrincipalHelper.get().hasRole(Administrator.class), "default.error.not-authorised");

		ModelAndView result;

		result = this.doPopulate(true);

		return result;
	}

	@GetMapping("/administrator/system/shut-down")
	public void shutDown() {
		Assert.state(PrincipalHelper.get().hasRole(Administrator.class), "default.error.not-authorised");

		ConfigurableApplicationContext context;

		context = FactoryHelper.getContext();
		Launcher.exit(context);
	}

	// Ancillary methods ------------------------------------------------------

	protected ModelAndView doPopulate(final boolean sampleData) {
		Assert.state(PrincipalHelper.get().hasRole(Administrator.class), "default.error.not-authorised");

		ModelAndView result;

		try {
			Launcher.reset(sampleData, sampleData);
			PrincipalHelper.handleUpdate();
			result = new ModelAndView();
			result.setViewName("fragments/welcome");
			result.addObject("_globalSuccessMessage", "default.global.message.success");
		} catch (final Throwable oops) {
			result = new ModelAndView();
			result.setViewName("master/panic");
			result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			result.addObject("_globalErrorMessage", "default.global.message.error");
			result.addObject("_oops", oops);
		}

		return result;
	}

}
