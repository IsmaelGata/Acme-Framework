/*
 * ConsoleStream.java
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

import java.io.IOException;
import java.io.OutputStream;

public class ConsoleStream extends OutputStream {

	// Constructors -----------------------------------------------------------

	public ConsoleStream(final OutputStream originalStream) {
		assert originalStream != null;

		this.target = originalStream;
	}

	// Internal state ---------------------------------------------------------


	private OutputStream		target;
	private static OutputStream	lastStream;

	// OutputStream interface -------------------------------------------------


	@Override
	public void close() throws IOException {
		this.target.close();
	}

	@Override
	public void flush() throws IOException {
		this.target.flush();
	}

	@Override
	public void write(final byte[] buffer) throws IOException {
		assert buffer != null && buffer.length >= 1;

		this.swap();
		this.target.write(buffer);
	}

	@Override
	public void write(final byte[] buffer, final int offset, final int length) throws IOException {
		assert buffer != null && buffer.length >= 1;
		assert offset >= 0 && offset < buffer.length;
		assert offset + length - 1 < buffer.length;

		this.swap();
		this.target.write(buffer, offset, length);
	}

	@Override
	public void write(final int datum) throws IOException {
		this.swap();
		this.target.write(datum);
	}

	// Ancillary methods ------------------------------------------------------

	protected void swap() throws IOException {
		if (ConsoleStream.lastStream != this && ConsoleStream.lastStream != null) {
			ConsoleStream.lastStream.flush();
			try {
				Thread.sleep(250);
			} catch (final InterruptedException oops) {
				;
			}
		}
		ConsoleStream.lastStream = this;
	}
}
