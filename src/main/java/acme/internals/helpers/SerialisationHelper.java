/*
 * SerialisationHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import acme.client.helpers.StringHelper;
import acme.internals.components.adts.FileRecord;
import acme.internals.components.exceptions.PassThroughException;

public abstract class SerialisationHelper {

	// Constructors -----------------------------------------------------------

	protected SerialisationHelper() {
	}

	// Datatypes --------------------------------------------------------------


	public enum Format {
		BASE64, JSON, CSV;
	}

	// Business methods -------------------------------------------------------


	public static String write(final Format format, final Object object) {
		assert format != null;
		assert object != null;

		String result;

		result = switch (format) {
		case BASE64 -> SerialisationHelper.serialiseBase64(object);
		case JSON -> SerialisationHelper.serialiseJson(object);
		case CSV -> SerialisationHelper.serialiseCsv(object);
		};

		return result;
	}

	public static void write(final Format format, final File file, final Object objects) {
		assert format != null;
		assert file != null && file.canWrite();
		assert objects != null;

		throw new UnsupportedOperationException();
	}

	public static <T> T read(final Format format, final String text, final Class<T> clazz) {
		assert format != null;
		assert !StringHelper.isBlank(text);
		assert clazz != null;

		T result;

		result = switch (format) {
		case BASE64 -> SerialisationHelper.deserialiseBase64(text, clazz);
		case JSON -> SerialisationHelper.deserialiseJson(text, clazz);
		case CSV -> SerialisationHelper.deserialiseCsv(text, clazz);
		};

		return result;
	}

	public static <T> List<FileRecord<T>> read(final Format format, final File file, final Class<T> clazz) {
		assert format != null;
		assert file != null && file.canRead();
		assert clazz != null;

		List<FileRecord<T>> result;

		result = switch (format) {
		case BASE64 -> SerialisationHelper.deserialiseBase64File(file, clazz);
		case JSON -> SerialisationHelper.deserialiseJsonFile(file, clazz);
		case CSV -> SerialisationHelper.deserialiseCsvFile(file, clazz);
		};

		return result;
	}

	public static String computeHeader(final Format format, final Class<?> clazz) {
		assert format != null;
		assert clazz != null;

		String result;

		result = switch (format) {
		case BASE64 -> SerialisationHelper.computeBase64Header(clazz);
		case JSON -> SerialisationHelper.computeJsonHeader(clazz);
		case CSV -> SerialisationHelper.computeCsvHeader(clazz);
		};

		return result;
	}

	public static <T> List<T> filterComments(final List<FileRecord<T>> data) {
		List<T> result;

		result = new ArrayList<T>();
		for (final FileRecord<T> record : data) {
			T object;

			if (record.hasObject()) {
				object = record.getObject();
				result.add(object);
			}
		}

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	// Base64 serialisation ...................................................


	private static String	STRONG_KEY;
	private static String	SALT;

	static {
		SerialisationHelper.STRONG_KEY = "\\/3ry-$tr0ng-|3@s364-K3y!";
		SerialisationHelper.SALT = "aabbccdd";
	}


	private static String serialiseBase64(final Object object) {
		assert object != null;

		String result;
		ByteArrayOutputStream outputStream;
		ObjectOutputStream objectStream;
		TextEncryptor encryptor;

		try {
			outputStream = new ByteArrayOutputStream();
			objectStream = new ObjectOutputStream(outputStream);
			objectStream.writeObject(object);
			objectStream.close();

			result = new String(Base64.getEncoder().encode(outputStream.toByteArray()));

			encryptor = Encryptors.text(SerialisationHelper.STRONG_KEY, SerialisationHelper.SALT);
			result = encryptor.encrypt(result);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T deserialiseBase64(final String text, final Class<T> clazz) {
		assert !StringHelper.isBlank(text);
		assert clazz != null;

		final T result;
		String clearText;
		byte[] data;
		InputStream inputStream;
		TextEncryptor encryptor;
		Object datum;

		try {
			encryptor = Encryptors.text(SerialisationHelper.STRONG_KEY, SerialisationHelper.SALT);
			clearText = encryptor.decrypt(text);

			data = Base64.getDecoder().decode(clearText.getBytes());
			inputStream = new ByteArrayInputStream(data);
			try (ObjectInputStream objectStream = new ObjectInputStream(inputStream)) {
				datum = objectStream.readObject();
			}
			assert ReflectionHelper.isAssignable(clazz, datum) : String.format("Cannot deserialise '%s' as' %s'", text, clazz.getName());
			result = (T) datum;
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	private static <T> List<FileRecord<T>> deserialiseBase64File(final File file, final Class<T> clazz) {
		assert file != null && file.canRead();
		assert clazz != null;

		throw new UnsupportedOperationException();
	}

	private static String computeBase64Header(final Class<?> clazz) {
		assert clazz != null;

		throw new UnsupportedOperationException();
	}

	// Json serialisation .....................................................


	private static Pattern jsonPattern;

	static {
		SerialisationHelper.jsonPattern = Pattern.compile("^(?<C>\\w+)?(?<D>\\{.*\\})$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}


	private static String serialiseJson(final Object object) {
		assert object != null;

		String result;
		ObjectWriter writer;

		try {
			writer = SerialisationHelper.buildJsonWriter(object.getClass());
			result = writer.writeValueAsString(object);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T deserialiseJson(final String text, final Class<T> clazz) {
		assert !StringHelper.isBlank(text);
		assert clazz != null;

		T result;
		Matcher matcher;
		String clazzName, definition;
		Class<?> actualClazz, declaredClazz;
		ObjectReader reader;

		try {
			matcher = SerialisationHelper.jsonPattern.matcher(text);
			if (!matcher.find())
				throw new IllegalArgumentException(String.format("Cannot parse '%s' as JSON", text));
			clazzName = matcher.group("C");
			if (clazzName == null) {
				definition = text;
				actualClazz = clazz;
			} else {
				definition = matcher.group("D");
				declaredClazz = ReflectionHelper.findDatatypeClazz(clazzName);
				assert declaredClazz != null : String.format("Class '%s' is not a datatype", clazzName);
				assert ReflectionHelper.isAssignable(clazz, declaredClazz) : String.format("Cannot cast '%s' as '%s'", declaredClazz.getName(), clazz.getName());
				actualClazz = declaredClazz;
			}

			reader = SerialisationHelper.buildJsonReader(actualClazz);
			result = (T) reader.readValue(definition, actualClazz);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	private static <T> List<FileRecord<T>> deserialiseJsonFile(final File file, final Class<T> clazz) {
		assert file != null && file.canRead();
		assert clazz != null;

		List<FileRecord<T>> result;
		ObjectReader reader;

		reader = SerialisationHelper.buildJsonReader(clazz);
		result = SerialisationHelper.readLines(file, reader, false, clazz);

		return result;
	}

	private static String computeJsonHeader(final Class<?> clazz) {
		assert clazz != null;

		throw new UnsupportedOperationException();
	}

	private static ObjectReader buildJsonReader(final Class<?> clazz) {
		assert clazz != null;

		ObjectReader result;
		ObjectMapper mapper;

		mapper = SerialisationHelper.buildJsonMapper();
		result = mapper.readerFor(clazz);

		return result;
	}

	private static ObjectWriter buildJsonWriter(final Class<?> clazz) {
		assert clazz != null;

		ObjectWriter result;
		ObjectMapper mapper;

		mapper = SerialisationHelper.buildJsonMapper();
		result = mapper.writerFor(clazz);

		return result;

	}

	private static ObjectMapper buildJsonMapper() {
		JsonMapper result;

		result = new JsonMapper();
		result.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		result.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		result.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

		return result;
	}

	// CSV serialisation ......................................................

	private static String serialiseCsv(final Object object) {
		assert object != null;

		String result;
		ObjectWriter writer;

		try {
			writer = SerialisationHelper.buildCsvWriter(object.getClass());
			result = writer.writeValueAsString(object);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	private static <T> T deserialiseCsv(final String text, final Class<T> clazz) {
		assert !StringHelper.isBlank(text);
		assert clazz != null;

		throw new UnsupportedOperationException();
	}

	private static <T> List<FileRecord<T>> deserialiseCsvFile(final File file, final Class<T> clazz) {
		assert file != null && file.canRead();
		assert clazz != null;

		List<FileRecord<T>> result;
		ObjectReader reader;

		reader = SerialisationHelper.buildCsvReader(clazz);
		result = SerialisationHelper.readLines(file, reader, true, clazz);

		return result;
	}

	private static String computeCsvHeader(final Class<?> clazz) {
		assert clazz != null;

		String result;
		CsvMapper mapper;
		CsvSchema schema;
		ObjectWriter writer;

		try {
			// HINT: getting the schema is a bit tricky, sin a writer with typed schema does not work. 
			mapper = (CsvMapper) SerialisationHelper.buildCsvMapper();
			schema = mapper.schemaFor(clazz).withHeader();
			writer = mapper.writer(schema);
			result = writer.writeValueAsString(null);
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	private static ObjectReader buildCsvReader(final Class<?> clazz) {
		assert clazz != null;

		ObjectReader result;
		CsvMapper mapper;
		CsvSchema schema;

		mapper = (CsvMapper) SerialisationHelper.buildCsvMapper();
		schema = mapper.schemaFor(clazz).withAllowComments(true);
		result = mapper.readerFor(clazz).with(schema);

		return result;
	}

	private static ObjectWriter buildCsvWriter(final Class<?> clazz) {
		assert clazz != null;

		final ObjectWriter result;

		CsvMapper mapper;

		mapper = (CsvMapper) SerialisationHelper.buildCsvMapper();
		result = mapper.writerWithTypedSchemaFor(clazz);

		return result;
	}

	private static ObjectMapper buildCsvMapper() {
		CsvMapper result;

		result = new CsvMapper();
		result.configure(CsvParser.Feature.ALLOW_COMMENTS, true);
		result.configure(CsvParser.Feature.ALLOW_TRAILING_COMMA, false);
		result.configure(CsvParser.Feature.SKIP_EMPTY_LINES, true);
		result.configure(CsvParser.Feature.TRIM_SPACES, true);
		result.configure(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS, true);
		result.configure(CsvParser.Feature.EMPTY_STRING_AS_NULL, false);
		result.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

		return result;
	}

	// Common ancillary methods ...............................................


	private static Pattern linePattern;

	static {
		SerialisationHelper.linePattern = Pattern.compile("^(?<C>([ \t]*#.*))$|^(?<D>.*)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}


	public static <T> List<FileRecord<T>> readLines(final File file, final ObjectReader reader, final boolean ignoreHeader, final Class<T> clazz) {
		assert file != null && file.canRead();
		assert reader != null;
		assert clazz != null;

		List<FileRecord<T>> result;
		Matcher matcher;
		String comment, definition;

		try {
			result = new ArrayList<FileRecord<T>>();

			try (LineIterator iterator = FileUtils.lineIterator(file, "utf-8")) {
				if (ignoreHeader && iterator.hasNext())
					iterator.nextLine();
				while (iterator.hasNext()) {
					String line;
					T object;
					FileRecord<T> record;

					record = new FileRecord<T>();
					line = iterator.nextLine();
					matcher = SerialisationHelper.linePattern.matcher(line);
					if (!matcher.find())
						throw new IllegalArgumentException(String.format("Cannot parse line '%s'.", line));
					comment = matcher.group("C");
					definition = matcher.group("D");

					if (comment != null)
						record.setComment(comment);
					else {
						assert definition != null;
						object = reader.readValue(definition, clazz);
						record.setObject(object);
					}
					result.add(record);
				}
			}
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

}
