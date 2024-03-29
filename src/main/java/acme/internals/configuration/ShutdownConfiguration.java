/*
 * ShutdownConfiguration.java
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

import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Configuration;

import lombok.extern.apachecommons.CommonsLog;

@Configuration
@CommonsLog
public class ShutdownConfiguration {

	// Constructor ------------------------------------------------------------

	protected ShutdownConfiguration() {
	}

	// Business methods -------------------------------------------------------

	@PreDestroy
	public void destroy() {
		ShutdownConfiguration.logger.info("The system is shutting down...");
	}

}
