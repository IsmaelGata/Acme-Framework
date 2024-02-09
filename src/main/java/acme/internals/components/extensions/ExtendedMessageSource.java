/*
 * ExtendedMessageSource.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.extensions;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import acme.client.helpers.CollectionHelper;
import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.PassThroughException;
import lombok.CustomLog;

@CustomLog
public class ExtendedMessageSource extends AbstractResourceBasedMessageSource implements ResourceLoaderAware {

	// Constructors -----------------------------------------------------------

	public ExtendedMessageSource(final String name) {
		assert !StringHelper.isBlank(name);

		this.cache = new HashMap<Locale, Properties>();
		this.timestamps = new HashMap<URI, Long>();
		this.propertiesPersister = new DefaultPropertiesPersister();
		this.resolver = new PathMatchingResourcePatternResolver();
		this.nextRefresh = -1;
		this.name = name;
	}

	// Internal state ---------------------------------------------------------


	private Map<Locale, Properties>				cache;  	// HINT: locale -> Properties
	private Map<URI, Long>						timestamps; // HINT: resource -> timestamp 
	private PropertiesPersister					propertiesPersister;
	private PathMatchingResourcePatternResolver	resolver;
	private long								nextRefresh;
	private String								name;

	// Properties -------------------------------------------------------------


	public String getName() {
		return this.name;
	}

	// Object interface -------------------------------------------------------

	@Override
	public String toString() {
		String result;

		result = String.format("%s: basenames = %s", this.getClass().getName(), this.getBasenameSet());

		return result;
	}

	// ResourceLoaderAware interface ------------------------------------------

	@Override
	public void setResourceLoader(final ResourceLoader resourceLoader) {
		assert resourceLoader != null;

		// HINT: we use a resource loader that can deal with ant-like patterns.
		// HINT+ so, we can safely ignore the parameter.
	}

	// AbstractResourceBasedMessageSource interface ---------------------------

	@SuppressWarnings("deprecation")
	@Override
	protected String resolveCodeWithoutArguments(final String code, final Locale locale) {
		assert !StringHelper.isBlank(code);
		assert locale != null;

		String result;
		Locale defaultLocale;

		result = this.fetch(code, locale);
		defaultLocale = Locale.getDefault();
		if (result == null && super.isFallbackToSystemLocale() && !locale.equals(defaultLocale))
			result = this.fetch(code, defaultLocale);
		if (result == null && super.isUseCodeAsDefaultMessage())
			result = code;

		return result;
	}

	@Override
	protected MessageFormat resolveCode(final String code, final Locale locale) {
		assert !StringHelper.isBlank(code);
		assert locale != null;

		MessageFormat result;
		String message;

		message = this.resolveCodeWithoutArguments(code, locale);
		if (message == null)
			result = null;
		else
			result = new MessageFormat(message, locale);

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	private String fetch(final String code, final Locale locale) {
		assert !StringHelper.isBlank(code);
		assert locale != null;

		String result;
		Properties properties;

		this.refreshSource(locale);
		properties = this.cache.get(locale);
		result = properties.getProperty(code);

		return result;
	}

	protected boolean mustRefresh() {
		boolean result;

		result = System.currentTimeMillis() + super.getCacheMillis() > this.nextRefresh;

		return result;
	}

	protected void refreshSource(final Locale locale) {
		assert locale != null;

		List<Resource> resources;
		Properties properties;
		boolean mustRebuild;

		properties = this.cache.get(locale);
		if (properties == null)
			this.rebuildSource(locale);
		else if (this.mustRefresh()) {
			resources = this.computeI18nResources(locale);
			mustRebuild = this.mustRebuild(resources, locale);
			if (mustRebuild)
				this.rebuildSource(resources, locale);
		}
		this.nextRefresh = System.currentTimeMillis() + super.getCacheMillis();
	}

	protected void rebuildSource(final Locale locale) {
		assert locale != null;

		List<Resource> resources;

		resources = this.computeI18nResources(locale);
		this.rebuildSource(resources, locale);
	}

	protected void rebuildSource(final List<Resource> resources, final Locale locale) {
		assert !CollectionHelper.someNull(resources);
		assert locale != null;

		Properties properties;

		ExtendedMessageSource.logger.trace("Message source '{}' is reloading i18n resources for locale '{}'.", this.getName(), locale.getDisplayName());
		properties = new Properties();
		for (Resource resource : resources)
			try ( //
				InputStream stream = resource.getInputStream(); //
				InputStreamReader reader = new InputStreamReader(stream, super.getDefaultEncoding()) //
			) {
				ExtendedMessageSource.logger.trace("Loading i18n resource '{}'", resource.getURI());
				this.propertiesPersister.load(properties, reader);
				this.timestamps.put(resource.getURI(), System.currentTimeMillis());
			} catch (Throwable oops) {
				throw new PassThroughException(oops);
			}
		this.cache.put(locale, properties);
	}

	protected boolean mustRebuild(final List<Resource> resources, final Locale locale) {
		assert !CollectionHelper.someNull(resources);
		assert locale != null;

		boolean result;
		Iterator<Resource> iterator;

		try {
			result = false;
			iterator = resources.iterator();
			while (!result && iterator.hasNext()) {
				Resource resource;
				URI path;
				long lastModified, deadline;

				resource = iterator.next();
				path = resource.getURI();
				lastModified = resource.lastModified();
				if (this.timestamps.containsKey(path))
					deadline = this.timestamps.get(path) + this.getCacheMillis();
				else
					deadline = -1;

				result = lastModified > deadline;
			}
		} catch (Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	protected List<Resource> computeI18nResources(final Locale locale) {
		assert locale != null;

		List<Resource> result;
		List<String> localisedBasenames;
		Resource[] resources;

		try {
			result = new ArrayList<Resource>();
			for (final String basename : this.getBasenameSet()) {
				localisedBasenames = this.computeLocalisedBasenames(basename, locale);
				for (final String localisedBasename : localisedBasenames) {
					resources = this.resolver.getResources(localisedBasename);
					Collections.addAll(result, resources);
				}
			}
		} catch (Throwable oops) {
			throw new PassThroughException(oops);
		}

		return result;
	}

	protected List<String> computeLocalisedBasenames(final String basename, final Locale locale) {
		assert !StringHelper.isBlank(basename);
		assert locale != null;

		List<String> result;
		List<String> suffixes;
		String basePath, localisedPath;

		basePath = basename;
		if (basePath.endsWith(".i18n"))
			basePath = basePath.replace(".i18n", "");

		result = new ArrayList<String>();
		suffixes = this.computeSuffixes(locale);
		for (final String suffix : suffixes) {
			localisedPath = String.format("%s%s.i18n", basePath, suffix);
			result.add(localisedPath);
		}

		return result;
	}

	protected List<String> computeSuffixes(final Locale locale) {
		assert locale != null;

		List<String> result;
		String language, country, variant;
		StringBuilder builder;

		result = new ArrayList<String>();

		language = locale.getLanguage();
		country = locale.getCountry();
		variant = locale.getVariant();

		builder = new StringBuilder();

		if (!language.isEmpty()) {
			builder.append(String.format("-%s", language));
			result.add(0, builder.toString());
		}

		if (!country.isEmpty()) {
			builder.append(String.format("-%s", country));
			result.add(0, builder.toString());
		}

		if (!variant.isEmpty()) {
			builder.append(String.format("-%s", variant));
			result.add(0, builder.toString());
		}

		return result;
	}

}
