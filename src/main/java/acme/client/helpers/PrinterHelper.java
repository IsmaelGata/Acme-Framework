/*
 * PrinterHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.helpers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import acme.internals.helpers.ReflectionHelper;

public abstract class PrinterHelper {

	// Constructors -----------------------------------------------------------

	protected PrinterHelper() {
	}

	// Business methods -------------------------------------------------------

	public static String printObject(final Object object, final boolean summary) {
		// HINT: object can be null

		StringBuilder buffer;

		buffer = new StringBuilder();
		PrinterHelper.printObject(buffer, object, summary);

		return buffer.toString();
	}

	public static void printObject(final StringBuilder buffer, final Object object, final boolean summary) {
		assert buffer != null;
		// HINT: object can be null

		boolean hasStructure;

		hasStructure = PrinterHelper.hasStructure(object);
		if (hasStructure)
			PrinterHelper.printStructure(buffer, object, summary);
		else
			PrinterHelper.printLiteral(buffer, object, summary);
	}

	public static String printType(final Type type) {
		assert type != null;

		StringBuilder buffer;

		buffer = new StringBuilder();
		PrinterHelper.printType(buffer, type);

		return buffer.toString();
	}

	public static void printType(final StringBuilder buffer, final Type type) {
		assert buffer != null;
		assert type != null;

		String name;

		name = type.getTypeName();
		buffer.append(name);
	}

	public static String printType(final Object type) {
		assert type != null;

		StringBuilder buffer;

		buffer = new StringBuilder();
		PrinterHelper.printType(buffer, type);

		return buffer.toString();
	}

	public static void printType(final StringBuilder buffer, final Object object) {
		assert buffer != null;
		// HINT: object can be null;

		final Class<?> clazz;

		if (object == null)
			clazz = Object.class;
		else
			clazz = object.getClass();
		PrinterHelper.printType(buffer, clazz);
	}

	public static String printCast(final Type type) {
		assert type != null;

		StringBuilder buffer;

		buffer = new StringBuilder();
		PrinterHelper.printCast(buffer, type);

		return buffer.toString();
	}

	public static void printCast(final StringBuilder buffer, final Type type) {
		assert buffer != null;
		assert type != null;

		buffer.append("(");
		PrinterHelper.printType(buffer, type);
		buffer.append(")");
	}

	public static String printCast(final Object object) {
		assert object != null;

		StringBuilder buffer;

		buffer = new StringBuilder();
		PrinterHelper.printCast(buffer, object);

		return buffer.toString();
	}

	private static void printCast(final StringBuilder buffer, final Object object) {
		assert buffer != null;
		// HINT: object can be null

		final Class<?> clazz;

		buffer.append("(");
		if (object == null)
			clazz = Object.class;
		else
			clazz = object.getClass();
		PrinterHelper.printType(buffer, clazz);
		buffer.append(")");
	}

	// Ancillary methods ------------------------------------------------------

	private static boolean hasStructure(final Object object) {
		// HINT: object can be null

		boolean result;

		result = !ReflectionHelper.isPrimitive(object) && //
			!ReflectionHelper.isEnum(object) && //
			!ReflectionHelper.isArray(object) && //
			!ReflectionHelper.isCollection(object) && //
			!ReflectionHelper.isMap(object);

		return result;
	}

	private static void printLiteral(final StringBuilder buffer, final Object object, final boolean summary) {
		assert buffer != null;
		// HINT: object can be null

		if (object == null || ReflectionHelper.isPrimitive(object))
			PrinterHelper.printPrimitive(buffer, object, summary);
		else if (ReflectionHelper.isEnum(object))
			PrinterHelper.printEnum(buffer, object, summary);
		else if (ReflectionHelper.isArray(object))
			PrinterHelper.printArray(buffer, object, summary);
		else if (ReflectionHelper.isCollection(object))
			PrinterHelper.printCollection(buffer, (Collection<?>) object, summary);
		else if (ReflectionHelper.isMap(object))
			PrinterHelper.printMap(buffer, (Map<?, ?>) object, summary);
		else {
			if (!summary)
				PrinterHelper.printCast(buffer, object);
			buffer.append(object.toString());
		}
	}

	private static void printStructure(final StringBuilder buffer, final Object object, final boolean summary) {
		assert buffer != null;
		// HINT: object can be null
		assert PrinterHelper.hasStructure(object);

		Class<?> clazz;
		String mark;
		List<Class<?>> superClazzes;
		List<Field> fields;

		if (summary)
			buffer.append("{");
		else
			PrinterHelper.printLiteral(buffer, object, true);
		mark = "";
		superClazzes = ReflectionHelper.getAllClazzes(object, Object.class);
		for (int i = superClazzes.size() - 1; i >= 0; i--) {
			clazz = superClazzes.get(i);
			fields = ReflectionHelper.getDeclaredFields(clazz, object, summary);
			if (!fields.isEmpty()) {
				buffer.append(mark);
				if (PrinterHelper.hasStructure(object))
					PrinterHelper.printFieldsInClazz(buffer, clazz, object, summary);
				else
					PrinterHelper.printLiteral(buffer, object, summary);
				mark = ", ";
			}
		}
		if (summary)
			buffer.append("}");
	}

	private static void printFieldsInClazz(final StringBuilder buffer, final Class<?> clazz, final Object object, final boolean summary) {
		assert buffer != null;
		assert clazz != null;
		assert object != null;

		List<Field> fields;
		String mark;

		fields = ReflectionHelper.getDeclaredFields(clazz, object, summary);
		if (!summary) {
			buffer.append("\n\t");
			buffer.append("from ");
			buffer.append(clazz.getName());
			buffer.append(":");
		}
		mark = "";
		for (final Field field : fields) {
			String name;
			Type type;
			Object value;
			int modifiers;

			name = field.getName();
			type = field.getGenericType();
			modifiers = field.getModifiers();

			try {
				value = field.get(object);
			} catch (final Throwable oops) {
				value = String.format("{%s}", oops.getMessage());
			}

			buffer.append(mark);
			if (!summary) {
				buffer.append("\n\t");
				if (Modifier.isPrivate(modifiers))
					buffer.append("-");
				else if (Modifier.isPublic(modifiers))
					buffer.append("+");
				else if (Modifier.isProtected(modifiers))
					buffer.append("#");
				else
					buffer.append("@");
				if (Modifier.isTransient(modifiers))
					buffer.append("~");
				buffer.append(" ");
			}
			buffer.append(name);
			if (!summary) {
				buffer.append(": ");
				PrinterHelper.printType(buffer, type);
			}
			buffer.append(" = ");
			PrinterHelper.printLiteral(buffer, value, true);
			mark = summary ? ", " : "";
		}
	}

	private static void printPrimitive(final StringBuilder buffer, final Object object, final boolean summary) {
		assert buffer != null;
		// HINT: object can be null

		String lpar, rpar;

		if (object == null)
			lpar = rpar = "";
		else if (object instanceof String)
			lpar = rpar = "\"";
		else if (object instanceof Number)
			lpar = rpar = "";
		else if (object instanceof Character)
			lpar = rpar = "\'";
		else if (object instanceof Boolean)
			lpar = rpar = "";
		else {
			lpar = "<<";
			rpar = ">>";
		}

		if (!summary)
			PrinterHelper.printCast(buffer, object);
		buffer.append(lpar);
		buffer.append(object == null ? "null" : object.toString());
		buffer.append(rpar);
	}

	private static void printEnum(final StringBuilder buffer, final Object object, final boolean summary) {
		assert buffer != null;
		assert object != null;

		if (!summary)
			PrinterHelper.printCast(buffer, object);
		buffer.append("<<");
		buffer.append(object.toString());
		buffer.append(">>");
	}

	private static void printArray(final StringBuilder buffer, final Object object, final boolean summary) {
		assert buffer != null;
		assert object != null;

		String separator;
		int length;
		Object item;

		if (!summary)
			PrinterHelper.printCast(buffer, object);
		separator = "";
		buffer.append("[");
		length = Array.getLength(object);
		for (int i = 0; i < length; i++) {
			item = Array.get(object, i);
			buffer.append(separator);
			PrinterHelper.printLiteral(buffer, item, summary);
			separator = ", ";
		}
		buffer.append("]");
	}

	private static void printCollection(final StringBuilder buffer, final Collection<?> objects, final boolean summary) {
		assert buffer != null;
		assert objects != null;  // HINT: the objects in the collection can be null

		String separator;

		if (!summary)
			PrinterHelper.printCast(buffer, objects);
		separator = "";
		buffer.append("[");
		for (final Object item : objects) {
			buffer.append(separator);
			PrinterHelper.printLiteral(buffer, item, summary);
			separator = ", ";
		}
		buffer.append("]");
	}

	private static void printMap(final StringBuilder buffer, final Map<?, ?> map, final boolean summary) {
		assert buffer != null;
		assert map != null;  // HINT: the objects in the map can be null

		String separator;

		if (!summary)
			PrinterHelper.printCast(buffer, map);
		separator = "";
		buffer.append("{");
		for (final Entry<?, ?> entry : map.entrySet()) {
			buffer.append(separator);
			PrinterHelper.printLiteral(buffer, entry.getKey(), summary);
			buffer.append(" = ");
			PrinterHelper.printLiteral(buffer, entry.getValue(), summary);
			separator = ", ";
		}
		buffer.append("}");
	}

}
