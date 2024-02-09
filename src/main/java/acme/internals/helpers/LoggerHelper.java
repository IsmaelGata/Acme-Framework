/*
 * LoggerHelper.java
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

import java.time.LocalDate;
import java.time.LocalTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import acme.client.helpers.RandomHelper;
import acme.client.helpers.StringHelper;
import acme.client.testing.Oracle;
import acme.internals.helpers.SerialisationHelper.Format;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import lombok.CustomLog;

@CustomLog
public abstract class LoggerHelper {

	// Constructors -----------------------------------------------------------

	protected LoggerHelper() {
	}

	// Business methods -------------------------------------------------------

	public static boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
		assert request != null;
		assert response != null;
		assert handler != null;

		String id, message;

		id = RandomHelper.nextUUID().toString();
		request.setAttribute("acme.request-id", id);
		request.setAttribute("acme.timestamps.pre-handle", System.nanoTime());

		message = LoggerHelper.buildLogMessage(Format.JSON, request, response, handler, null);
		LoggerHelper.logger.debug(">> PRE-HANDLE {}", message);

		return true;
	}

	public static void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView) {
		assert request != null;
		assert response != null;
		assert handler != null;
		// HINT: modelAndView can be null

		String message;

		request.setAttribute("acme.timestamps.post-handle", System.nanoTime());

		message = LoggerHelper.buildLogMessage(Format.JSON, request, response, handler, null);
		LoggerHelper.logger.debug(">> POST-HANDLE {}", message);
	}

	public static void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Throwable oops) {
		assert request != null;
		assert response != null;
		assert handler != null;
		// HINT: oops can be null

		String jsonMessage, csvMessage;

		request.setAttribute("acme.timestamps.after-completion", System.nanoTime());

		jsonMessage = LoggerHelper.buildLogMessage(Format.JSON, request, response, handler, null);
		LoggerHelper.logger.debug(">> AFTER-COMPLETION {}", jsonMessage);

		csvMessage = LoggerHelper.buildLogMessage(Format.CSV, request, response, handler, null);
		TraceLoggerHelper.log("{}", csvMessage);
	}

	public static String formatEvent(final ILoggingEvent event, final String format) {
		assert event != null;
		assert !StringHelper.isBlank(format);

		String result;
		String timestamp, level, method, description, exception;

		timestamp = String.format("%s %s", LocalDate.now(), LocalTime.now());
		level = String.format("%-5s", event.getLevel());
		method = String.format("%s@%s", event.getLoggerName(), event.getThreadName());
		description = LoggerHelper.extractDescription(event);
		exception = LoggerHelper.extractException(event);

		result = format.replace("!T", timestamp). //
			replace("!L", level). //
			replace("!M", method). //
			replace("!D", description). //
			replace("!E", exception);
		result += System.lineSeparator();

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	private static String extractDescription(final ILoggingEvent event) {
		assert event != null;

		String result;
		String message;

		message = event.getFormattedMessage();
		result = StringHelper.isBlank(message) ? "" : ThrowableHelper.formatText(message, "\\\\n");

		return result;
	}

	private static String extractException(final ILoggingEvent event) {
		assert event != null;

		String result;
		StringBuilder buffer;
		IThrowableProxy iterator;
		String separator, title, description, paragraph;

		separator = "";
		buffer = new StringBuilder();
		iterator = event.getThrowableProxy();
		while (iterator != null) {
			title = iterator.getStackTraceElementProxyArray()[0].toString();
			description = iterator.getMessage();
			description = description != null ? description : iterator.getClassName();
			paragraph = ThrowableHelper.formatSection(title, description, " ");
			buffer.append(separator);
			buffer.append(paragraph);
			iterator = iterator.getCause();
			separator = " ";
		}
		result = buffer.toString();

		return result;
	}

	private static String buildLogMessage(final Format format, final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Throwable oops) {
		assert format != null;
		assert request != null;
		assert response != null;
		assert handler != null;
		// HINT: exception can be null

		String result;
		Oracle oracle;

		oracle = Oracle.from(request, response, handler, oops);
		result = SerialisationHelper.write(format, oracle);

		return result;
	}

}
