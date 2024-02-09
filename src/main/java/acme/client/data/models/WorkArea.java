/*
 * WorkArea.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.data.models;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import acme.client.helpers.CollectionHelper;
import acme.client.helpers.ConversionHelper;
import acme.client.helpers.PrinterHelper;
import acme.client.helpers.StringHelper;

public abstract class WorkArea {

	// Internal state ---------------------------------------------------------

	private Map<String, Dataset> map;

	// Constructors -----------------------------------------------------------


	protected WorkArea() {
		this.map = new LinkedHashMap<String, Dataset>();

		Dataset global, data;

		global = new Dataset();
		this.map.put("$globals", global);

		data = new Dataset();
		this.map.put("$data", data);
	}

	// Global properties ------------------------------------------------------

	public Dataset getGlobals() {
		Dataset result;

		result = this.map.get("$globals");

		return result;
	}

	public boolean hasGlobal(final String key) {
		assert !StringHelper.isBlank(key);

		boolean result;

		result = this.map.get("$globals").containsKey(key);

		return result;
	}

	public boolean hasGlobal(final String key, final Class<?> clazz) {
		assert !StringHelper.isBlank(key);
		assert clazz != null;

		boolean result;
		Object datum;

		result = this.map.get("$globals").containsKey(key);
		if (result) {
			datum = this.map.get("$globals").get(key);
			result = ConversionHelper.canConvert(datum, clazz);
		}

		return result;
	}

	public Object getGlobal(final String key) {
		assert !StringHelper.isBlank(key) && this.hasGlobal(key);

		Object result;

		result = this.map.get("$globals").get(key);

		return result;
	}

	public <T> T getGlobal(final String key, final Class<T> clazz) {
		assert !StringHelper.isBlank(key) && this.hasGlobal(key, clazz);
		assert clazz != null;

		T result;
		Object object;

		object = this.map.get("$globals").get(key);
		result = ConversionHelper.convert(object, clazz);

		return result;
	}

	public void addGlobal(final String key, final Object object) {
		assert !StringHelper.isBlank(key);
		// HINT: object can be null

		Dataset target;

		target = this.map.get("$globals");
		target.put(key, object);
	}

	// Data properties --------------------------------------------------------

	public Dataset getData() {
		Dataset result;

		result = this.map.get("$data");

		return result;
	}

	public boolean hasData(final String key) {
		assert !StringHelper.isBlank(key);

		boolean result;

		result = this.map.get("$data").containsKey(key);

		return result;
	}

	public boolean hasData(final String key, final Class<?> clazz) {
		assert !StringHelper.isBlank(key);
		assert clazz != null;

		boolean result;
		Object datum;

		result = this.map.get("$data").containsKey(key);
		if (result) {
			datum = this.map.get("$data").get(key);
			result = ConversionHelper.canConvert(datum, clazz);
		}

		return result;
	}

	public Object getData(final String key) {
		assert !StringHelper.isBlank(key) && this.hasData(key);

		Object result;

		result = this.map.get("$globals").get(key);

		return result;
	}

	public <T> T getData(final String key, final Class<T> clazz) {
		assert !StringHelper.isBlank(key) && this.hasData(key, clazz);
		assert clazz != null;

		T result;
		Object object;

		object = this.map.get("$data").get(key);
		result = ConversionHelper.convert(object, clazz);

		return result;
	}

	public void addData(final Object object) {
		// HINT: object can be null

		Dataset target;
		String key;

		key = StringHelper.toIdentity(object);
		target = this.map.get("$data");
		target.put(key, object);
	}

	public void addData(final String key, final Object object) {
		assert !StringHelper.isBlank(key);
		// HINT: object can be null

		Dataset target;

		target = this.map.get("$data");
		target.put(key, object);
	}

	public void addData(final Collection<?> data) {
		assert data != null;

		for (final Object datum : data)
			this.addData(datum);
	}

	public void addData(final Collection<String> keys, final Collection<Object> objects) {
		assert !CollectionHelper.someNull(keys);
		assert objects != null; // HINT: there can be null data.
		assert keys.size() == objects.size();

		Iterator<String> keysIterator;
		Iterator<?> dataIterator;

		keysIterator = keys.iterator();
		dataIterator = objects.iterator();

		while (keysIterator.hasNext() && dataIterator.hasNext()) {
			String key;
			Object datum;

			key = keysIterator.next();
			datum = dataIterator.next();
			this.addData(key, datum);
		}
		assert !keysIterator.hasNext() && !dataIterator.hasNext();
	}

	// General properties -----------------------------------------------------

	public Collection<Entry<String, Dataset>> getEntries() {
		Collection<Entry<String, Dataset>> result;

		result = this.map.entrySet();

		return result;
	}

	public Collection<Entry<String, Object>> getGlobalEntries() {
		Collection<Entry<String, Object>> result;

		result = this.map.get("$globals").entrySet();

		return result;
	}

	public Collection<Entry<String, Object>> getDataEntries() {
		Collection<Entry<String, Object>> result;

		result = this.map.get("$data").entrySet();

		return result;
	}

	// Object interface -------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder result;

		result = new StringBuilder();
		for (final Entry<String, Dataset> entry : this.getEntries()) {
			result.append("* Dataset ");
			result.append(entry.getKey());
			result.append(":\n");
			result.append(PrinterHelper.printObject(entry.getValue(), true));
			result.append(System.lineSeparator());
		}

		return result.toString();
	}

}
