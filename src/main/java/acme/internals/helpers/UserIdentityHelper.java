/*
 * UserIdentityHelper.java
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
import java.util.Collections;
import java.util.List;

import acme.client.data.accounts.DefaultUserIdentity;
import acme.client.helpers.CollectionHelper;
import acme.client.helpers.StringHelper;

public abstract class UserIdentityHelper {

	// Constructors -----------------------------------------------------------

	protected UserIdentityHelper() {
	}

	// Business methods -------------------------------------------------------

	public static Class<DefaultUserIdentity> findIdentityClazz() {
		Class<DefaultUserIdentity> result;
		String identityClazz;

		identityClazz = EnvironmentHelper.getProperty("acme.identity.extended-class", (String) null);
		if (identityClazz == null)
			identityClazz = EnvironmentHelper.getRequiredProperty("acme.identity.default-class");
		assert ReflectionHelper.existsClass(identityClazz, DefaultUserIdentity.class) : String.format("Cannot find class '%s'.", identityClazz);
		result = ReflectionHelper.getClass(identityClazz, DefaultUserIdentity.class);

		return result;
	}

	public static DefaultUserIdentity createBlankIdentity() {
		DefaultUserIdentity result;
		Class<DefaultUserIdentity> identityClazz;

		identityClazz = UserIdentityHelper.findIdentityClazz();
		result = ReflectionHelper.instantiate(identityClazz);

		return result;

	}

	public static String[] computeProperties(final String... baseProperties) {
		assert !CollectionHelper.someNull(baseProperties);

		List<String> result;

		result = new ArrayList<String>();
		Collections.addAll(result, baseProperties);

		UserIdentityHelper.doComputeProperties(result, "acme.identity.default-attributes", true);
		UserIdentityHelper.doComputeProperties(result, "acme.identity.extended-attributes", false);

		return result.toArray(new String[0]);
	}

	// Ancillary methods ------------------------------------------------------

	private static void doComputeProperties(final List<String> partial, final String key, final boolean required) {
		assert !CollectionHelper.someNull(partial);
		assert !StringHelper.isBlank(key);

		String specification;
		String[] attributes;

		specification = EnvironmentHelper.getProperty(key, (String) null);
		assert !required || specification != null : String.format("Could not find property '%s' in your enviroment.", key);
		if (specification != null) {
			attributes = StringHelper.splitChoices(specification);
			for (final String identityAttribute : attributes)
				partial.add(String.format("identity.%s", identityAttribute));
		}
	}

}
