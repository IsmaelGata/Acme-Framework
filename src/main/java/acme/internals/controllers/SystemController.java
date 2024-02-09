/*
 * SystemController.java
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SystemController {

	// Endpoints --------------------------------------------------------------

	@GetMapping("/")
	public ModelAndView index() {
		ModelAndView result;

		result = new ModelAndView();
		result.setViewName("redirect:/any/system/welcome");

		return result;
	}

	@GetMapping("/any/system/welcome")
	public ModelAndView welcome() {
		ModelAndView result;

		result = new ModelAndView();
		result.setViewName("fragments/welcome");

		return result;
	}

	@GetMapping("/anonymous/system/sign-in")
	public ModelAndView signIn(@RequestParam final Map<String, String> params) {
		assert params != null;

		ModelAndView result;

		result = new ModelAndView();
		result.setViewName("master/sign-in");
		result.addObject("username", "");
		result.addObject("password", "");
		result.addObject("remember", false);

		return result;
	}

	// HINT: Note that there is no post /anonymous/system/sign-in end point or 
	// HINT+ get/post /authenticated/system/sign-out end points because the sign-in and 
	// HINT+ sign-out processes are controlled by Spring.

	@GetMapping("/any/system/company")
	public ModelAndView company() {
		final ModelAndView result;

		result = new ModelAndView();
		result.setViewName("fragments/company");

		return result;
	}

	@GetMapping("/any/system/license")
	public ModelAndView license() {
		final ModelAndView result;

		result = new ModelAndView();
		result.setViewName("fragments/license");

		return result;
	}

	@GetMapping("/any/system/panic")
	public ModelAndView panic(final HttpServletRequest request, final HttpServletResponse response) {
		assert request != null;
		assert response != null;

		ModelAndView result;

		result = new ModelAndView();
		result.setViewName("master/panic");
		result.setStatus(HttpStatus.valueOf(response.getStatus()));

		return result;
	}

	@GetMapping("/any/system/oops")
	public ModelAndView oops() {
		throw new RuntimeException("This is a test exception!");
	}

	@GetMapping("/any/system/referrer")
	public ModelAndView referrer() {
		ModelAndView result;

		result = new ModelAndView();
		result.setViewName("master/referrer");

		return result;
	}

}
