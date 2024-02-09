/*
 * ErrorsHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.helpers;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import acme.client.data.AbstractEntity;
import acme.client.data.models.Errors;
import acme.client.helpers.CollectionHelper;

public abstract class ErrorsHelper {

	// Constructors -----------------------------------------------------------

	protected ErrorsHelper() {
	}

	// Business methods -------------------------------------------------------

	public static void transferErrors(final BindingResult bindingResult, final Errors errors) {
		assert bindingResult != null;
		assert errors != null;

		String attributeName, message;

		for (final ObjectError error : bindingResult.getGlobalErrors()) {
			attributeName = "*";
			message = error.getDefaultMessage();
			errors.add(attributeName, message);
		}

		for (final FieldError error : bindingResult.getFieldErrors()) {
			attributeName = error.getField();
			message = error.getDefaultMessage();
			errors.add(attributeName, message);
		}
	}

	public static void transferErrors(final Set<ConstraintViolation<AbstractEntity>> violations, final Errors errors) {
		assert !CollectionHelper.someNull(violations);
		assert errors != null;

		String property, message;

		for (final ConstraintViolation<AbstractEntity> violation : violations) {
			property = violation.getPropertyPath().toString();
			message = violation.getMessage();
			errors.add(property, message);
		}
	}

}
