/*
 * TraceAnalyser.java
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import acme.client.helpers.StringHelper;
import acme.internals.components.adts.FileRecord;
import acme.internals.helpers.SerialisationHelper;
import acme.internals.helpers.SerialisationHelper.Format;
import acme.internals.helpers.ServletHelper;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.CustomLog;

@CustomLog
public class TraceAnalyser {

	// Constructors -----------------------------------------------------------

	public TraceAnalyser() {
		this.valueMap = new LinkedHashMap<String, List<Map<String, String>>>();
		this.headerMap = new LinkedHashMap<String, Set<String>>();
	}

	// Internal state ---------------------------------------------------------


	private Map<String, List<Map<String, String>>>	valueMap;  // HINT: feature -> [parameter -> value]	
	private Map<String, Set<String>>				headerMap;  // HINT: feature -> [parameter]

	// Business methods -------------------------------------------------------


	public void analyse(final File traceFile) {
		assert traceFile != null && traceFile.isFile() && traceFile.canRead();

		List<FileRecord<Oracle>> script;
		Oracle oracle;
		String path, feature, requestFeature, responseFeature;
		Map<String, String> requestPayload, responsePayload;

		TraceAnalyser.logger.info("Analysing trace '{}'.", traceFile.getAbsolutePath());
		script = SerialisationHelper.read(Format.CSV, traceFile, Oracle.class);
		for (final FileRecord<Oracle> record : script) {
			oracle = record.getObject();
			if (oracle != null && ServletHelper.isStandardFeature(oracle.getRequestPath())) {
				path = oracle.getRequestPath();
				feature = String.format("%s %s/%s/%s",  //
					oracle.getRequestMethod(), //
					ServletHelper.getFeatureRole(path), //
					ServletHelper.getFeatureObject(path), //
					ServletHelper.getFeatureCommand(path) //
				);

				requestFeature = String.format("%s (REQUEST)", feature);
				requestPayload = ServletHelper.decodeQuery(oracle.getRequestQuery());
				requestPayload.putAll(ServletHelper.decodeQuery(oracle.getRequestPayload()));
				this.updateMap(requestFeature, requestPayload);

				responseFeature = String.format("%s (RESPONSE)", feature);
				responsePayload = ServletHelper.decodeQuery(oracle.getResponsePayload());
				this.updateMap(responseFeature, responsePayload);
			}
		}
	}


	private static CWC_LongestLine CWC = new CWC_LongestLine();


	public void showReport() {
		AsciiTable tableau;
		Set<String> parameters;

		for (final Entry<String, List<Map<String, String>>> featureEntry : this.valueMap.entrySet()) {
			TraceAnalyser.logger.info("** {}", featureEntry.getKey());

			assert this.headerMap.containsKey(featureEntry.getKey());
			parameters = this.headerMap.get(featureEntry.getKey());

			if (parameters.isEmpty())
				TraceAnalyser.logger.info("[ ]");
			else {
				tableau = new AsciiTable();
				tableau.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);
				tableau.getRenderer().setCWC(TraceAnalyser.CWC);

				tableau.addRule();
				{
					tableau.addRow(parameters);
				}
				tableau.addRule();
				{
					List<String> buffer;
					Map<String, String> values;
					String name;

					for (int i = 0; i < featureEntry.getValue().size(); i++) {
						buffer = new ArrayList<String>();
						values = featureEntry.getValue().get(i);
						for (String parameter : parameters) {
							name = parameter.replaceAll("\\[\\d+\\]", "");
							if (values.containsKey(name))
								buffer.add(values.get(name));
							else
								buffer.add("");
						}
						tableau.addRow(buffer);
					}
				}
				tableau.addRule();

				TraceAnalyser.logger.info("{}", tableau.render());
			}
		}

	}

	// Ancillary methods ------------------------------------------------------

	protected void updateMap(final String feature, final Map<String, String> payload) {
		assert !StringHelper.isBlank(feature);
		assert payload != null;

		List<Map<String, String>> payloads;
		Set<String> parameters;

		payloads = this.valueMap.computeIfAbsent(feature, k -> new ArrayList<Map<String, String>>());
		payloads.add(payload);

		parameters = this.headerMap.computeIfAbsent(feature, k -> new TreeSet<String>());
		for (final Entry<String, String> entry : payload.entrySet()) {
			String key;

			key = entry.getKey().replaceAll("\\[\\d+\\]", "");
			parameters.add(key);
		}
	}
}
