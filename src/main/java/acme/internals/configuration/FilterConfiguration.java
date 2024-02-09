/*
 * FilterConfiguration.java
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

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import acme.internals.components.extensions.ExtendedSecurityExpressionHandler;
import acme.internals.components.interposers.AuthenticationFilter;
import acme.internals.components.interposers.CsrfHeaderFilter;
import acme.internals.components.interposers.FormatterFilter;
import acme.internals.controllers.RememberMeLogoutHandler;
import acme.internals.services.AuthenticationService;

@Configuration
@EnableWebSecurity
public class FilterConfiguration {

	// Constructor ------------------------------------------------------------

	protected FilterConfiguration() {
	}


	static {
		FilterConfiguration.STRONG_KEY = "\\/3ry-$tr0ng-@uth3nt1kat10n-K3Y!";
	}

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuthenticationService	authenticationService;

	private static String			STRONG_KEY;

	// Beans ------------------------------------------------------------------


	@Bean
	public SecurityFilterChain securityFilterChain(final HttpSecurity security) throws Exception {
		assert security != null;

		SecurityFilterChain result;
		CustomFilterConfigurer configurer;

		configurer = new CustomFilterConfigurer();
		security.apply(configurer);

		security.csrf(Customizer.withDefaults());

		security.authorizeRequests() //
			.anyRequest() //
			.permitAll();

		security.formLogin() //
			.permitAll() //
			.loginPage("/anonymous/system/sign-in") //
			.usernameParameter("username") //
			.passwordParameter("password") //
			.defaultSuccessUrl("/");

		security.logout() //
			.permitAll() //			
			.logoutRequestMatcher(new AntPathRequestMatcher("/authenticated/system/sign-out", "GET")) //
			.logoutSuccessUrl("/any/system/welcome") //
			.invalidateHttpSession(true) //
			.clearAuthentication(true) //
			.deleteCookies("JSESSIONID", "remember") //
			.addLogoutHandler(new RememberMeLogoutHandler());

		security.rememberMe() //
			.key(FilterConfiguration.STRONG_KEY) //
			.rememberMeParameter("remember") //
			.rememberMeCookieName("remember") //
			.tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(15)) //
			.userDetailsService(this.authenticationService);

		result = security.build();

		return result;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder result;

		result = new BCryptPasswordEncoder(5);

		return result;
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider result;

		result = new DaoAuthenticationProvider();
		result.setUserDetailsService(this.authenticationService);
		result.setPasswordEncoder(this.passwordEncoder());

		return result;
	}

	@Bean
	public ExtendedSecurityExpressionHandler webSecurityExpressionHandler() {
		ExtendedSecurityExpressionHandler result;

		result = new ExtendedSecurityExpressionHandler();

		return result;
	}

	// Ancillary classes ------------------------------------------------------


	public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {

		@Override
		public void configure(final HttpSecurity security) throws Exception {
			assert security != null;

			AuthenticationManager authenticationManager;
			AuthenticationFilter signInFilter, signOutFilter;
			FormatterFilter responseFormatterFilter;
			CsrfHeaderFilter csrfHeaderFilter;

			// HINT: the manager can only be fetched here, not in the securityFilterChain method above :/
			authenticationManager = security.getSharedObject(AuthenticationManager.class);

			signInFilter = new AuthenticationFilter("POST", "/anonymous/system/sign-in", authenticationManager);
			security.addFilter(signInFilter);

			signOutFilter = new AuthenticationFilter("GET", "/authenticated/system/sign-out", authenticationManager);
			security.addFilter(signOutFilter);

			csrfHeaderFilter = new CsrfHeaderFilter();
			security.addFilterAfter(csrfHeaderFilter, CsrfFilter.class);

			responseFormatterFilter = new FormatterFilter();
			security.addFilterAfter(responseFormatterFilter, SwitchUserFilter.class);
		}

	}

}
