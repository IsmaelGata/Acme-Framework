/*
 * ControllerMetadata.java
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

import acme.client.data.AbstractObject;
import acme.client.data.AbstractRole;
import acme.client.helpers.CollectionHelper;
import acme.internals.helpers.ReflectionHelper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ControllerMetadata<R extends AbstractRole, O extends AbstractObject> {

	// Constructors -----------------------------------------------------------

	@SuppressWarnings("unchecked")
	public ControllerMetadata(final Class<?>[] types) {
		assert !CollectionHelper.someNull(types) && types.length == 2;

		// HINT: unfortunately, the parameters in this class are not bound directly,
		// HINT+ but indirectly from the AbstractController class.  Thus, one cannot
		// HINT+ expect the information about the actual parameters to be available here.
		// HINT+ That is the reason why it must be passed on to the constructor from 
		// HINT+ the AbstractController constructor.

		this.roleClazz = (Class<R>) types[0];
		this.objectClazz = (Class<O>) types[1];

		this.roleName = this.roleClazz.getSimpleName();
		this.roleName = ReflectionHelper.computeKebapName(this.roleName);
		this.objectName = this.objectClazz.getSimpleName();
		this.objectName = ReflectionHelper.computeKebapName(this.objectName);

		this.listView = String.format("%s/%s/list", this.roleName, this.objectName);
		this.formView = String.format("%s/%s/form", this.roleName, this.objectName);
		this.requestPath = String.format("/%s/%s/", this.roleName, this.objectName);
	}

	// Properties -------------------------------------------------------------


	private Class<R>	roleClazz;

	private Class<O>	objectClazz;

	private String		roleName;

	private String		objectName;

	private String		listView;

	private String		formView;

	private String		requestPath;

}
