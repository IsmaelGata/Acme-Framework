/*
 * TilesConfiguration.java
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

import acme.internals.components.tiles.DynamicTilesView;

@Configuration
public class TilesConfiguration implements WebMvcConfigurer {

	// Constructor ------------------------------------------------------------

	protected TilesConfiguration() {
	}

	// Beans ------------------------------------------------------------------

	@Bean
	public TilesConfigurer tilesConfigurer() {
		TilesConfigurer result;

		result = new TilesConfigurer();
		result.setCheckRefresh(true);
		result.setDefinitions("classpath:/WEB-INF/views/**/tiles.xml");

		return result;
	}

	@Bean
	public ViewResolver viewResolver() {
		TilesViewResolver result;

		result = new TilesViewResolver();
		result.setViewClass(DynamicTilesView.class);

		return result;
	}

}
