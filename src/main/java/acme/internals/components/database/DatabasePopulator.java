/*
 * DatabasePopulator.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.database;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.OneToMany;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import acme.client.data.AbstractDatatype;
import acme.client.data.AbstractEntity;
import acme.client.data.models.Errors;
import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.PassThroughException;
import acme.internals.helpers.EnvironmentHelper;
import acme.internals.helpers.ErrorsHelper;
import acme.internals.helpers.ReflectionHelper;
import acme.internals.helpers.ThrowableHelper;
import lombok.CustomLog;

@Component
@CustomLog
public class DatabasePopulator {

	// Constructors -----------------------------------------------------------

	protected DatabasePopulator() {
	}

	// Internal state ---------------------------------------------------------


	@Autowired
	private DatabaseManager	manager;

	@Autowired
	private Validator		validator;

	@Autowired
	private ResourceLoader	loader;

	// Business methods -------------------------------------------------------


	public void populate(final boolean createSchema, final boolean sampleData) {
		String initialFilename, sampleFilename;

		initialFilename = EnvironmentHelper.getRequiredProperty("acme.population.initial-data", String.class);
		sampleFilename = EnvironmentHelper.getRequiredProperty("acme.population.sample-data", String.class);

		if (!sampleData)
			this.populate(createSchema, initialFilename);
		else
			this.populate(createSchema, initialFilename, sampleFilename);
	}

	// Ancillary methods ------------------------------------------------------

	protected void populate(final boolean createSchema, final String... resourcePaths) {
		assert !StringHelper.someBlank(resourcePaths);

		EntityWeb web;

		try {
			web = new EntityWeb();
			for (final String resourcePath : resourcePaths) {
				DatabasePopulator.logger.debug("Reading entities from '{}'.", resourcePath);
				this.readEntities(web, resourcePath);
			}
			this.validate(web);
			this.sort(web);
			if (createSchema)
				this.manager.createSchema();
			else
				this.manager.cleanSchema();
			this.persist(web);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}
	}

	protected void checkAttributeTypes(final Errors errors, final AbstractEntity entity) {
		assert errors != null;
		assert entity != null;

		Map<String, TypeDescriptor> descriptors;

		descriptors = ReflectionHelper.getProperties(entity);
		for (final Entry<String, TypeDescriptor> entry : descriptors.entrySet()) {
			String propertyName;
			TypeDescriptor propertyType;

			propertyName = entry.getKey();
			propertyType = entry.getValue();
			if (!this.isTypeAllowed(propertyType))
				errors.add(propertyName, "Only entity, mapped entity collections, and datatype types are allowed for persistent attributes.");
		}
	}

	protected boolean isTypeAllowed(final TypeDescriptor descriptor) {
		assert descriptor != null;

		boolean result;
		Class<?> rootClazz, componentClazz;
		OneToMany oneToMany;
		String mappedBy;

		rootClazz = descriptor.getType();
		result = ReflectionHelper.isPrimitive(descriptor) || //
			ReflectionHelper.isEnum(descriptor) || //
			GrantedAuthority.class.isAssignableFrom(rootClazz) || //
			AbstractEntity.class.isAssignableFrom(rootClazz) || //
			AbstractDatatype.class.isAssignableFrom(rootClazz);

		if (!result && ReflectionHelper.isCollection(rootClazz)) {
			componentClazz = descriptor.getElementTypeDescriptor().getObjectType(); // NOSONAR
			oneToMany = descriptor.getAnnotation(javax.persistence.OneToMany.class);
			mappedBy = oneToMany != null ? oneToMany.mappedBy() : null;

			result = AbstractEntity.class.isAssignableFrom(componentClazz) && (oneToMany == null || mappedBy != null);
		}

		return result;
	}

	protected void readEntities(final EntityWeb web, final String resourcePath) {
		assert web != null;
		assert !StringHelper.isBlank(resourcePath);

		File file;
		Resource resource;
		EntityStore store;

		try {
			resource = this.loader.getResource(resourcePath);
			assert resource.exists() : String.format("Could not find resource '%s'.", resourcePath);
			file = resource.getFile();
			assert file.canRead() : String.format("Could not read from resource '%s'.", resourcePath);
			assert file.isDirectory() : String.format("Resource '%s' is not a folder.", resourcePath);
			store = EntityStore.from(file);
			web.add(store);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}
	}

	protected void validate(final EntityWeb web) {
		assert web != null;

		Set<ConstraintViolation<AbstractEntity>> violations;
		Errors errors;
		String name, fullName;
		AbstractEntity entity;
		String message;

		DatabasePopulator.logger.debug("Validating your entities.");
		for (final Entry<String, AbstractEntity> entry : web) {
			name = entry.getKey();
			entity = entry.getValue();

			violations = this.validator.validate(entity);
			errors = new Errors();
			ErrorsHelper.transferErrors(violations, errors);
			this.checkAttributeTypes(errors, entity);
			fullName = String.format("%s@%s", entity.getClass().getName(), name);

			if (!errors.hasErrors())
				DatabasePopulator.logger.debug("Validating '{}' ... PASS.", name);
			else {
				DatabasePopulator.logger.error("Validating '{}' ... FAILED.", name);
				message = ThrowableHelper.toString(fullName, errors);
				throw new ValidationException(message);
			}
		}
	}

	protected void sort(final EntityWeb web) {
		assert web != null;

		List<Class<AbstractEntity>> order;
		Set<List<Class<AbstractEntity>>> cycles;

		DatabasePopulator.logger.debug("Sorting your entity clazzes topologically.");
		web.close();
		order = web.getClazzOrder();
		cycles = web.getCycles();

		DatabasePopulator.logger.debug("Best topological order for your entity clazzes:");
		for (final Class<?> clazz : order)
			DatabasePopulator.logger.debug("- {}", clazz.getName());

		if (!cycles.isEmpty()) {
			DatabasePopulator.logger.debug("Cycles between entity clazzes:");
			for (final List<Class<AbstractEntity>> cycle : cycles) {
				StringBuilder message;
				String separator;

				message = new StringBuilder();
				separator = "";
				for (final Class<AbstractEntity> clazz : cycle) {
					message.append(separator);
					message.append(clazz.getName());
					separator = " -> ";
				}
				DatabasePopulator.logger.debug("- {}", message.toString());
			}
		}

		assert cycles.isEmpty() : "Cannot persist your entities due cyclic clazz dependencies.";
	}

	protected void persist(final EntityWeb web) {
		assert web != null;

		List<Class<AbstractEntity>> order;
		Collection<AbstractEntity> entities;

		try {
			DatabasePopulator.logger.debug("Persisting your entities.");
			order = web.getClazzOrder();
			this.manager.startTransaction();
			for (final Class<AbstractEntity> clazz : order) {
				DatabasePopulator.logger.debug("Persisting entities of clazz '{}'.", clazz.getName());
				entities = web.getEntities(clazz);
				this.manager.persist(entities);
			}
			this.manager.commitTransaction();
		} catch (final Throwable oops) {
			this.manager.rollbackTransaction();
			throw new PassThroughException(oops);
		}
	}

}
