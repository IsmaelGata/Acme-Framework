/*
 * PanicService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.services;

import org.springframework.stereotype.Service;

import acme.client.data.AbstractObject;
import acme.client.data.AbstractRole;
import acme.client.services.AbstractService;

@Service
public class PanicService<R extends AbstractRole, O extends AbstractObject> extends AbstractService<R, O> {

}
