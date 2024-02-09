/*
 * LocalisedBeanPropertyBindingResult.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.beans;

import org.springframework.beans.BeanWrapper;
import org.springframework.validation.BeanPropertyBindingResult;

import acme.client.helpers.StringHelper;

public class LocalisedBeanPropertyBindingResult extends BeanPropertyBindingResult {

	// Serialisation ----------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// Constructors -----------------------------------------------------------


	public LocalisedBeanPropertyBindingResult(final Object target, final String objectName) {
		super(target, objectName);

		assert target != null;
		assert !StringHelper.isBlank(objectName);
	}

	public LocalisedBeanPropertyBindingResult(final Object target, final String objectName, final boolean autoGrowNestedPaths, final int autoGrowCollectionLimit) {
		super(target, objectName, autoGrowNestedPaths, autoGrowCollectionLimit);

		assert target != null;
		assert objectName != null;
		assert autoGrowCollectionLimit >= 0;
	}

	//  BeanPropertyBindingResult interface -----------------------------------

	@Override
	protected BeanWrapper createBeanWrapper() {
		assert super.getTarget() != null;

		BeanWrapper result;

		result = new LocalisedBeanWrapperImpl(super.getTarget());

		return result;
	}

}
