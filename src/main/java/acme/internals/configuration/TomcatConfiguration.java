/*
 * TomcatConfiguration.java
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

import java.io.File;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TomcatConfiguration implements WebMvcConfigurer {

	// Constructor ------------------------------------------------------------

	protected TomcatConfiguration() {
	}

	// Beans ------------------------------------------------------------------

	@Bean
	public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
		WebServerFactoryCustomizer<ConfigurableWebServerFactory> result;

		result = factory -> {
			assert factory instanceof TomcatServletWebServerFactory;

			TomcatServletWebServerFactory tomcat;
			File root;

			tomcat = (TomcatServletWebServerFactory) factory;
			root = new File("./target/classes");
			tomcat.setDocumentRoot(root);
		};

		return result;
	}

}
