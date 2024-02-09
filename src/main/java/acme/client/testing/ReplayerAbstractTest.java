/*
 * ReplayerAbstractTest.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.testing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import acme.Launcher;

public abstract class ReplayerAbstractTest extends AbstractTest {

	// Set-up methods ---------------------------------------------------------

	@BeforeAll
	@Override
	public void beforeAllTests(final TestInfo context) {
		assert context != null;

		if (!super.isInitialised())
			Launcher.main("--platform", "testing", "--launcher", "tester");

		super.beforeAllTests(context);
	}

}
