/*
 * ValidationConfiguration.java
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

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import acme.internals.components.clocks.DefaultClockProvider;
import acme.internals.components.extensions.ExtendedMessageSource;

@Configuration
public class ValidationConfiguration implements WebMvcConfigurer {

	// Constructor ------------------------------------------------------------

	protected ValidationConfiguration() {
	}

	// WebMvcConfigurer -------------------------------------------------------

	@Override
	public Validator getValidator() {
		return this.validator();
	}

	// Beans ------------------------------------------------------------------

	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean result;
		ExtendedMessageSource messageSource;

		messageSource = new ExtendedMessageSource("Validator");
		messageSource.setDefaultEncoding("utf-8");
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setUseCodeAsDefaultMessage(false);
		messageSource.setCacheSeconds(5);
		messageSource.setBasenames("/WEB-INF/views/*.i18n", "/WEB-INF/views/**/*.i18n");

		result = new LocalValidatorFactoryBean() {
			// HINT: this is ugly, but it is the only way to change the clock used for validation :/

			@Override
			protected void postProcessConfiguration(final javax.validation.Configuration<?> configuration) {
				configuration.clockProvider(DefaultClockProvider.INSTANCE);
				configuration.addProperty(BaseHibernateValidatorConfiguration.TEMPORAL_VALIDATION_TOLERANCE, "1");
			}
		};
		result.setValidationMessageSource(messageSource);

		return result;
	}

}
