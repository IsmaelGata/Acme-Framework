/*
 * SelectChoices.java
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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import acme.client.data.AbstractEntity;
import acme.client.helpers.CollectionHelper;
import acme.client.helpers.ConversionHelper;
import acme.client.helpers.StringHelper;
import acme.internals.helpers.ReflectionHelper;

public class SelectChoices implements Iterable<SelectChoice> {

	// Internal state ---------------------------------------------------------

	private Map<String, SelectChoice>	keyMap;
	private Map<String, SelectChoice>	labelMap;
	private SelectChoice				selected;

	// Constructors -----------------------------------------------------------


	public SelectChoices() {
		this.keyMap = new LinkedHashMap<String, SelectChoice>();
		this.labelMap = new LinkedHashMap<String, SelectChoice>();
		this.selected = null;
	}

	public static <E extends Enum<?>> SelectChoices from(final Class<E> source, final E selected) {
		assert source != null;
		// HINT: selected can be null

		SelectChoices result;
		Collection<E> choices;
		Iterator<E> iterator;

		result = new SelectChoices();
		choices = CollectionHelper.toCollection(source);
		result.add("0", "----", selected == null);
		iterator = choices.iterator();
		while (iterator.hasNext()) {
			Object choice;
			String key, label;

			choice = iterator.next();
			key = choice.toString();
			label = choice.toString();
			result.add(key, label, choice.equals(selected));
		}
		assert result.getSelected() != null : "There is no selected choice in source.";

		return result;
	}

	public static <E extends AbstractEntity> SelectChoices from(final Collection<E> source, final String property, final E selected) {
		assert !CollectionHelper.someNull(source);
		assert !StringHelper.isBlank(property);
		// HINT: selected can be null

		SelectChoices result;
		Iterator<E> iterator;

		result = new SelectChoices();
		result.add("0", "----", selected == null);
		iterator = source.iterator();
		while (iterator.hasNext()) {
			E choice;
			String key;
			Object value;
			String label;

			choice = iterator.next();
			key = Integer.toString(choice.getId());
			assert ReflectionHelper.hasProperty(choice, property) : String.format("Object '%s' does not have property '%s'.", choice, property);
			value = ReflectionHelper.getProperty(choice, property);
			assert ConversionHelper.canConvert(value, String.class) : String.format("Cannot convert property '%s' to a string.", property);
			label = ConversionHelper.convert(value, String.class);
			result.add(key, label, choice.equals(selected));
		}
		assert result.getSelected() != null : "There is no selected choice in source.";

		return result;
	}

	public static SelectChoices from(final SelectChoice[] choices) {
		final SelectChoices result;

		result = new SelectChoices();
		for (int i = 0; i < choices.length; i++)
			result.add(choices[i]);
		assert result.getSelected() != null : "There is no selected choice in source.";

		return result;
	}

	// Properties -------------------------------------------------------------

	public boolean hasChoiceWithKey(final String key) {
		assert key != null;

		boolean result;

		result = this.keyMap.containsKey(key);

		return result;
	}

	public boolean hasChoiceWithLabel(final String label) {
		assert !StringHelper.isBlank(label);

		boolean result;

		result = this.labelMap.containsKey(label);

		return result;
	}

	public SelectChoice getSelected() {
		assert this.selected.isSelected();

		return this.selected;
	}

	// Business methods -------------------------------------------------------

	public void add(final String key, final String label, final boolean selected) {
		assert (key == null || !StringHelper.isBlank(key)) && !this.hasChoiceWithKey(key);
		assert !StringHelper.isBlank(label);
		assert !selected || this.selected == null;

		SelectChoice choice;

		choice = new SelectChoice();
		choice.setKey(key);
		choice.setLabel(label);
		choice.setSelected(selected);
		choice.setSealed(true);

		this.keyMap.put(key, choice);
		if (selected)
			this.selected = choice;
	}

	public void add(final SelectChoice choice) {
		assert choice != null;
		assert (choice.getKey() == null || !StringHelper.isBlank(choice.getKey())) && !this.hasChoiceWithKey(choice.getKey());
		assert !StringHelper.isBlank(choice.getLabel());
		assert !choice.isSelected() || this.selected == null;

		if (!choice.isSealed())
			choice.setSealed(true);

		this.keyMap.put(choice.getKey(), choice);
		if (choice.isSelected())
			this.selected = choice;
	}

	// Iterable interface -----------------------------------------------------

	@Override
	public Iterator<SelectChoice> iterator() {
		Iterator<SelectChoice> result;

		result = this.keyMap.values().iterator();

		return result;
	}

}
