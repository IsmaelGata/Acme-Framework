/*
 * SelectChoice.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.views;

import acme.client.helpers.StringHelper;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {
	"key", "label"
})
public class SelectChoice {

	// Internal state ---------------------------------------------------------

	private String	key;
	private String	label;
	private boolean	selected;
	private boolean	sealed;

	// Constructor ------------------------------------------------------------


	public SelectChoice() {
		this.key = "*";
		this.label = "*";
		this.selected = false;
		this.sealed = false;
	}

	// Properties -------------------------------------------------------------

	public String getKey() {
		return this.key;
	}

	public void setKey(final String key) {
		assert key == null || !StringHelper.isBlank(key);
		assert !this.isSealed();

		this.key = key;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(final String label) {
		assert !StringHelper.isBlank(label);
		assert !this.isSealed();

		this.label = label;
	}

	public boolean isSelected() {
		return this.selected;
	}

	public void setSelected(final boolean selected) {
		assert !this.isSealed();

		this.selected = selected;
	}

	public boolean isSealed() {
		return this.sealed;
	}

	public void setSealed(final boolean sealed) {
		assert !this.isSealed();

		this.sealed = sealed;
	}

}
