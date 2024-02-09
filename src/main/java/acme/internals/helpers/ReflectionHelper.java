/*
 * ReflectionHelper.java
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

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import acme.client.data.AbstractDatatype;
import acme.client.data.AbstractEntity;
import acme.client.data.models.Dataset;
import acme.client.helpers.StringHelper;
import acme.internals.components.beans.CustomBindingErrorProcessor;
import acme.internals.components.beans.LocalisedDataBinder;

public abstract class ReflectionHelper {

	// Internal state ---------------------------------------------------------

	private static Map<Class<? extends Annotation>, Map<String, Class<?>>> cache;

	static {
		ReflectionHelper.cache = new LinkedHashMap<Class<? extends Annotation>, Map<String, Class<?>>>();
	}

	// Constructors -----------------------------------------------------------


	protected ReflectionHelper() {
	}

	// Business methods -------------------------------------------------------

	public static <T> T instantiate(final Class<T> clazz) {
		assert clazz != null;

		T result;

		result = BeanUtils.instantiateClass(clazz);

		return result;
	}

	public static <T> T instantiate(final String clazzName, final Class<T> base) {
		assert !StringHelper.isBlank(clazzName);
		// HINT: base can be null
		assert ReflectionHelper.existsClass(clazzName, base);

		T result;
		Class<T> clazz;

		clazz = ReflectionHelper.getClass(clazzName, base);
		assert clazz != null;
		result = BeanUtils.instantiateClass(clazz);

		return result;
	}

	public static BindingResult bind(final Object target, final Dataset dataset) {
		assert target != null;
		assert dataset != null;

		BindingResult result;
		Set<String> properties;
		PropertyValues binding;
		ConversionService conversionService;
		CustomBindingErrorProcessor errorProcessor;
		WebDataBinder binder;
		Dataset camelDataset;

		camelDataset = new Dataset();
		for (final Entry<String, Object> entry : dataset.entrySet()) {
			String property, camelName;
			Object object;

			property = entry.getKey();
			object = entry.getValue();
			camelName = ReflectionHelper.computeCamelName(property, false);
			camelDataset.put(camelName, object);
		}
		properties = camelDataset.keySet();
		binding = new MutablePropertyValues(camelDataset);

		conversionService = FactoryHelper.getConversionService();
		errorProcessor = new CustomBindingErrorProcessor();

		binder = new LocalisedDataBinder(target);
		binder.setConversionService(conversionService);
		binder.setBindingErrorProcessor(errorProcessor);
		binder.setAllowedFields(properties.toArray(new String[] {}));
		binder.setAutoGrowNestedPaths(true);
		binder.bind(binding);

		result = binder.getBindingResult();

		return result;
	}

	public static Dataset unbind(final Object target, final List<String> properties) {
		assert target != null;
		assert !StringHelper.someBlank(properties);

		Dataset result;
		BeanWrapper wrapper;
		Object value;

		result = new Dataset();
		wrapper = new DirectFieldAccessFallbackBeanWrapper(target);
		wrapper.setAutoGrowNestedPaths(true);
		for (final String property : properties) {
			String camelName;

			camelName = ReflectionHelper.computeCamelName(property, false);
			assert wrapper.isReadableProperty(camelName) : String.format("Cannot unbind property '%s'", camelName);
			value = wrapper.getPropertyValue(camelName);
			result.put(camelName, value);
		}

		return result;
	}

	public static boolean isPrimitive(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> clazz;

		clazz = ReflectionHelper.getEffectiveClass(object);
		result = clazz == null || //
			String.class.isAssignableFrom(clazz) || //
			Number.class.isAssignableFrom(clazz) || //
			Character.class.isAssignableFrom(clazz) || //
			Boolean.class.isAssignableFrom(clazz) || //
			java.util.Date.class.isAssignableFrom(clazz) || //
			java.sql.Date.class.isAssignableFrom(clazz) || //
			Timestamp.class.isAssignableFrom(clazz);

		return result;
	}

	public static boolean isObjectArray(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> rootClazz, componentClazz;

		if (object == null)
			result = false;
		else {
			rootClazz = ReflectionHelper.getEffectiveClass(object);
			componentClazz = rootClazz.getComponentType();
			result = rootClazz.getName().startsWith("[L") && !ReflectionHelper.isPrimitive(componentClazz);
		}

		return result;
	}

	public static boolean isPrimitiveArray(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> rootClazz, componentClazz;

		if (object == null)
			result = false;
		else {
			rootClazz = ReflectionHelper.getEffectiveClass(object);
			componentClazz = rootClazz.getComponentType();
			result = rootClazz.getName().startsWith("[") && ReflectionHelper.isPrimitive(componentClazz);
		}

		return result;
	}

	public static boolean isArray(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> rootClazz;

		if (object == null)
			result = false;
		else {
			rootClazz = ReflectionHelper.getEffectiveClass(object);
			result = rootClazz.getName().startsWith("[");
		}

		return result;
	}

	public static boolean isEnum(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> clazz;

		if (object == null)
			result = false;
		else {
			clazz = ReflectionHelper.getEffectiveClass(object);
			result = Enum.class.isAssignableFrom(clazz);
		}

		return result;
	}

	public static boolean isCollection(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> clazz;

		if (object == null)
			result = false;
		else {
			clazz = ReflectionHelper.getEffectiveClass(object);
			result = Collection.class.isAssignableFrom(clazz);
		}

		return result;
	}

	public static boolean isMap(final Object object) {
		// HINT: object can be null

		boolean result;
		Class<?> clazz;

		if (object == null)
			result = false;
		else {
			clazz = ReflectionHelper.getEffectiveClass(object);
			result = Map.class.isAssignableFrom(clazz);
		}

		return result;
	}

	public static List<Field> getDeclaredFields(final Class<?> clazz, final Object object, final boolean omitInstrumented) {
		assert clazz != null;
		// HINT: object can be null

		List<Field> result;
		Field[] fields;

		result = new ArrayList<Field>();
		fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			String name;

			name = field.getName();

			if (field.trySetAccessible() || Modifier.isPublic(field.getModifiers()))
				if (!omitInstrumented || !name.equals("serialVersionUID") && !name.equals("logger")) // NOSONAR
					result.add(field);
		}

		return result;
	}

	public static Map<String, TypeDescriptor> getProperties(final Object target) {
		assert target != null && !(target instanceof TypeDescriptor);

		Map<String, TypeDescriptor> result;

		result = ReflectionHelper.getProperties(target, false);

		return result;

	}

	public static Map<String, TypeDescriptor> getProperties(final Object target, final boolean kebapNames) {
		assert target != null && !(target instanceof TypeDescriptor);
		// HINT: nameStrategy can be null

		Map<String, TypeDescriptor> result;
		DirectFieldAccessFallbackBeanWrapper wrapper;
		PropertyDescriptor[] descriptors;
		String key;

		wrapper = new DirectFieldAccessFallbackBeanWrapper(target);
		wrapper.setAutoGrowNestedPaths(true);
		result = new LinkedHashMap<String, TypeDescriptor>();
		descriptors = wrapper.getPropertyDescriptors();
		for (final PropertyDescriptor descriptor : descriptors) {
			String propertyName;
			TypeDescriptor typeDescriptor;

			propertyName = descriptor.getName();
			typeDescriptor = wrapper.getPropertyTypeDescriptor(propertyName);
			if (typeDescriptor != null && //
				!propertyName.equals("class") && !propertyName.equals("transient") && //
				!typeDescriptor.hasAnnotation(java.beans.Transient.class) && //
				!typeDescriptor.hasAnnotation(javax.persistence.Transient.class) && //
				!typeDescriptor.hasAnnotation(org.springframework.data.annotation.Transient.class)) {
				if (kebapNames)
					key = ReflectionHelper.computeKebapName(propertyName);
				else
					key = propertyName;
				result.put(key, typeDescriptor);
			}
		}

		return result;
	}

	public static boolean hasProperty(final Object target, final String property) {
		assert target != null;
		assert !StringHelper.isBlank(property);

		boolean result;
		BeanWrapper wrapper;

		wrapper = new DirectFieldAccessFallbackBeanWrapper(target);
		wrapper.setAutoGrowNestedPaths(true);
		result = wrapper.isReadableProperty(property) && wrapper.isWritableProperty(property);

		return result;
	}

	public static Object getProperty(final Object target, final String property) {
		assert target != null;
		assert !StringHelper.isBlank(property);
		assert ReflectionHelper.hasProperty(target, property);

		Object result;
		BeanWrapper wrapper;

		wrapper = new DirectFieldAccessFallbackBeanWrapper(target);
		wrapper.setAutoGrowNestedPaths(true);
		result = wrapper.getPropertyValue(property);

		return result;
	}

	public static void setProperty(final Object target, final String property, final Object object) {
		assert target != null;
		assert !StringHelper.isBlank(property) && ReflectionHelper.hasProperty(target, property);
		// HINT: object can be null

		String realProperty;
		BeanWrapper wrapper;
		ConversionService conversionService;

		if (property.startsWith("GET") || property.startsWith("set"))
			realProperty = property.substring(3);
		else if (property.startsWith("is"))
			realProperty = property.substring(2);
		else
			realProperty = property;
		assert !StringHelper.isBlank(realProperty);
		realProperty = StringHelper.smallInitial(realProperty);

		conversionService = FactoryHelper.getConversionService();

		wrapper = new DirectFieldAccessFallbackBeanWrapper(target);
		wrapper.setAutoGrowNestedPaths(true);
		wrapper.setConversionService(conversionService);
		try {
			wrapper.setPropertyValue(realProperty, object);
		} catch (final NotWritablePropertyException oops) {
			wrapper.setPropertyValue(property, object);
		}
	}

	public static Class<?> getPropertyClazz(final Object target, final String property) {
		assert target != null;
		assert !StringHelper.isBlank(property);
		assert ReflectionHelper.hasProperty(target, property);

		Class<?> result;
		BeanWrapper wrapper;

		wrapper = new DirectFieldAccessFallbackBeanWrapper(target);
		wrapper.setAutoGrowNestedPaths(true);
		result = wrapper.getPropertyType(property);

		return result;
	}

	public static List<Class<?>> getAllClazzes(final Object object, final Class<?> rootClazz) {
		assert object != null;
		assert rootClazz != null;

		ArrayList<Class<?>> result;
		Class<?> clazz;

		result = new ArrayList<Class<?>>();
		clazz = object.getClass();
		while (clazz != null && rootClazz.isAssignableFrom(clazz)) {
			result.add(clazz);
			clazz = clazz.getSuperclass();
		}

		return result;
	}

	public static boolean supports(final Object target, final Class<?> clazz) {
		assert target != null;
		assert clazz != null;

		boolean result;

		// TODO: think of something that takes the converters into account.
		result = clazz.isAssignableFrom(target.getClass());

		return result;
	}

	public static boolean isAssignable(final Class<?> targetClazz, final Class<?> sourceClazz) {
		assert sourceClazz != null;
		assert targetClazz != null;

		boolean result;

		// TODO: think of something that takes the converters into account.
		result = targetClazz.isAssignableFrom(sourceClazz);

		return result;
	}

	public static boolean isAssignable(final Class<?> targetClazz, final Object sourceObject) {
		assert targetClazz != null;
		// HINT: sourceObject can be null

		boolean result;

		// TODO: think of something that takes the converters into account.
		result = sourceObject == null || targetClazz.isAssignableFrom(sourceObject.getClass()); // NOSONAR

		return result;
	}

	public static boolean existsClass(final String fullName, final Class<?> base) {
		assert !StringHelper.isBlank(fullName);
		// HINT: base can be null

		boolean result;
		Class<?> clazz;

		try {
			clazz = Class.forName(fullName);
			result = base == null || base.isAssignableFrom(clazz);
		} catch (final ClassNotFoundException e) {
			result = false;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(final String fullName, final Class<T> base) {
		assert !StringHelper.isBlank(fullName);
		// HINT: base can be null
		assert ReflectionHelper.existsClass(fullName, base);

		Class<T> result;

		try {
			result = (Class<T>) Class.forName(fullName);
		} catch (final ClassNotFoundException e) {
			result = null;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static Class<AbstractEntity> findEntityClazz(final String simpleName) {
		assert !StringHelper.isBlank(simpleName);

		Class<?> result;

		result = ReflectionHelper.findClazzByAnnotation(simpleName, Entity.class);
		assert result == null || AbstractEntity.class.isAssignableFrom(result) : String.format("Clazz '%s' is not an entity clazz", simpleName);

		return (Class<AbstractEntity>) result;
	}

	@SuppressWarnings("unchecked")
	public static Class<AbstractDatatype> findDatatypeClazz(final String simpleName) {
		assert !StringHelper.isBlank(simpleName);

		Class<?> result;

		result = ReflectionHelper.findClazzByAnnotation(simpleName, Embeddable.class);
		assert result == null || AbstractDatatype.class.isAssignableFrom(result) : String.format("Clazz '%s' is not a datatype clazz", simpleName);

		return (Class<AbstractDatatype>) result;
	}

	public static String computeCamelName(final String name, final boolean capitaliseFirst) {
		assert !StringHelper.isBlank(name);

		String result;
		StringBuilder buffer;
		boolean capitalise;

		buffer = new StringBuilder();
		capitalise = capitaliseFirst;
		for (int index = 0; index < name.length(); index++) {
			char current;

			current = name.charAt(index);
			if (capitalise)
				current = Character.toUpperCase(current);
			if (current != '_' && current != '-')
				buffer.append(current);
			if (current == '.')
				capitalise = capitaliseFirst;
			else
				capitalise = current == '_' || current == '-';
		}
		result = buffer.toString();

		return result;
	}

	public static String computeKebapName(final String name) {
		assert !StringHelper.isBlank(name);

		String result;
		StringBuilder buffer;

		buffer = new StringBuilder();
		for (int index = 0; index < name.length(); index++) {
			char current;

			current = name.charAt(index);
			if (index > 0 && Character.isUpperCase(current))
				buffer.append("-");
			current = Character.toLowerCase(current);
			buffer.append(current);
		}
		result = buffer.toString();

		return result;
	}

	public static List<String> findClassPathEntries() {
		List<String> result;
		String[] components;

		result = new ArrayList<String>();
		// HINT: calling EnvironmentHelper::getRequiredProperty is not safe here.  If Spring
		// HINT+ could not load properly, then the environment is not accessible. Using
		// HINT+ System::getProperty will surely work well in every context.
		components = System.getProperty("java.class.path", "null").split(File.pathSeparator);
		Collections.addAll(result, components);

		return result;
	}

	public static List<BeanDefinition> findEntities() {
		List<BeanDefinition> result;
		ClassPathScanningCandidateComponentProvider scanner;
		AnnotationTypeFilter filter;
		Set<BeanDefinition> definitions;

		scanner = new ClassPathScanningCandidateComponentProvider(false);
		filter = new AnnotationTypeFilter(Entity.class);
		scanner.addIncludeFilter(filter);
		definitions = scanner.findCandidateComponents("acme");

		result = new ArrayList<BeanDefinition>();
		result.addAll(definitions);

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	private static Class<?> getEffectiveClass(final Object object) {
		Class<?> result;

		if (object == null)
			result = null;
		else if (object instanceof TypeDescriptor)  // NOSONAR
			result = ((TypeDescriptor) object).getObjectType();
		else if (object instanceof Class<?>)
			result = (Class<?>) object;
		else
			result = object.getClass();

		return result;
	}

	private static Class<?> findClazzByAnnotation(final String simpleName, final Class<? extends Annotation> annotation) {
		assert !StringHelper.isBlank(simpleName);
		// HINT: annotation can be null

		Class<?> result;
		Map<String, Class<?>> subcache;

		ReflectionHelper.fillCache(annotation);
		subcache = ReflectionHelper.cache.get(annotation);

		if (subcache.containsKey(simpleName))
			result = subcache.get(simpleName);
		else
			result = null;

		return result;
	}

	private static void fillCache(final Class<? extends Annotation> annotation) {
		// HINT: annotation can be null

		Map<String, Class<?>> subcache;
		ClassPathScanningCandidateComponentProvider scanner;
		Set<BeanDefinition> definitions;
		AnnotationTypeFilter filter;

		if (ReflectionHelper.cache.containsKey(annotation))
			subcache = ReflectionHelper.cache.get(annotation);
		else {
			subcache = new LinkedHashMap<String, Class<?>>();
			ReflectionHelper.cache.put(annotation, subcache);
		}

		if (subcache.isEmpty()) {
			scanner = new ClassPathScanningCandidateComponentProvider(annotation == null);
			if (annotation != null) {
				filter = new AnnotationTypeFilter(annotation);
				scanner.addIncludeFilter(filter);
			}
			definitions = scanner.findCandidateComponents("acme");
			for (final BeanDefinition definition : definitions) {
				String clazzName, simpleName;
				Class<?> clazz;

				clazzName = definition.getBeanClassName();
				clazz = ReflectionHelper.getClass(clazzName, null);
				if (clazz != null) {
					simpleName = clazz.getSimpleName();
					subcache.put(simpleName, clazz);
				}
			}
		}
	}

}
