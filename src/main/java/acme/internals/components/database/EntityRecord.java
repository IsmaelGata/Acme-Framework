/*
 * EntityRecord.java
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
import java.util.HashMap;

import acme.client.data.AbstractEntity;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, of = {
	"source", "line", "column", "clazz"
})
@EqualsAndHashCode(callSuper = true)
public class EntityRecord extends HashMap<String, Object> {

	// Serializable interface -------------------------------------------------

	private static final long		serialVersionUID	= 1L;

	// Internal state ---------------------------------------------------------

	private File					source;
	private long					line, column;
	private Class<AbstractEntity>	clazz;

	// Constructors -----------------------------------------------------------


	public EntityRecord() {
		this.source = null;
		this.line = 0;
		this.column = 0;
		this.clazz = null;
	}

	// Properties -------------------------------------------------------------

	public File getSource() {
		return this.source;
	}

	public void setSource(final File source) {
		assert source != null;

		this.source = source;
	}

	public long getLine() {
		return this.line;
	}

	public void setLine(final long line) {
		assert line >= 0;

		this.line = line;
	}

	public long getColumn() {
		return this.column;
	}

	public void setColumn(final long column) {
		assert column >= 0;

		this.column = column;
	}

	public boolean hasKey() {
		boolean result;

		result = this.containsKey("key") && this.get("key") instanceof String;

		return result;
	}

	public Class<AbstractEntity> getClazz() {
		return this.clazz;
	}

	public void setClazz(final Class<AbstractEntity> clazz) {
		assert clazz != null;

		this.clazz = clazz;
	}

	public String getKey() {
		assert this.hasKey();

		String result;

		result = (String) this.get("key");

		return result;
	}

}
