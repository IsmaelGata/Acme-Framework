/*
 * PasswordHelper.java
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

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;

import acme.client.helpers.StringHelper;

public abstract class PasswordHelper {

	// Constructors -----------------------------------------------------------

	protected PasswordHelper() {
	}

	// Internal state ---------------------------------------------------------


	private static Pattern pattern;

	static {
		PasswordHelper.pattern = Pattern.compile("^\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}$");
	}

	// Business methods -------------------------------------------------------


	public static String encode(final String password) {
		assert !StringHelper.isBlank(password);

		String result;
		PasswordEncoder encoder;

		encoder = FactoryHelper.getPasswordEncoder();

		if (password == null || PasswordHelper.isEncoded(password))
			result = password;
		else
			result = encoder.encode(password);

		return result;
	}

	public static boolean isEncoded(final String password) {
		assert !StringHelper.isBlank(password);

		boolean result;

		result = PasswordHelper.pattern.matcher(password).matches();

		return result;
	}

	public static boolean checkConfirmation(final String password, final String confirmation) {
		assert !StringHelper.isBlank(password);
		assert !StringHelper.isBlank(confirmation);

		boolean result;
		boolean isPasswordEncoded, isConfirmationEncoded;
		PasswordEncoder encoder;

		isPasswordEncoded = PasswordHelper.isEncoded(password);
		isConfirmationEncoded = PasswordHelper.isEncoded(confirmation);

		if (isPasswordEncoded == isConfirmationEncoded)
			result = password.equals(confirmation);
		else {
			assert isPasswordEncoded && !isConfirmationEncoded;
			encoder = FactoryHelper.getPasswordEncoder();
			result = encoder.matches(confirmation, password);
		}

		return result;
	}

}
