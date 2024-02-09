/*
 * DatabaseManager.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.database;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.Session;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Action;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToWriter;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import acme.client.data.AbstractEntity;
import acme.client.helpers.CollectionHelper;
import acme.client.helpers.RandomHelper;
import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.PassThroughException;
import lombok.CustomLog;

@Component
@CustomLog
public class DatabaseManager {

	// Constructors -----------------------------------------------------------

	protected DatabaseManager() {
		this.definitions = new ThreadLocal<DefaultTransactionDefinition>();
		this.statuses = new ThreadLocal<TransactionStatus>();
	}

	// Internal state ---------------------------------------------------------


	@Autowired
	private PlatformTransactionManager					transactionManager;
	@Autowired
	private EntityManager								entityManager;
	private ThreadLocal<DefaultTransactionDefinition>	definitions;
	private ThreadLocal<TransactionStatus>				statuses;


	protected enum SchemaOperation {
		DROP, CREATE, CLEAN;
	}

	// Properties -------------------------------------------------------------


	public boolean isTransactionActive() {
		boolean result;
		TransactionStatus status;

		status = this.statuses.get();
		result = status != null && !status.isCompleted();

		return result;
	}

	public String getTransactionName() {
		assert this.isTransactionActive();

		String result;
		DefaultTransactionDefinition definition;

		definition = this.definitions.get();
		result = definition.getName();

		return result;
	}

	public Session getSession() {
		Session result;
		EntityManager txem;

		txem = this.entityManager.getEntityManagerFactory().createEntityManager();
		result = txem.unwrap(Session.class);
		assert result != null;

		return result;
	}

	// Transaction methods ----------------------------------------------------

	public void startTransaction() {
		assert !this.isTransactionActive();

		DefaultTransactionDefinition definition;
		String name;
		TransactionStatus status;

		name = RandomHelper.nextUUID().toString();
		DatabaseManager.logger.debug("Starting transaction {}.", name);

		definition = new DefaultTransactionDefinition();
		definition.setName(name);
		status = this.transactionManager.getTransaction(definition);

		this.definitions.set(definition);
		this.statuses.set(status);
	}

	public void commitTransaction() {
		assert this.isTransactionActive();

		DefaultTransactionDefinition definition;
		String name;
		TransactionStatus status;

		definition = this.definitions.get();
		name = definition.getName();
		DatabaseManager.logger.debug("Committing transaction {}.", name);

		status = this.statuses.get();
		this.statuses.remove();
		this.definitions.remove();

		this.transactionManager.commit(status);
	}

	public void rollbackTransaction() {
		assert this.isTransactionActive();

		DefaultTransactionDefinition definition;
		String name;
		TransactionStatus status;

		definition = this.definitions.get();
		name = definition.getName();
		DatabaseManager.logger.debug("Rolling transaction {} back.", name);

		status = this.statuses.get();
		this.statuses.remove();
		this.definitions.remove();

		this.transactionManager.rollback(status);
	}

	public void setReadUncommittedIsolationLevel() {
		assert this.isTransactionActive();

		this.executeCommand("set transaction isolation level read uncommitted;");
	}

	public void setReadCommittedIsolationLevel() {
		assert this.isTransactionActive();

		this.executeCommand("set transaction isolation level read committed;");
	}

	// Query methods ----------------------------------------------------------

	public Query createQuery(final String command) {
		assert !StringHelper.isBlank(command);
		assert this.isTransactionActive();

		Query result;

		DatabaseManager.logger.debug("Creating query for '{}'.", command);
		result = this.entityManager.createQuery(command);

		return result;
	}

	public void persist(final AbstractEntity entity) {
		assert entity != null;
		assert this.isTransactionActive();

		DatabaseManager.logger.debug("Persisting entity '{}'.", StringHelper.toIdentity(entity));
		this.entityManager.persist(entity);
	}

	public void persist(final Collection<AbstractEntity> entities) {
		assert !CollectionHelper.someNull(entities);
		assert this.isTransactionActive();

		for (final AbstractEntity entity : entities)
			this.persist(entity);
	}

	public void remove(final AbstractEntity entity) {
		assert entity != null;
		assert this.isTransactionActive();

		DatabaseManager.logger.debug("Removing entity '{}'.", StringHelper.toIdentity(entity));
		this.entityManager.remove(entity);
	}

	public void remove(final Collection<AbstractEntity> entities) {
		assert !CollectionHelper.someNull(entities);
		assert this.isTransactionActive();

		for (final AbstractEntity entity : entities)
			this.remove(entity);
	}

	public void merge(final AbstractEntity entity) {
		assert entity != null;
		assert this.isTransactionActive();

		DatabaseManager.logger.debug("Merging entity '{}'.", StringHelper.toIdentity(entity));
		this.entityManager.merge(entity);
	}

	public void merge(final Collection<AbstractEntity> entities) {
		assert !CollectionHelper.someNull(entities);
		assert this.isTransactionActive();

		for (final AbstractEntity entity : entities)
			this.merge(entity);
	}

	public void clear() {
		assert this.isTransactionActive();

		DatabaseManager.logger.debug("Closing transaction context.");
		this.entityManager.close();
	}

	// Running scripts --------------------------------------------------------

	public void executeScript(final List<String> commands) {
		assert !CollectionHelper.someNull(commands);

		String name;

		try {
			this.startTransaction();
			name = this.getTransactionName();
			try (Session session = this.getSession()) {
				session.doWork(connection -> {
					Statement statement;

					statement = connection.createStatement();
					DatabaseManager.logger.debug("Creating batch for transaction {}.", name);
					for (final String command : commands)
						if (!StringHelper.isBlank(command)) {
							DatabaseManager.logger.debug("Adding '{}' to batch in transaction {}.", command, name);
							statement.addBatch(command);
						}
					DatabaseManager.logger.debug("Submitting batch in transaction {}.", name);
					statement.executeBatch();
				});
			}
			this.commitTransaction();
		} catch (final Throwable oops) {
			if (this.isTransactionActive())
				this.rollbackTransaction();
			throw new PassThroughException(oops);
		}
	}

	// Schema management ------------------------------------------------------

	public void createSchema() {
		Metadata metadata;
		List<String> dropScript, createScript;

		metadata = this.buildMetadataSources();

		DatabaseManager.logger.debug("Dropping existing database schema, if any.");
		dropScript = this.generateScript(metadata, SchemaOperation.DROP);
		this.executeScript(dropScript);

		DatabaseManager.logger.debug("Creating database schema.");
		createScript = this.generateScript(metadata, SchemaOperation.CREATE);
		this.executeScript(createScript);
	}

	public void cleanSchema() {
		Metadata metadata;
		List<String> clearScript;

		metadata = this.buildMetadataSources();

		DatabaseManager.logger.debug("Cleaning database schema.");
		clearScript = this.generateScript(metadata, SchemaOperation.CLEAN);
		this.executeScript(clearScript);
	}

	// Command execution ------------------------------------------------......

	public void executeCommand(final String command) {
		assert !StringHelper.isBlank(command);

		DatabaseManager.logger.debug("Executing command '{}'.", command);
		try (Session session = this.getSession()) {
			session.doWork(connection -> {
				Statement statement;

				statement = connection.createStatement();
				statement.execute(command);
			});
		}
	}

	public int executeUpdate(final String command) {
		assert !StringHelper.isBlank(command);

		int result;
		Query query;

		DatabaseManager.logger.debug("Executing command '{}'.", command);
		query = this.createQuery(command);
		result = query.executeUpdate();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object> executeSelect(final String command) {
		assert !StringHelper.isBlank(command);

		List<Object> result;
		Query query;

		DatabaseManager.logger.debug("Executing command '{}'.", command);
		query = this.createQuery(command);
		result = query.getResultList();

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	protected Metadata buildMetadataSources() {
		Metadata result;
		ServiceRegistry registry;
		MetadataSources sources;
		Metamodel metamodel;
		Collection<EntityType<?>> entities;
		Collection<EmbeddableType<?>> embeddables;

		try (Session session = this.getSession()) {
			registry = this.getRegistry(session);
			sources = new MetadataSources(registry);
			metamodel = this.entityManager.getMetamodel();
			entities = metamodel.getEntities();
			for (final EntityType<?> entity : entities)
				sources.addAnnotatedClass(entity.getJavaType());
			embeddables = metamodel.getEmbeddables();
			for (final EmbeddableType<?> embeddable : embeddables)
				sources.addAnnotatedClass(embeddable.getJavaType());
			result = sources.buildMetadata();
		}

		return result;
	}

	@SuppressWarnings("deprecation")
	protected ServiceRegistry getRegistry(final Session session) {
		assert session != null;

		final ServiceRegistry result;

		result = session.getSessionFactory().getSessionFactory().getServiceRegistry().getParentServiceRegistry();

		return result;
	}

	protected List<String> generateScript(final Metadata metadata, final SchemaOperation operation) {
		assert metadata != null;
		assert operation != null;

		List<String> result;
		String text;
		String[] statements;

		text = null;
		try (OutputStream outputStream = new ByteArrayOutputStream(); //
			Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {

			ScriptTargetOutput target;
			Action ddlAction;
			SchemaExport exporter;

			target = new ScriptTargetOutputToWriter(writer);
			ddlAction = operation.equals(SchemaOperation.DROP) || operation.equals(SchemaOperation.CLEAN) ? Action.DROP : Action.CREATE;

			exporter = new SchemaExport();
			exporter.setHaltOnError(true);
			exporter.setFormat(false);
			exporter.setDelimiter(";");

			exporter.perform(ddlAction, metadata, target);
			text = outputStream.toString();
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}

		switch (operation) {
		case DROP:
			// HINT: the exporter generates ALTER statements without checking if the tables exists!
			// HINT+ Thus, we need to remove those statements explicitly.
			text = text.replaceAll("alter table[^\\r\\n]*[\\r\\n]+", "");
			break;
		case CLEAN:
			// HINT: Neither does the exporter generate clean scripts; so we need to generate a drop
			// HINT+ script and patch it by removing the alter statements and changing the drop
			// HINT+ statements into truncate statements. Hibernate's sequences must be restored, too.
			text = text.replaceAll("alter table[^\\r\\n]*[\\r\\n]+", "");
			text = text.replace("drop table if exists", "truncate table");
			text += "\ninsert into hibernate_sequences(sequence_name, next_val) values (\"default\", 0);\n";
			break;
		case CREATE:
			break;
		}

		statements = text.split(";\\s*[\r\n]+");

		result = new ArrayList<String>();
		result.add("set foreign_key_checks=0;\n");
		Collections.addAll(result, statements);
		result.add("set foreign_key_checks=1;\n");

		return result;
	}

}
