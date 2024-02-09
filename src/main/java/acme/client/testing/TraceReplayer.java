/*
 * TraceReplayer.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.testing;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import acme.Launcher;
import acme.client.helpers.ConversionHelper;
import acme.client.helpers.StringHelper;
import acme.internals.components.adts.FileRecord;
import acme.internals.helpers.SerialisationHelper;
import acme.internals.helpers.ServletHelper;
import acme.internals.testing.AcmeBrowser;
import acme.internals.testing.TesterResult;
import lombok.CustomLog;

@CustomLog
public class TraceReplayer {

	// Constructors -----------------------------------------------------------

	public TraceReplayer() {
		this.browser = new AcmeBrowser();
	}

	// Internal state ---------------------------------------------------------


	private final AcmeBrowser browser;

	// Business methods -------------------------------------------------------


	public void run(final File traceFile) {
		assert traceFile != null && traceFile.isFile() && traceFile.canRead();

		List<FileRecord<Oracle>> script;
		TesterResult testerResult;
		String explanation;
		boolean needsReset;

		TraceReplayer.logger.info("Replaying {}.", traceFile.getPath());
		Launcher.reset(true, true);
		this.browser.reset();
		needsReset = false;
		script = SerialisationHelper.read(SerialisationHelper.Format.CSV, traceFile, Oracle.class);
		for (final FileRecord<Oracle> record : script) {
			String comment;
			Oracle oracle;

			comment = record.getComment();
			oracle = record.getObject();
			if (comment != null) {
				if (comment.startsWith("# RESET")) {
					if (needsReset) {
						Launcher.reset(false, true);
						this.browser.reset();
					}
					needsReset = true;
				}
				TraceReplayer.logger.info("{}", comment);
			} else {
				assert oracle != null;
				testerResult = this.check(oracle);
				if (!StringHelper.isBlank(testerResult.getOops())) {
					explanation = String.format( //
						"FAILED %s %s (request-id=\"%s\", input=\"%s\"): %s", //
						testerResult.getMethod(), testerResult.getPath(), testerResult.getRequestId(), testerResult.getInput(), testerResult.getOops());
					TraceReplayer.logger.info("{}", explanation);
				}
			}
		}
	}

	// Ancillary methods ------------------------------------------------------

	protected TesterResult check(final Oracle oracle) {
		assert oracle != null;

		TesterResult result;
		Map<String, String> requestPayload, expectedPayload, actualPayload;
		Response response;
		long startTime, endTime;
		String html, payload;
		Document document;

		result = new TesterResult();
		result.setRequestId(oracle.getRequestId());
		result.setMethod(oracle.getRequestMethod());
		result.setPath(String.format("%s%s%s", oracle.getRequestPath(), oracle.getRequestQuery().isBlank() ? "" : "?", oracle.getRequestQuery()));
		result.setInput(oracle.getRequestPayload());

		requestPayload = ServletHelper.decodeQuery(oracle.getRequestPayload());

		startTime = System.nanoTime();
		response = this.browser.request(oracle.getRequestMethod(), oracle.getRequestPath(), oracle.getRequestQuery(), requestPayload);
		endTime = System.nanoTime();

		result.setElapsedTime(endTime - startTime);

		this.stateSame(result, "status", response.statusCode(), oracle.getResponseStatus());
		this.stateSame(result, "content-type", response.contentType() == null ? "" : response.contentType(), oracle.getResponseContentType());

		if (!StringHelper.startsWith(response.contentType(), "text/html", true))
			result.setOutput("");
		else {
			html = response.body();
			document = Jsoup.parse(html);
			payload = document.select("meta[name='payload']").attr("content");
			result.setOutput(payload);

			actualPayload = ServletHelper.decodeQuery(payload);
			expectedPayload = ServletHelper.decodeQuery(oracle.getResponsePayload());
			this.stateSame(result, "payload", actualPayload, expectedPayload);
		}

		return result;
	}

	protected void stateSame(final TesterResult testerResult, final String subject, final Object actual, final Object expected) {
		assert testerResult != null;
		assert !StringHelper.isBlank(subject);
		// HINT: actual can be null
		// HINT: expected can be null

		boolean same;
		Object conversion;
		String oops;

		if (expected == null && actual == null)
			same = true;
		else if (expected == null && actual != null || expected != null && actual == null)  // NOSONAR
			same = false;
		else if (!ConversionHelper.canConvert(expected, actual.getClass()))
			same = false;
		else {
			conversion = ConversionHelper.convert(expected, actual.getClass());
			same = actual.equals(conversion);
		}

		if (!same) {
			oops = String.format("Expected '%s' to be '%s', but got '%s'.", subject, expected, actual);
			testerResult.accumulateOops(oops);
		}
	}

}
