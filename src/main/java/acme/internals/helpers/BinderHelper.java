/*
 * BinderHelper.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.validation.BindingResult;

import acme.client.data.AbstractEntity;
import acme.client.data.models.Buffer;
import acme.client.data.models.Dataset;
import acme.client.data.models.Errors;
import acme.client.data.models.Request;
import acme.client.helpers.StringHelper;

public abstract class BinderHelper {

	// Constructors -----------------------------------------------------------

	protected BinderHelper() {
	}

	// Business methods -------------------------------------------------------

	public static void bind(final Object target, final Request request, final Buffer buffer, final String... properties) {
		assert target != null;
		assert request != null;
		assert buffer != null;
		assert !StringHelper.someBlank(properties);

		Dataset rawDataset, filteredDataset;
		Errors errors;
		List<String> inclusions;
		BindingResult bindingResult;

		rawDataset = request.getData();
		errors = buffer.getGlobal("$errors", Errors.class);
		inclusions = BinderHelper.buildInclusions(target, properties);
		filteredDataset = BinderHelper.buildDataset(rawDataset, inclusions);
		bindingResult = ReflectionHelper.bind(target, filteredDataset);
		ErrorsHelper.transferErrors(bindingResult, errors);
	}

	public static Dataset unbind(final Object target, final String... properties) {
		assert target != null;
		assert !StringHelper.someBlank(properties);

		Dataset result;
		List<String> inclusions;

		inclusions = BinderHelper.buildInclusions(target, properties);
		result = ReflectionHelper.unbind(target, inclusions);

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	private static ArrayList<String> buildInclusions(final Object object, final String... properties) {
		assert object != null;
		assert !StringHelper.someBlank(properties);

		ArrayList<String> result;

		result = new ArrayList<String>(Arrays.asList(properties));
		if (object instanceof AbstractEntity) {
			if (!result.contains("id"))
				result.add("id");
			if (!result.contains("version"))
				result.add("version");
		}

		return result;
	}

	private static Dataset buildDataset(final Dataset dataset, final List<String> properties) {
		assert dataset != null;
		assert !StringHelper.someBlank(properties);

		Dataset result;

		result = new Dataset();
		for (final String property : properties) {
			Object object;

			object = dataset.get(property);
			result.put(property, object);
		}

		return result;
	}

}
