/*
 * Oracle.java
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import acme.client.helpers.StringHelper;
import acme.internals.helpers.ServletHelper;

@JsonPropertyOrder({
	"requestId", "requestMethod", "requestPath", "requestQuery", "requestPayload", // 
	"responseStatus", "responseContentType", "responsePayload", "responseOops", // 
	"preHandleTimestamp", "postHandleTimestamp", "afterCompletionTimestamp" //
})
public class Oracle {

	// Constructors -----------------------------------------------------------

	public static Oracle from(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Throwable oops) {
		assert request != null;
		assert response != null;
		assert handler != null;
		// HINT: exception can be null

		Oracle result;
		String requestId, requestMethod, requestPath, requestQuery, requestPayload;
		String responseStatus, responseContentType, responsePayload, responseOops;
		String preHandleTimestamp, postHandleTimestamp, afterCompletionTimestamp;

		requestId = String.valueOf(request.getAttribute("acme.request-id"));

		requestMethod = ServletHelper.getRequestMethod(request);
		requestPath = ServletHelper.getRequestPath(request, false);
		requestQuery = ServletHelper.getRequestQuery(request);
		requestPayload = ServletHelper.getRequestPayload(request);

		responseStatus = String.valueOf(ServletHelper.getResponseStatus(response));
		responseContentType = ServletHelper.getResponseContentType(response);
		responsePayload = ServletHelper.getResponsePayload(request, response);
		responseOops = ServletHelper.getResponseOops(request, response, oops);

		{
			// HINT: POST /anonymous/system/sign-in and GET /authenticated/system/sign-out returns status  
			// HINT+ 302, but it is not properly recorded in the response, which requires this patch.

			if (requestMethod.equals("POST") && requestPath.equals("/anonymous/system/sign-in"))
				responseStatus = "302";
			if (requestMethod.equals("GET") && requestPath.equals("/authenticated/system/sign-out"))
				responseStatus = "302";
		}

		preHandleTimestamp = String.valueOf(request.getAttribute("acme.timestamps.pre-handle"));
		postHandleTimestamp = String.valueOf(request.getAttribute("acme.timestamps.post-handle"));
		afterCompletionTimestamp = String.valueOf(request.getAttribute("acme.timestamps.after-completion"));

		result = new Oracle();

		result.setRequestId(requestId);

		result.setRequestMethod(requestMethod);
		result.setRequestPath(requestPath);
		result.setRequestQuery(requestQuery);
		result.setRequestPayload(requestPayload);

		result.setResponseStatus(responseStatus);
		result.setResponseContentType(responseContentType);
		result.setResponsePayload(responsePayload);
		result.setResponseOops(responseOops);

		result.setPreHandleTimestamp(preHandleTimestamp);
		result.setPostHandleTimestamp(postHandleTimestamp);
		result.setAfterCompletionTimestamp(afterCompletionTimestamp);

		return result;
	}

	// Internal state ---------------------------------------------------------


	private String	requestId;

	private String	requestMethod;

	private String	requestPath;

	private String	requestQuery;

	private String	requestPayload;

	private String	responseStatus;

	private String	responseContentType;

	private String	ResponsePayload;

	private String	responseOops;

	private String	preHandleTimestamp;

	private String	postHandleTimestamp;

	private String	afterCompletionTimestamp;

	// Properties -------------------------------------------------------------


	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(final String requestId) {
		assert !StringHelper.isBlank(requestId);

		this.requestId = requestId;
	}

	public String getRequestMethod() {
		return this.requestMethod;
	}

	public void setRequestMethod(final String method) {
		assert StringHelper.anyOf(method, "GET|POST");

		this.requestMethod = method;
	}

	public String getRequestPath() {
		return this.requestPath;
	}

	public void setRequestPath(final String requestPath) {
		assert !StringHelper.isBlank(requestPath);

		this.requestPath = requestPath;
	}

	public String getRequestQuery() {
		return this.requestQuery;
	}

	public void setRequestQuery(final String requestQuery) {
		assert requestQuery != null;

		this.requestQuery = requestQuery;
	}

	public String getRequestPayload() {
		return this.requestPayload;
	}

	public void setRequestPayload(final String requestPayload) {
		assert requestPayload != null;

		this.requestPayload = requestPayload;
	}

	public String getResponseStatus() {
		return this.responseStatus;
	}

	public void setResponseStatus(final String responseStatus) {
		assert !StringHelper.isBlank(responseStatus);

		this.responseStatus = responseStatus;
	}

	public String getResponseContentType() {
		return this.responseContentType;
	}

	public void setResponseContentType(final String responseContentType) {
		assert responseContentType != null;

		this.responseContentType = responseContentType;
	}

	public String getResponsePayload() {
		return this.ResponsePayload;
	}

	public void setResponsePayload(final String ResponsePayload) {
		assert ResponsePayload != null;

		this.ResponsePayload = ResponsePayload;
	}

	public String getResponseOops() {
		return this.responseOops;
	}

	public void setResponseOops(final String oops) {
		// HINT: oops can be null

		if (oops == null)
			this.responseOops = "";
		else
			this.responseOops = oops;
	}

	public String getPreHandleTimestamp() {
		return this.preHandleTimestamp;
	}

	public void setPreHandleTimestamp(final String preHandleTimestamp) {
		assert !StringHelper.isBlank(preHandleTimestamp);

		this.preHandleTimestamp = preHandleTimestamp;
	}

	public String getPostHandleTimestamp() {
		return this.postHandleTimestamp;
	}

	public void setPostHandleTimestamp(final String postHandleTimestamp) {
		assert !StringHelper.isBlank(postHandleTimestamp);

		this.postHandleTimestamp = postHandleTimestamp;
	}

	public String getAfterCompletionTimestamp() {
		return this.afterCompletionTimestamp;
	}

	public void setAfterCompletionTimestamp(final String afterCompletionTimestamp) {
		assert !StringHelper.isBlank(afterCompletionTimestamp);

		this.afterCompletionTimestamp = afterCompletionTimestamp;
	}

}
