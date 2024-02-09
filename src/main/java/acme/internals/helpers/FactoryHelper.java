/*
 * FactoryHelper.java
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

import javax.persistence.EntityManager;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import acme.client.helpers.CollectionHelper;
import acme.client.helpers.StringHelper;
import acme.internals.services.AuthenticationService;
import lombok.Getter;

public abstract class FactoryHelper {

	// Constructors -----------------------------------------------------------

	protected FactoryHelper() {
	}

	public static void initialise(final ConfigurableApplicationContext context) {
		assert context != null;
		assert !FactoryHelper.isInitialised();

		FactoryHelper.context = context;
		FactoryHelper.autowiringFactory = context.getAutowireCapableBeanFactory();
		FactoryHelper.messageSource = context.getBean(MessageSource.class);
		FactoryHelper.conversionService = context.getBean(FormattingConversionService.class);
		FactoryHelper.validator = context.getBean(LocalValidatorFactoryBean.class);
		FactoryHelper.passwordEncoder = context.getBean(PasswordEncoder.class);
		FactoryHelper.entityManager = context.getBean(EntityManager.class);
		FactoryHelper.transactionManager = context.getBean(PlatformTransactionManager.class);
		FactoryHelper.authenticationService = context.getBean(AuthenticationService.class);
		FactoryHelper.environment = context.getEnvironment();
	}

	// Properties -------------------------------------------------------------


	@Getter
	private static ConfigurableApplicationContext	context;

	@Getter
	private static AutowireCapableBeanFactory		autowiringFactory;

	@Getter
	private static MessageSource					messageSource;

	@Getter
	private static ConversionService				conversionService;

	@Getter
	private static Validator						validator;

	@Getter
	private static PasswordEncoder					passwordEncoder;

	@Getter
	private static EntityManager					entityManager;

	@Getter
	private static PlatformTransactionManager		transactionManager;

	@Getter
	private static AuthenticationService			authenticationService;

	@Getter
	private static Environment						environment;

	// Business methods -------------------------------------------------------


	public static boolean isInitialised() {
		boolean result;

		result = FactoryHelper.context != null;

		return result;
	}

	public static void autowire(final Iterable<?> objects) {
		assert !CollectionHelper.someNull(objects);
		assert FactoryHelper.isInitialised();

		for (final Object object : objects)
			FactoryHelper.autowire(object);
	}

	public static <T> void autowire(final T object) {
		assert object != null;
		assert FactoryHelper.isInitialised();

		FactoryHelper.autowiringFactory.autowireBean(object);
	}

	public static <T> T getBean(final Class<T> clazz) {
		assert clazz != null;
		assert FactoryHelper.isInitialised();

		T result;

		result = FactoryHelper.context.getBean(clazz);

		return result;
	}

	public static Object getBean(final String name) {
		assert !StringHelper.isBlank(name);
		assert FactoryHelper.isInitialised();

		Object result;

		result = FactoryHelper.context.getBean(name);

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(final String name, final Class<T> clazz) {
		assert !StringHelper.isBlank(name);
		assert clazz != null;
		assert FactoryHelper.isInitialised();

		T result;
		Object bean;

		bean = FactoryHelper.context.getBean(name);
		assert clazz.isAssignableFrom(bean.getClass());
		result = (T) bean;

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T createBean(final Class<T> clazz) {
		assert clazz != null;
		assert FactoryHelper.isInitialised();

		T result;
		Object bean;

		bean = FactoryHelper.autowiringFactory.createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		assert clazz.isAssignableFrom(bean.getClass());
		result = (T) bean;

		return result;
	}

}
