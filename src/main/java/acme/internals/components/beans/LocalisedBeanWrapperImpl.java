/*
 * LocalisedBeanWrapperImpl.java
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

import java.beans.PropertyChangeEvent;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.TypeDescriptor;

import acme.client.helpers.ConversionHelper;
import acme.client.helpers.StringHelper;

public class LocalisedBeanWrapperImpl extends BeanWrapperImpl {

	// Constructors -----------------------------------------------------------

	public LocalisedBeanWrapperImpl() {
		super(true);
	}

	public LocalisedBeanWrapperImpl(final boolean registerDefaultEditors) {
		super(registerDefaultEditors);
	}

	public LocalisedBeanWrapperImpl(final Object target) {
		super(target);

		assert target != null;
	}

	public LocalisedBeanWrapperImpl(final Class<?> clazz) {
		super(clazz);

		assert clazz != null;
	}

	public LocalisedBeanWrapperImpl(final Object target, final String path, final Object root) {
		super(target, path, root);

		assert target != null;
		assert !StringHelper.isBlank(path);
		assert root != null;
	}

	// BeanWrapperImpl interface ----------------------------------------------

	@Override
	protected Object convertForProperty(final String propertyName, final Object oldValue, final Object newValue, final TypeDescriptor type) throws TypeMismatchException {
		assert !StringHelper.isBlank(propertyName);
		// HINT: oldValue can be null
		// HINT: newValue can be null
		assert type != null;

		Object result;
		PropertyChangeEvent event;

		if (ConversionHelper.canConvert(newValue, type.getObjectType()))
			result = ConversionHelper.convert(newValue, type.getObjectType());
		else {
			event = new PropertyChangeEvent(this.getRootInstance(), this.getNestedPath() + propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(event, type.getType(), new Throwable("oops"));
		}

		return result;
	}

}
