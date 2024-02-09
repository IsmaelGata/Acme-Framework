/*
 * EntityStore.java
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acme.client.helpers.FileHelper;

public class EntityStore implements Iterable<EntityTable> {

	// Constructors -----------------------------------------------------------

	public EntityStore() {
		this.folder = null;
		this.tableMap = new LinkedHashMap<Class<?>, EntityTable>();
	}

	public static EntityStore from(final File folder) {
		assert folder != null && folder.exists() && folder.isDirectory() && folder.canRead();

		EntityStore result;
		List<File> sourceFiles;
		EntityTable table;

		result = new EntityStore();
		sourceFiles = FileHelper.listFiles(folder.getPath(), "csv");
		for (final File sourceFile : sourceFiles) {
			assert sourceFile.exists() && sourceFile.isFile() && sourceFile.canRead() : String.format("Could not read from from resource '%s'.", sourceFile.getPath());
			table = EntityTable.from(sourceFile);
			result.add(table);
		}

		return result;
	}

	// Interface Iterable -----------------------------------------------------

	@Override
	public Iterator<EntityTable> iterator() {
		Iterator<EntityTable> result;

		result = this.tableMap.values().iterator();

		return result;
	}

	// Internal state ---------------------------------------------------------


	private File								folder;
	private final Map<Class<?>, EntityTable>	tableMap;

	// Properties -------------------------------------------------------------


	public File getFolder() {
		return this.folder;
	}

	public void setFolder(final File folder) {
		assert folder != null;

		this.folder = folder;
	}

	public void add(final EntityTable table) {
		assert table != null;

		Class<?> clazz;
		EntityTable base;

		clazz = table.getClazz();
		if (!this.hasEntityTable(clazz))
			this.tableMap.put(clazz, table);
		else {
			base = this.tableMap.get(clazz);
			for (final EntityRecord record : table) {
				String key;

				key = record.getKey();
				assert !base.hasRecord(key) : String.format("Found duplicated key '%s' in '%s'.", key, record.getSource().getPath());
				base.add(record);
			}
		}
	}

	public boolean hasEntityTable(final Class<?> clazz) {
		assert clazz != null;

		boolean result;

		result = this.tableMap.containsKey(clazz);

		return result;
	}

	public EntityTable getEntityTable(final Class<?> clazz) {
		assert clazz != null;
		assert this.hasEntityTable(clazz);

		EntityTable result;
		result = this.tableMap.get(clazz);

		return result;
	}

}
