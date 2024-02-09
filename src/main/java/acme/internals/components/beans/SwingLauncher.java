/*
 * SwingLauncher.java
 *
 * Copyright (C) 2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.beans;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

public class SwingLauncher extends WindowAdapter {

	// Internal state ---------------------------------------------------------

	private JFrame		frame;
	private Semaphore	beacon;

	// Constructors -----------------------------------------------------------


	public SwingLauncher(final JFrame frame) {
		assert frame != null && !frame.isVisible();

		this.frame = frame;
		this.beacon = new Semaphore(1);
	}

	// Interface WindowAdapter ------------------------------------------------

	@Override
	public void windowClosing(final WindowEvent event) {
		assert event != null;

		this.beacon.release();
	}

	// Business methods -------------------------------------------------------

	public void run() {
		assert !this.frame.isVisible();

		try {
			this.frame.addWindowListener(this);
			this.beacon.acquire();
			this.frame.setVisible(true);
			this.beacon.acquire();
			this.beacon.release();
			this.frame.removeWindowListener(this);
		} catch (InterruptedException oops) {
			;
		} finally {
			if (this.beacon.availablePermits() == 0)
				this.beacon.release();
		}
	}

};
