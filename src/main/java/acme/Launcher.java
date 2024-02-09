/*
 * Launcher.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme;

import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import acme.client.helpers.CollectionHelper;
import acme.client.helpers.MomentHelper;
import acme.client.helpers.RandomHelper;
import acme.internals.components.database.DatabaseInquirer;
import acme.internals.components.database.DatabasePopulator;
import acme.internals.helpers.FactoryHelper;
import acme.internals.helpers.ReflectionHelper;
import acme.internals.helpers.ThrowableHelper;
import lombok.CustomLog;

@SpringBootApplication
@CustomLog
public class Launcher extends SpringBootServletInitializer {

	// Command-line entry point -----------------------------------------------

	public static void main(final String... argv) {
		assert !CollectionHelper.someNull(argv);

		CommandLine commandLine;
		ConfigurableApplicationContext context;
		String message;

		context = null;
		try {
			System.setProperty("java.awt.headless", "false");
			commandLine = Launcher.parseArguments(argv);
			Launcher.setProfiles(commandLine);
			context = Launcher.startSpring();
			Launcher.logContextInformation();
			FactoryHelper.initialise(context);
			MomentHelper.initialise();
			RandomHelper.initialise();
			Launcher.launchWorker(commandLine, context);
		} catch (final Throwable oops) {
			message = ThrowableHelper.toString(oops);
			Launcher.logger.error(message);
			Launcher.logger.debug("The log might provide further details.");
			Launcher.exit(context);
		}
	}

	// Application server entry point ------------------------------------------

	// HINT: the support to get started within an application server was discontinued.
	// HINT+ The code is preserved here just in case it is continued in future, but not 
	// HINT+ guaranteed to work well at all. 

	@Override
	public void onStartup(final ServletContext servletContext) throws ServletException {
		assert servletContext != null;

		Object root;
		ConfigurableApplicationContext context;

		System.setProperty("java.awt.headless", "true");
		System.setProperty("spring.profiles.active", "production,runner");
		super.onStartup(servletContext);
		Launcher.logContextInformation();
		root = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		assert root instanceof ConfigurableApplicationContext;
		context = (ConfigurableApplicationContext) root;
		FactoryHelper.initialise(context);
		MomentHelper.initialise();
		RandomHelper.initialise();
		Launcher.logger.debug("Starting servlet...");
	}

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
		assert builder != null;

		SpringApplicationBuilder result;

		result = builder.sources(Launcher.class);

		return result;
	}

	// Business methods -------------------------------------------------------

	public static void reset(final boolean createSchema, final boolean sampleData) {
		DatabasePopulator populator;

		Launcher.logger.info( //
			"Resetting application ({}, populate {}, reset clock, reset random generator).", //
			createSchema ? "create schema" : "keep schema", sampleData ? "initial data" : "initial and sample data");

		Launcher.logger.debug("Initialising clock.");
		MomentHelper.reset();

		Launcher.logger.debug("Initialising random generator.");
		RandomHelper.reset();

		Launcher.logger.debug("Populating database.");
		populator = FactoryHelper.getBean(DatabasePopulator.class);
		populator.populate(createSchema, sampleData);

		Launcher.logger.debug("Resetting clock.");
		MomentHelper.reset();

		Launcher.logger.debug("Resetting random generator.");
		RandomHelper.reset();
	}

	public static void exit(final ApplicationContext context) {
		// HINT: context can be null

		int status;

		status = context == null ? 1 : SpringApplication.exit(context);
		Launcher.logger.info("Exiting application with status {}.", status);
		if (status != 0)
			Launcher.logger.info("The log might provide further details.");
		if (context == null) {
			Launcher.sanityPause();
			System.exit(status);
		}
	}

	public static void detach(final ApplicationContext context) {
		// HINT: context can be null

		Launcher.sanityPause();
		Launcher.logger.info("Application {} is now running.", context.getApplicationName());
	}

	public static void runInquirer() {
		DatabaseInquirer databaseInquirer;

		databaseInquirer = FactoryHelper.getBean(DatabaseInquirer.class);
		databaseInquirer.run();
	}

	// Ancillary methods ------------------------------------------------------

	private static CommandLine parseArguments(final String[] argv) {
		assert argv != null;

		CommandLine result;
		String[] validPlatforms, validLaunchers;
		Options options;
		CommandLineParser parser;
		String platform, launcher;

		validPlatforms = new String[] {
			"development", "testing", "production"
		};
		validLaunchers = new String[] {
			"populator#initial", "populator#sample", "inquirer", "runner", "recorder", "tester",
		};

		options = new Options();
		options.addOption("p", "platform", true, "sets an execution platform");
		options.addOption("l", "launcher", true, "executes a launcher");

		try {
			parser = new DefaultParser();
			result = parser.parse(options, argv);

			platform = (String) result.getParsedOptionValue("p");
			Assert.state(ArrayUtils.contains(validPlatforms, platform), "Wrong platform");

			launcher = (String) result.getParsedOptionValue("l");
			Assert.state(ArrayUtils.contains(validLaunchers, launcher), "Wrong launcher");
		} catch (final Throwable oops) {
			result = null;
			Launcher.showUsage();
		}

		return result;
	}

	private static ConfigurableApplicationContext startSpring() {
		ConfigurableApplicationContext result;
		String[] argv;

		argv = new String[] {
			"-Dspring.config.name=application,launcher,platform"
		};
		result = SpringApplication.run(Launcher.class, argv);

		return result;
	}

	private static void setProfiles(final CommandLine commandLine) {
		assert commandLine != null;

		Locale locale;
		String platformProfile, launcherProfile;
		String profiles;

		locale = Locale.of("en");
		Locale.setDefault(locale);

		platformProfile = commandLine.getOptionValue("platform");
		launcherProfile = commandLine.getOptionValue("launcher").replaceFirst("#.*$", "");

		profiles = String.format("%s,%s", platformProfile, launcherProfile);
		System.setProperty("spring.config.name", "application,platform,launcher");
		System.setProperty("spring.profiles.active", profiles);
	}

	private static void launchWorker(final CommandLine commandLine, final ConfigurableApplicationContext context) {
		assert commandLine != null;
		assert context != null;

		String platform, launcher;

		platform = commandLine.getOptionValue("platform");
		launcher = commandLine.getOptionValue("launcher");
		Launcher.logger.info("Launching {} in your {} platform.", launcher, platform);
		switch (launcher) {
		case "populator#initial":
			Launcher.reset(true, false);
			Launcher.exit(context);
			break;
		case "populator#sample":
			Launcher.reset(true, true);
			Launcher.exit(context);
			break;
		case "inquirer":
			Launcher.runInquirer();
			Launcher.exit(context);
			break;
		case "runner":
			Launcher.detach(context);
			break;
		case "recorder":
			Launcher.reset(true, true);
			Launcher.detach(context);
			break;
		case "tester":
			Launcher.detach(context);
			break;
		default:
			assert false;
		}
	}

	private static void logContextInformation() {
		List<String> classPathEntries;
		List<BeanDefinition> entities;

		classPathEntries = ReflectionHelper.findClassPathEntries();
		for (final String entry : classPathEntries)
			Launcher.logger.trace("Found classpath entry '{}'", entry);

		entities = ReflectionHelper.findEntities();
		for (final BeanDefinition entity : entities)
			Launcher.logger.trace("Found entity class '{}", entity.getBeanClassName());
	}

	private static void showUsage() {
		System.err.println("");
		System.err.println("Usage: launcher (--platform <p>)? (--launcher <l>)?");
		System.err.println("");
		System.err.println("Platforms:");
		System.err.println("development       development platform (default)");
		System.err.println("production        production platform");
		System.err.println("testing           testing platform");
		System.err.println("");
		System.err.println("Launchers:");
		System.err.println("populator#initial populates the database with initial data");
		System.err.println("populator#sample  populates the database with sample data");
		System.err.println("inquirer          opens a shell to query the database");
		System.err.println("runner            runs application in normal mode (default)");
		System.err.println("recorder          runs application in recording mode");
		System.err.println("tester            runs application in testing mode");
		System.err.println("");
		Launcher.exit(null);
	}

	private static void sanityPause() {
		try {
			// HINT: note that calling MomentHelper::sleep is not safe here.  If Spring fails to 
			// HINT+ load properly, then calling that method would result in an additional spurious
			// HINT+ exception.  Allowing the current thread to sleep for a while is way safer.
			Thread.sleep(2500);
		} catch (final InterruptedException oops) {
			;
		}
	}

}
