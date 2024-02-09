/*
 * DynamicTilesView.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.tiles;

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.Attribute;
import org.apache.tiles.Definition;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.access.TilesAccess;
import org.apache.tiles.request.AbstractRequest;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.Request;
import org.apache.tiles.request.render.Renderer;
import org.apache.tiles.request.servlet.ServletRequest;
import org.apache.tiles.request.servlet.ServletUtil;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.tiles3.TilesView;
import org.springframework.web.util.WebUtils;

import acme.client.helpers.StringHelper;

public class DynamicTilesView extends TilesView {

	// Constructors -----------------------------------------------------------

	public DynamicTilesView() {
		this.renderer = null;
		this.exposeJstlAttributes = true;
		this.alwaysInclude = false;
	}

	// Internal state ---------------------------------------------------------


	private Renderer	renderer;
	private boolean		exposeJstlAttributes;
	private boolean		alwaysInclude;

	// Properties -------------------------------------------------------------


	public Renderer getRenderer() {
		return this.renderer;
	}

	@Override
	public void setRenderer(final Renderer renderer) {
		assert renderer != null;

		this.renderer = renderer;
	}

	protected boolean isExposeJstlAttributes() {
		return this.exposeJstlAttributes;
	}

	@Override
	protected void setExposeJstlAttributes(final boolean exposeJstlAttributes) {
		this.exposeJstlAttributes = exposeJstlAttributes;
	}

	@Override
	public void setAlwaysInclude(final boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	public boolean isAlwaysInclude() {
		return this.alwaysInclude;
	}

	// AbstractUrlBasedView interface -----------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		ServletContext servletContext;
		TilesContainer container;
		ApplicationContext applicationContext;

		super.afterPropertiesSet();
		servletContext = this.getServletContext();
		assert servletContext != null;
		applicationContext = ServletUtil.getApplicationContext(servletContext);
		assert applicationContext != null;
		container = TilesAccess.getContainer(applicationContext);
		assert container != null;
		this.renderer = new DynamicDefinitionRenderer(container);
	}

	@Override
	public boolean checkResource(final Locale locale) throws Exception {
		assert locale != null;
		assert this.renderer != null;

		boolean result;
		RequestAttributes requestAttributes;
		HttpServletRequest servletRequest;
		ServletContext servletContext;
		ApplicationContext applicationContext;
		Request request;

		requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes)
			servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		else
			servletRequest = null;
		servletContext = this.getServletContext();
		assert servletContext != null;
		applicationContext = ServletUtil.getApplicationContext(servletContext);
		assert applicationContext != null;
		request = this.createTilesRequest(applicationContext, servletRequest, null, locale);

		result = this.renderer.isRenderable(super.getUrl(), request);

		return result;
	}

	// AbstractView interface -------------------------------------------------

	@Override
	protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		assert model != null;
		assert request != null;
		assert response != null;
		assert this.renderer != null;

		ServletContext servletContext;
		ApplicationContext context;
		TilesContainer container;
		ApplicationContext applicationContext;
		Request tilesRequest;
		String viewName;
		Attribute body;
		Definition definition;

		servletContext = this.getServletContext();
		assert servletContext != null : "There is not a servlet context available.";
		context = ServletUtil.getApplicationContext(servletContext);
		container = TilesAccess.getContainer(context);
		assert container != null : "There is not a TilesConfigurer in your application context.";
		applicationContext = ServletUtil.getApplicationContext(servletContext);

		this.exposeModelAsRequestAttributes(model, request);
		if (this.exposeJstlAttributes)
			JstlUtils.exposeLocalizationContext(new RequestContext(request, this.getServletContext()));
		if (this.alwaysInclude)
			request.setAttribute(AbstractRequest.FORCE_INCLUDE_ATTRIBUTE_NAME, true);
		tilesRequest = this.createTilesRequest(applicationContext, request, response, null);

		JstlUtils.exposeLocalizationContext(new RequestContext(request, servletContext));
		// HINT: must not expose forward request attributes for servlet servers above 2.5
		if (!response.isCommitted() && servletContext.getMajorVersion() == 2 && servletContext.getMinorVersion() < 5)
			this.exposeForwardRequestAttributes(request);

		definition = container.getDefinition("/master", tilesRequest);
		assert definition != null : "Could not locate view '/master'. Very likely the Acme Framework is not properly linked!";

		viewName = String.format("/WEB-INF/views/%s.jsp", super.getUrl());
		body = new Attribute();
		body.setValue(viewName);
		definition.putAttribute("body", body);

		container.render(definition, tilesRequest);
	}

	// Ancillary methods ------------------------------------------------------

	protected Request createTilesRequest(final ApplicationContext applicationContext, final HttpServletRequest request, final HttpServletResponse response, final Locale locale) {
		assert applicationContext != null;
		assert request != null;
		// HINT: response can be null
		// HINT: locale can be null

		Request result;
		Locale actualLocale;

		actualLocale = locale != null ? locale : RequestContextUtils.getLocale(request);
		result = new ServletRequest(applicationContext, request, response) {

			@Override
			public Locale getRequestLocale() {
				return actualLocale;
			}
		};

		return result;
	}

	protected void exposeForwardRequestAttributes(final HttpServletRequest request) {
		assert request != null;

		this.exposeRequestAttributeIfNotPresent(request, WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, request.getRequestURI());
		this.exposeRequestAttributeIfNotPresent(request, WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE, request.getContextPath());
		this.exposeRequestAttributeIfNotPresent(request, WebUtils.FORWARD_SERVLET_PATH_ATTRIBUTE, request.getServletPath());
		this.exposeRequestAttributeIfNotPresent(request, WebUtils.FORWARD_PATH_INFO_ATTRIBUTE, request.getPathInfo());
		this.exposeRequestAttributeIfNotPresent(request, WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE, request.getQueryString());
	}

	protected void exposeRequestAttributeIfNotPresent(final HttpServletRequest request, final String name, final Object object) {
		assert request != null;
		assert !StringHelper.isBlank(name);
		// HINT: object can be null

		if (request.getAttribute(name) == null)
			request.setAttribute(name, object);
	}

}
