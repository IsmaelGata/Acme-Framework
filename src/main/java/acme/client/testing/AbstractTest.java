/*
 * AbstractTest.java
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer.OrderAnnotation;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.ActiveProfiles;

import acme.internals.components.extensions.ExtendedJUnitParameterResolver;
import acme.internals.helpers.FactoryHelper;
import acme.internals.helpers.TraceLoggerHelper;
import lombok.CustomLog;

@ActiveProfiles(profiles = "testing")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodName.class)
@TestClassOrder(OrderAnnotation.class)
@ExtendWith(ExtendedJUnitParameterResolver.class)
@CustomLog
public abstract class AbstractTest {

	// Internal state ---------------------------------------------------------

	private static boolean initialised = false;

	// Properties -------------------------------------------------------------


	public boolean isInitialised() {
		return AbstractTest.initialised;
	}

	public void setInitialised() {
		AbstractTest.initialised = true;
	}

	// Set-up method ----------------------------------------------------------

	@BeforeAll
	public void beforeAllTests(final TestInfo context) {
		assert context != null && context.getTestClass().isPresent();

		AbstractTest.logger.debug("Auto-wiring test class.");
		FactoryHelper.autowire(this);

		AbstractTest.logger.info("# BEGIN TEST CLASS \"{}\"", context.getDisplayName());

		TraceLoggerHelper.log("# RESET");
		TraceLoggerHelper.log("# BEGIN TEST CLASS \"{}\"", context.getDisplayName());

		this.setInitialised();
	}

	@AfterAll
	public void afterAllTests(final TestInfo context) {
		assert context != null;

		AbstractTest.logger.info("# END TEST CLASS \"{}\"", context.getDisplayName());

		TraceLoggerHelper.log("# END TEST CLASS \"{}\"", context.getDisplayName());
	}

	@BeforeEach
	public void beforeEachTest(final TestInfo context) {
		assert context != null && context.getTestMethod().isPresent();

		String header;

		header = this.computeDisplayName(context);

		TraceLoggerHelper.log("# BEGIN TEST CASE \"{}\"", header);
	}

	@AfterEach
	public void afterEachTest(final TestInfo context, final ExtensionContext status) {
		assert context != null && context.getTestMethod().isPresent();
		assert status != null;

		String displayName, conclusion;

		displayName = this.computeDisplayName(context);

		AbstractTest.logger.info("> {}", displayName);
		if (status.getExecutionException().isPresent()) {
			conclusion = String.format("OOPS %s", status.getExecutionException().get().getMessage());
			AbstractTest.logger.info("! {}", conclusion);
		}

		TraceLoggerHelper.log("# END TEST CASE \"{}\"", displayName);
	}

	// Ancillary methods ------------------------------------------------------

	protected String computeDisplayName(final TestInfo context) {
		assert context != null && context.getTestMethod().isPresent();

		String result;
		String methodName, displayName;

		methodName = context.getTestMethod().get().getName(); // NOSONAR
		displayName = context.getDisplayName();
		if (displayName.startsWith(methodName))
			result = methodName;
		else
			result = String.format("%s %s", methodName, displayName);

		return result;
	}

}
