/*
 * DynamicTilesUrlBasedViewResolver.java
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

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import acme.client.helpers.StringHelper;

public class DynamicTilesUrlBasedViewResolver extends UrlBasedViewResolver {

	// UrlBasedViewResolver interface -----------------------------------------

	@Override
	protected AbstractUrlBasedView buildView(final String viewName) throws Exception {
		assert !StringHelper.isBlank(viewName);

		AbstractUrlBasedView view;

		view = super.buildView(viewName);

		return view;
	}

}
