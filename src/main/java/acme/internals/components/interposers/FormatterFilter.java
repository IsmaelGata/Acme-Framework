/*
 * FormatterFilter.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.interposers;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import acme.internals.helpers.ServletHelper;

public class FormatterFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
		assert request != null;
		assert response != null;
		assert filterChain != null;

		byte[] input, output;
		String text;
		Document document;
		ContentCachingResponseWrapper wrapper;

		wrapper = new ContentCachingResponseWrapper(response);
		filterChain.doFilter(request, wrapper);
		input = wrapper.getContentAsByteArray();
		text = new String(input);

		if (!ServletHelper.hasResponseHtml(wrapper))
			output = input;
		else {
			document = Jsoup.parse(text);
			assert document != null;
			document.outputSettings() //
				.charset("utf-8") //
				.indentAmount(4) //
				.prettyPrint(true);
			output = document.html().getBytes();
		}

		response.getOutputStream().write(output);
	}

}
