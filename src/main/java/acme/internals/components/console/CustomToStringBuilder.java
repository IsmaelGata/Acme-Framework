/*
 * CustomToStringBuilder.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.console;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CustomToStringBuilder extends ReflectionToStringBuilder {

	// Constructors -----------------------------------------------------------

	public CustomToStringBuilder(final Object object) {
		super(object);

		assert object != null;
	}

	public CustomToStringBuilder(final Object object, final ToStringStyle style) {
		super(object, style);

		assert object != null;
		assert style != null;
	}

	public CustomToStringBuilder(final Object object, final ToStringStyle style, final StringBuffer buffer) {
		super(object, style, buffer);

		assert object != null;
		assert style != null;
		assert buffer != null;
	}

	public CustomToStringBuilder(final Object object, final ToStringStyle style, final StringBuffer buffer, final Class<?> reflectUpToClass, final boolean outputTransients) {
		super(object, style, buffer);

		assert object != null;
		assert style != null;
		assert buffer != null;
		assert reflectUpToClass != null;

		this.setUpToClass(reflectUpToClass);
		this.setAppendTransients(outputTransients);
	}

	// Internal state ---------------------------------------------------------


	private static CustomPrintStyle customQueryStyle = new CustomPrintStyle();

	// Business methods -------------------------------------------------------


	public static String toString(final Object object) {
		// HINT: object can be null

		String result;
		StringBuffer buffer;
		CustomToStringBuilder builder;

		if (object == null)
			result = "null";
		else if (CustomToStringBuilder.isPrimitive(object)) {
			buffer = new StringBuffer();
			buffer.append(object.getClass().getName());
			buffer.append("{");
			CustomToStringBuilder.customQueryStyle.appendObject(buffer, object);
			buffer.append("}");
			result = buffer.toString();
		} else {
			builder = new CustomToStringBuilder(object, CustomToStringBuilder.customQueryStyle);
			result = builder.toString();
		}

		return result;
	}

	// Object interface -------------------------------------------------------

	@Override
	public String toString() {
		String result;
		Object obj;
		CustomPrintStyle style;
		StringBuffer buffer;
		Class<?> clazz;

		obj = this.getObject();

		if (obj == null)
			result = "null";
		else {
			clazz = obj.getClass();
			this.processSuperClazzes(clazz);
			style = (CustomPrintStyle) this.getStyle();
			buffer = this.getStringBuffer();
			style.appendEnd(buffer, obj);
			result = buffer.toString();
		}

		return result;
	}

	// Internal methods -------------------------------------------------------

	protected void processSuperClazzes(Class<?> clazz) {
		assert clazz != null;

		List<Class<?>> superClazzes;

		superClazzes = new ArrayList<Class<?>>();
		while (clazz != null && clazz != this.getUpToClass()) {
			superClazzes.add(clazz);
			clazz = clazz.getSuperclass();
		}

		for (int i = superClazzes.size() - 1; i >= 0; i--) {
			clazz = superClazzes.get(i);
			this.appendFieldsIn(clazz);
		}
	}

	private static boolean isPrimitive(final Object object) {
		// HINT: object can be null

		boolean result;

		result = object instanceof String || object instanceof Number || //
			object instanceof Character || object instanceof Boolean || //
			object instanceof java.util.Date || object instanceof java.sql.Date || //
			object instanceof Timestamp;

		return result;
	}

}
