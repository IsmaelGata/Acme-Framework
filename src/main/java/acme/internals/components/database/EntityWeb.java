/*
 * EntityWeb.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import acme.client.data.AbstractEntity;
import acme.client.helpers.StringHelper;
import acme.internals.components.adts.Graph;
import acme.internals.helpers.ReflectionHelper;
import lombok.CustomLog;

@CustomLog
public class EntityWeb implements Iterable<Entry<String, AbstractEntity>> {

	// Internal state ---------------------------------------------------------

	private final Map<String, AbstractEntity>						keyMap;
	private final Map<Class<AbstractEntity>, List<AbstractEntity>>	clazzMap;
	private final List<Class<AbstractEntity>>						clazzes;
	private final Graph<Class<AbstractEntity>>						dependencies;

	// Constructors -----------------------------------------------------------


	public EntityWeb() {
		this.keyMap = new LinkedHashMap<String, AbstractEntity>();
		this.clazzMap = new LinkedHashMap<Class<AbstractEntity>, List<AbstractEntity>>();
		this.clazzes = new ArrayList<Class<AbstractEntity>>();
		this.dependencies = new Graph<Class<AbstractEntity>>();
	}

	// Iterable interface -----------------------------------------------------

	@Override
	public Iterator<Entry<String, AbstractEntity>> iterator() {
		Iterator<Entry<String, AbstractEntity>> result;

		result = this.keyMap.entrySet().iterator();

		return result;
	}

	// Properties -------------------------------------------------------------

	public boolean isClosed() {
		return this.dependencies.isClosed();
	}

	public List<Class<AbstractEntity>> getClazzOrder() {
		assert this.isClosed();

		return this.dependencies.getOrder();
	}

	public Set<List<Class<AbstractEntity>>> getCycles() {
		assert this.isClosed();

		return this.dependencies.getCycles();
	}

	// Business methods -------------------------------------------------------

	public int size() {
		int result;

		result = this.keyMap.size();

		return result;
	}

	public boolean contains(final String key) {
		assert !StringHelper.isBlank(key);

		boolean result;

		result = this.keyMap.containsKey(key);

		return result;
	}

	public boolean contains(final Class<AbstractEntity> clazz) {
		assert clazz != null;

		boolean result;

		result = this.clazzMap.containsKey(clazz);

		return result;
	}

	@SuppressWarnings("unchecked")
	public void add(final String key, final AbstractEntity entity) {
		assert !StringHelper.isBlank(key) && !this.contains(key);
		assert entity != null;
		assert !this.isClosed();

		Class<AbstractEntity> clazz;
		List<AbstractEntity> brotherhood;

		this.keyMap.put(key, entity);
		clazz = (Class<AbstractEntity>) entity.getClass();
		this.clazzes.add(clazz);

		if (this.clazzMap.containsKey(clazz))
			brotherhood = this.clazzMap.get(clazz);
		else {
			brotherhood = new ArrayList<AbstractEntity>();
			this.clazzMap.put(clazz, brotherhood);
		}
		brotherhood.add(entity);
	}

	public void update(final String key, final AbstractEntity entity) {
		assert !StringHelper.isBlank(key) && this.contains(key);
		assert entity != null;
		assert !this.isClosed();

		this.keyMap.put(key, entity);
	}

	public void add(final EntityStore store) {
		assert store != null;
		assert !this.isClosed();

		this.performPass(1, this, store);
		this.performPass(2, this, store);
	}

	public AbstractEntity getEntity(final Class<AbstractEntity> clazz, final String key) {
		assert clazz != null;
		assert !StringHelper.isBlank(key);

		AbstractEntity result;

		if (!this.keyMap.containsKey(key))
			result = null;
		else {
			result = this.keyMap.get(key);
			result = clazz.isAssignableFrom(result.getClass()) ? result : null;
		}

		return result;
	}

	public Collection<AbstractEntity> getEntities(final Class<AbstractEntity> clazz) {
		assert clazz != null && this.contains(clazz);

		Collection<AbstractEntity> result;

		result = this.clazzMap.get(clazz);

		return result;
	}

	public Collection<Class<AbstractEntity>> getClazzes() {
		return this.clazzes;
	}

	public void addDependency(final Class<AbstractEntity> master, final Class<AbstractEntity> slave) {
		assert master != null;
		assert slave != null;
		assert !this.isClosed();

		if (AbstractEntity.class.isAssignableFrom(slave) && !this.dependencies.hasEdge(master, slave)) {
			if (!this.dependencies.hasVertex(master))
				this.dependencies.addVertex(master);
			if (!this.dependencies.hasVertex(slave))
				this.dependencies.addVertex(slave);
			this.dependencies.addEdge(master, slave);
		}
	}

	public void addLeafClazz(final Class<AbstractEntity> master) {
		assert master != null;
		assert !this.isClosed();

		if (!this.dependencies.hasVertex(master))
			this.dependencies.addVertex(master);
	}

	public void close() {
		assert !this.isClosed();

		this.dependencies.close();
	}

	// Ancillary methods ------------------------------------------------------

	protected void performPass(final int passNumber, final EntityWeb web, final EntityStore store) {
		assert passNumber == 1 || passNumber == 2;
		assert web != null;
		assert store != null;

		for (final EntityTable table : store) {
			Class<AbstractEntity> clazz;

			clazz = table.getClazz();

			if (passNumber == 1)
				EntityWeb.logger.debug("Setting attributes of entities of class {}.", clazz.getName());
			else
				EntityWeb.logger.debug("Linking entities of class {}.", clazz.getName());

			for (final EntityRecord record : table) {
				String key;
				AbstractEntity entity;

				if (passNumber == 1) {
					key = record.getKey();
					entity = ReflectionHelper.instantiate(clazz);
					this.computeRegularAttributes(entity, record);
					assert !web.contains(key) : String.format("%s: duplicated key '%s' (check initial data).", record.getSource().getPath(), key);
					web.add(key, entity);
				} else {
					key = record.getKey();
					entity = web.getEntity(clazz, key);
					this.computeLinkAttributes(entity, record, web);
					web.update(key, entity);
				}
			}
		}
	}

	protected void computeRegularAttributes(final Object entity, final EntityRecord record) {
		assert entity != null;
		assert record != null;

		for (final Entry<String, Object> mappingEntry : record.entrySet()) {
			String property;
			Object value;

			property = mappingEntry.getKey();
			if (!property.equals("key") && !property.startsWith("key:")) {
				property = ReflectionHelper.computeCamelName(property, false);
				value = mappingEntry.getValue();
				assert ReflectionHelper.hasProperty(entity, property) : String.format("%s: cannot read/write property '%s::%s'.", record.getSource().getPath(), entity.getClass().getName(), property);
				try {
					ReflectionHelper.setProperty(entity, property, value);
				} catch (final Throwable oops) {
					assert false : String.format("%s: could not assign value '%s' to property '%s@%s::%s'.", record.getSource().getPath(), value, entity.getClass().getName(), record.getKey(), property);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void computeLinkAttributes(final Object entity, final EntityRecord record, final EntityWeb web) {
		assert web != null;
		assert entity != null;
		assert record != null;

		boolean leafClazz;

		leafClazz = true;
		for (final Entry<String, Object> recordEntry : record.entrySet()) {
			String property;
			Object value;
			Class<?> foreignClazz;
			String foreignKey;
			Object foreignEntity;

			property = recordEntry.getKey();
			if (property.startsWith("key:")) {
				leafClazz = false;
				property = property.replaceFirst("key:", "");
				property = ReflectionHelper.computeCamelName(property, false);
				assert ReflectionHelper.hasProperty(entity, property) : String.format("%s: cannot read/write property '%s::%s'.", record.getSource().getPath(), entity.getClass().getName(), property);
				foreignClazz = ReflectionHelper.getPropertyClazz(entity, property);
				assert AbstractEntity.class.isAssignableFrom(foreignClazz) : String.format("%s: property '%s' cannot be assigned an object of type '%s'.", record.getSource().getPath(), property, foreignClazz.getName());
				web.addDependency(record.getClazz(), (Class<AbstractEntity>) foreignClazz);

				value = recordEntry.getValue();
				if (value == null) {
					foreignKey = "null";
					foreignEntity = null;
				} else {
					assert value instanceof String : String.format("%s: the value for property '%s@%s::%s' must be a string.", record.getSource().getPath(), entity.getClass().getName(), record.getKey(), property);
					foreignKey = (String) value;
					foreignEntity = web.getEntity((Class<AbstractEntity>) foreignClazz, foreignKey);
					assert foreignEntity != null : String.format("%s: could not find entity '%s@%s' for property '%s@%s::%s'.", record.getSource().getPath(), foreignClazz.getName(), foreignKey, entity.getClass().getName(), record.getKey(), property);
				}

				try {
					ReflectionHelper.setProperty(entity, property, foreignEntity);
				} catch (final Throwable oops) {
					assert false : String.format("%s: could not assign entity '%s@%s' to property '%s@%s::%s'.", record.getSource().getPath(), foreignClazz.getName(), foreignKey, entity.getClass().getName(), record.getKey(), property);
				}
			}
		}

		if (leafClazz)
			web.addLeafClazz(record.getClazz());
	}

}
