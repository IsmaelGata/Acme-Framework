/*
 * CommandManager.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import acme.client.data.AbstractObject;
import acme.client.data.AbstractRole;
import acme.client.helpers.StringHelper;
import acme.client.services.AbstractService;

public class CommandManager<R extends AbstractRole, E extends AbstractObject> {

	// Internal state ---------------------------------------------------------

	private Map<String, AbstractService<R, E>>	supportServices;
	private Map<String, String>					commandHierarchy;

	// Constructors -----------------------------------------------------------


	public CommandManager() {
		this.supportServices = new LinkedHashMap<String, AbstractService<R, E>>();
		this.commandHierarchy = new LinkedHashMap<String, String>();
	}

	// Business methods -------------------------------------------------------

	public boolean isBasic(final String command) {
		assert !StringHelper.isBlank(command);

		boolean result;

		result = StringHelper.anyOf(command, "list|show|create|update|delete|perform");

		return result;
	}

	public boolean isRegistered(final String command) {
		assert !StringHelper.isBlank(command);

		boolean result;

		result = this.supportServices.containsKey(command);

		return result;
	}

	public void addBasicCommand(final String command, final AbstractService<R, E> service) {
		assert !StringHelper.isBlank(command) && !this.isRegistered(command) && this.isBasic(command);
		assert service != null;

		this.supportServices.put(command, service);
		this.commandHierarchy.put(command, command);
	}

	public void addCustomCommand(final String command, final String superCommand, final AbstractService<R, E> service) {
		assert !StringHelper.isBlank(command) && !this.isRegistered(command) && !this.isBasic(command);
		assert !StringHelper.isBlank(superCommand) && this.isBasic(superCommand);
		assert service != null;

		this.supportServices.put(command, service);
		this.commandHierarchy.put(command, superCommand);
	}

	public String getSuperCommand(final String command) {
		assert !StringHelper.isBlank(command) && this.isRegistered(command);

		String result;

		result = this.commandHierarchy.get(command);

		return result;
	}

	public Collection<String> getSubCommands(final String command) {
		assert !StringHelper.isBlank(command) && this.isRegistered(command) && !this.isBasic(command);

		Collection<String> result;
		String key, value;

		result = new ArrayList<String>();
		for (final Entry<String, String> entry : this.commandHierarchy.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (value.equals(command))
				result.add(key);
		}

		return result;
	}

	public AbstractService<R, E> getService(final String command, final String method) {
		assert !StringHelper.isBlank(command) && this.isRegistered(command);
		assert method != null;

		AbstractService<R, E> result;

		result = this.supportServices.get(command);

		return result;
	}

}
