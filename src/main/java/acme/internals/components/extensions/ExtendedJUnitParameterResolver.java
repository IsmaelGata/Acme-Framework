/*
 * ExtendedJUnitParameterResolver.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.extensions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class ExtendedJUnitParameterResolver implements ParameterResolver {

	// ParameterResolver interface --------------------------------------------

	@Override
	public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
		assert parameterContext != null;
		assert extensionContext != null;

		boolean result;

		result = parameterContext.getParameter().getType().equals(ExtensionContext.class);

		return result;
	}

	@Override
	public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
		assert parameterContext != null;
		assert extensionContext != null;

		return extensionContext;
	}

}
