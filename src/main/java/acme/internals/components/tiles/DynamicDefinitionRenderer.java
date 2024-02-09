/*
 * DynamicDefinitionRenderer.java
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

import java.io.IOException;

import org.apache.tiles.Attribute;
import org.apache.tiles.Definition;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.request.Request;
import org.apache.tiles.request.render.Renderer;

import acme.client.helpers.StringHelper;

public class DynamicDefinitionRenderer implements Renderer {

	// Internal state ---------------------------------------------------------

	private final TilesContainer container;

	// Constructors -----------------------------------------------------------


	public DynamicDefinitionRenderer(final TilesContainer container) {
		assert container != null;

		this.container = container;
	}

	// Renderer interface -----------------------------------------------------

	@Override
	public void render(final String path, final Request request) throws IOException {
		assert !StringHelper.isBlank(path);
		assert request != null;

		Attribute body;
		Definition definition;

		body = new Attribute();
		body.setValue(String.format("/WEB-INF/views/%s.jsp", path));

		definition = this.container.getDefinition("/master", request);
		definition.putAttribute("body", body);

		this.container.render(definition, request);
	}

	@Override
	public boolean isRenderable(final String path, final Request request) {
		assert !StringHelper.isBlank(path);
		assert request != null;

		return true;
	}

}
