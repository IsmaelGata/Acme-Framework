/*
 * MessageConfiguration.java
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

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import acme.internals.components.extensions.ExtendedMessageSource;

@Configuration
public class MessageConfiguration implements WebMvcConfigurer {

	// Constructor ------------------------------------------------------------

	protected MessageConfiguration() {
	}

	// Beans ------------------------------------------------------------------

	@Bean
	public MessageSource messageSource() {
		ExtendedMessageSource result;

		result = new ExtendedMessageSource("Views");
		result.setDefaultEncoding("utf-8");
		result.setFallbackToSystemLocale(false);
		result.setUseCodeAsDefaultMessage(true);
		result.setCacheSeconds(5);
		result.setBasenames("/WEB-INF/views/*.i18n", "/WEB-INF/views/**/*.i18n");

		return result;
	}

}
