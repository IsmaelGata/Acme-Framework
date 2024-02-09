/*
 * EclipseConsoleRunner.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.console;

import java.io.PrintStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import acme.internals.components.exceptions.PassThroughException;

@Component
@Order(0)
public class EclipseConsoleRunner implements CommandLineRunner {

	// Constructors -----------------------------------------------------------

	protected EclipseConsoleRunner() {
	}

	// Internal state ---------------------------------------------------------


	private static boolean isFixed = false;

	// CommandLineRunner interface --------------------------------------------


	@Override
	public void run(final String... strings) {
		ConsoleStream out, err;

		try {
			// HINT: this introduces a short delay into the 'System.err' or 'System.out' OutputStreams
			// HINT+ every time the output switches from one to the other. This is enough to prevent
			// HINT+ the Eclipse console from showing the output of the two streams out of order.

			if (!EclipseConsoleRunner.isFixed) {
				EclipseConsoleRunner.isFixed = true;
				out = new ConsoleStream(System.out);
				err = new ConsoleStream(System.err);
				System.setOut(new PrintStream(out, true, "utf-8"));
				System.setErr(new PrintStream(err, true, "utf-8"));
			}
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}
	}

}
