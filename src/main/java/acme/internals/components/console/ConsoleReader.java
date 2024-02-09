/*
 * ConsoleReader.java
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.PassThroughException;

public class ConsoleReader {

	// Internal state ---------------------------------------------------------

	private InputStreamReader	stream;
	private BufferedReader		reader;

	// Constructors -----------------------------------------------------------


	public ConsoleReader() {
		this.stream = new InputStreamReader(System.in);
		this.reader = new BufferedReader(this.stream);
	}

	// Finalisers -------------------------------------------------------------

	@Override
	protected void finalize() {
		try {
			if (this.reader != null)
				this.reader.close();  // HINT: this implicitly closes the stream reader.
		} catch (Throwable oops) {
			;  // HINT: silently ignore exceptions here to prevent object revival.
		}

	}

	// Business methods -------------------------------------------------------

	public String readCommand() {
		String result;
		StringBuilder buffer;
		String line;
		String prompt;
		boolean done;

		do {
			prompt = "> ";
			buffer = new StringBuilder();
			done = false;
			do {
				line = this.readLine(prompt);
				if (line == null)
					line = "exit;";
				line = line.trim();
				if (line.endsWith(";")) {
					done = true;
					line = line.substring(0, line.length() - 1);
				}
				buffer.append(line);
				buffer.append(' ');
				prompt = "\t> ";
			} while (!done);
			result = StringUtils.trim(buffer.toString());
		} while (result.isEmpty());

		return result;
	}

	public String readLine(final String prompt) {
		assert !StringHelper.isBlank(prompt);

		String result;

		do {
			System.out.printf(prompt); // NOSONAR
			try {
				result = this.reader.readLine();
				result = result != null ? result.trim() : "";
			} catch (final Throwable oops) {
				throw new PassThroughException(oops);
			}
		} while (StringHelper.isBlank(result));

		return result;
	}

}
