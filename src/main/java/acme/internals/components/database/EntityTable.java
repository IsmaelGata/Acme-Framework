/*
 * EntityTable.java
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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import acme.client.data.AbstractEntity;
import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.LocationAwareException;
import acme.internals.helpers.ReflectionHelper;
import lombok.CustomLog;

@CustomLog
public class EntityTable implements Iterable<EntityRecord> {

	// Constructors -----------------------------------------------------------

	public EntityTable() {
		this.source = null;
		this.clazz = null;
		this.instances = new LinkedHashMap<String, EntityRecord>();
	}

	public static EntityTable from(final File file) {
		assert file != null && file.exists() && file.isFile() && file.canRead();

		EntityTable result;
		Path path;

		EntityTable.logger.debug("Reading entities from '{}'.", file.getPath());
		result = new EntityTable();
		path = Paths.get(file.getPath());
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			CSVReaderHeaderAwareBuilder builder;
			CSVReaderHeaderAware parser;
			Map<String, String> mapping;
			String clazzName;
			Class<AbstractEntity> clazz;
			String key;

			builder = new CSVReaderHeaderAwareBuilder(reader);
			builder.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);
			builder.withKeepCarriageReturn(false);
			builder.withSkipLines(0);
			parser = builder.build();

			clazzName = file.getName().replaceFirst("\\.csv$", "");
			clazzName = ReflectionHelper.computeCamelName(clazzName, true);
			clazz = ReflectionHelper.findEntityClazz(clazzName);
			assert clazz != null : String.format("%s: could not find a class to map the entities in this resource.", file.getPath());
			result.setClazz(clazz);

			while ((mapping = parser.readMap()) != null) {
				assert mapping.containsKey("key") : String.format("%s: could not find field 'key' in this resource.", file.getPath());
				key = mapping.get("key");
				assert !result.hasRecord(key) : String.format("%s: key '%s' is duplicated.", file.getPath(), key);
				EntityTable.logger.debug("Reading mapping '{}' from '{}'.", mapping, file.getPath());
				result.add(file, parser.getLinesRead(), 0, clazz, mapping);
			}
		} catch (final Throwable oops) {
			throw new LocationAwareException(file, oops);
		}

		return result;
	}

	// Internal state ---------------------------------------------------------


	private File							source;
	private Class<AbstractEntity>			clazz;
	private final Map<String, EntityRecord>	instances;

	// Interface Iterator -----------------------------------------------------


	@Override
	public Iterator<EntityRecord> iterator() {
		Iterator<EntityRecord> result;

		result = this.instances.values().iterator();

		return result;
	}

	// Properties -------------------------------------------------------------

	public void setClazz(final Class<AbstractEntity> clazz) {
		assert clazz != null;

		this.clazz = clazz;
	}

	public Class<AbstractEntity> getClazz() {
		return this.clazz;
	}

	public File getSource() {
		return this.source;
	}

	public void setSource(final File source) {
		assert source != null;

		this.source = source;
	}

	public void add(final File source, final long line, final long column, final Class<AbstractEntity> clazz, final Map<String, String> mapping) {
		assert source != null;
		assert line >= 0;
		assert column >= 0;
		assert clazz != null;
		assert mapping != null;

		EntityRecord record;

		record = new EntityRecord();
		record.setSource(source);
		record.setLine(line);
		record.setColumn(column);
		for (final Entry<String, String> entry : mapping.entrySet()) {
			String key, value;

			key = entry.getKey();
			value = entry.getValue();
			value = value != null && value.trim().equals("null") ? null : value;
			record.put(key, value);
		}
		record.setClazz(clazz);
		this.add(record);
	}

	public void add(final EntityRecord record) {
		assert record != null && record.hasKey();
		assert !this.hasRecord(record.getKey());

		String key;

		key = record.getKey();
		this.instances.put(key, record);
	}

	public boolean hasRecord(final String key) {
		assert !StringHelper.isBlank(key);

		boolean result;

		result = this.instances.containsKey(key);

		return result;
	}

	public EntityRecord getRecord(final String key) {
		assert !StringHelper.isBlank(key);

		EntityRecord result;

		result = this.getRecord(key);

		return result;
	}

}
