/*
 * ControllerConfiguration.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Configuration
@ControllerAdvice
public class ControllerConfiguration {

	// Constructor ------------------------------------------------------------

	protected ControllerConfiguration() {
	}

	// Handlers ---------------------------------------------------------------

	@ExceptionHandler(Throwable.class)
	public ModelAndView handleException(final Throwable oops) {
		assert oops != null;

		ModelAndView result;

		result = new ModelAndView();
		result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		result.setViewName("master/panic");
		result.addObject("_oops", oops);

		return result;
	}

}
