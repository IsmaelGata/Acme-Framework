/*
 * ConversionConfiguration.java
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

import java.util.Date;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import acme.internals.components.formatters.DatatypeFormatter;
import acme.internals.components.formatters.LocalisedDateFormatter;
import acme.internals.components.formatters.LocalisedDoubleFormatter;
import acme.internals.components.formatters.LocalisedMoneyFormatter;
import acme.internals.components.formatters.SelectChoicesFormatter;

@Configuration
public class ConversionConfiguration implements WebMvcConfigurer {

	// Constructor ------------------------------------------------------------

	protected ConversionConfiguration() {
	}

	// Beans ------------------------------------------------------------------

	@Override
	public void addFormatters(final FormatterRegistry registry) {
		assert registry != null;

		LocalisedDateFormatter dateFormatter;
		LocalisedMoneyFormatter moneyFormatter;
		LocalisedDoubleFormatter doubleFormatter;
		DatatypeFormatter datatypeFormatter;
		SelectChoicesFormatter selectChoicesFormatter;

		registry.removeConvertible(String.class, Date.class);
		registry.removeConvertible(Date.class, String.class);
		dateFormatter = new LocalisedDateFormatter();
		registry.addFormatter(dateFormatter);

		registry.removeConvertible(String.class, Double.class);
		registry.removeConvertible(Double.class, String.class);
		doubleFormatter = new LocalisedDoubleFormatter();
		registry.addFormatter(doubleFormatter);

		moneyFormatter = new LocalisedMoneyFormatter();
		registry.addFormatter(moneyFormatter);

		selectChoicesFormatter = new SelectChoicesFormatter();
		registry.addFormatter(selectChoicesFormatter);

		// HINT: the generic datatype formatter must be added at the last position.

		datatypeFormatter = new DatatypeFormatter();
		registry.addFormatter(datatypeFormatter);
	}

}
