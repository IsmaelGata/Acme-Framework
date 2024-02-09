/*
 * InterceptorConfiguration.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.configuration;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import acme.internals.components.interposers.DebugInterceptor;
import acme.internals.components.interposers.LoggerInterceptor;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

	// Constructor ------------------------------------------------------------

	protected InterceptorConfiguration() {
	}

	// WebMvcConfigurer interface ---------------------------------------------

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		assert registry != null;

		DebugInterceptor debugInterceptor;
		LoggerInterceptor loggerInterceptor;
		LocaleChangeInterceptor localeInterceptor;

		debugInterceptor = new DebugInterceptor();
		registry.addInterceptor(debugInterceptor);

		loggerInterceptor = new LoggerInterceptor();
		registry.addInterceptor(loggerInterceptor);

		localeInterceptor = new LocaleChangeInterceptor();
		localeInterceptor.setParamName("locale");
		registry.addInterceptor(localeInterceptor);
	}

	@Bean
	public LocaleContextResolver localeResolver() {
		CookieLocaleResolver result;

		result = new CookieLocaleResolver();
		result.setDefaultLocale(Locale.ENGLISH);
		result.setCookieName("locale");
		result.setCookieMaxAge((int) TimeUnit.DAYS.toSeconds(15));

		return result;
	}

}
