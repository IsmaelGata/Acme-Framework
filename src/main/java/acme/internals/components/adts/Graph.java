/*
 * Graph.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.adts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acme.client.helpers.CollectionHelper;

public class Graph<T> {

	// Internal state ---------------------------------------------------------

	private final Map<T, Set<T>>	adjacency;
	private List<T>					order;
	private Set<List<T>>			cycles;

	// Constructors -----------------------------------------------------------


	public Graph() {
		this.adjacency = new LinkedHashMap<T, Set<T>>();
		this.order = null;
		this.cycles = null;
	}

	// Properties -------------------------------------------------------------

	public boolean isClosed() {
		boolean result;

		result = this.order != null && this.cycles != null;

		return result;
	}

	public List<T> getOrder() {
		assert this.isClosed();

		return this.order;
	}

	public Set<List<T>> getCycles() {
		assert this.isClosed();

		return this.cycles;
	}

	// Business methods -------------------------------------------------------

	public void close() {
		Set<T> processed;
		Deque<T> path;

		this.order = new ArrayList<T>();
		this.cycles = new LinkedHashSet<List<T>>();
		processed = new LinkedHashSet<T>();
		for (final T vertex : this.adjacency.keySet())
			if (!processed.contains(vertex)) {
				path = new ArrayDeque<T>();
				this.computeTopologicalSort(vertex, processed, this.order, this.cycles, path);
			}
	}

	public boolean hasVertex(final T vertex) {
		assert vertex != null;

		boolean result;

		result = this.adjacency.containsKey(vertex);

		return result;
	}

	public void addVertex(final T vertex) {
		assert vertex != null && !this.hasVertex(vertex);
		assert !this.isClosed();

		Set<T> neighbours;

		neighbours = new LinkedHashSet<T>();
		this.adjacency.put(vertex, neighbours);
	}

	public boolean hasEdge(final T vertex1, final T vertex2) {
		assert vertex1 != null;
		assert vertex2 != null;

		boolean result;
		Set<T> neighbours;

		result = this.hasVertex(vertex1) && this.hasVertex(vertex2);
		if (result) {
			neighbours = this.adjacency.get(vertex1);
			result = neighbours.contains(vertex2);
		}

		return result;
	}

	public void addEdge(final T vertex1, final T vertex2) {
		assert vertex1 != null && this.hasVertex(vertex1);
		assert vertex2 != null && this.hasVertex(vertex2);
		assert !this.hasEdge(vertex1, vertex2);
		assert !this.isClosed();

		Set<T> neighbours;

		neighbours = this.adjacency.get(vertex1);
		neighbours.add(vertex2);
	}

	// Ancillary methods ------------------------------------------------------

	protected void computeTopologicalSort(final T vertex, final Set<T> processed, final List<T> order, final Set<List<T>> cycles, final Deque<T> path) {
		assert vertex != null;
		assert !CollectionHelper.someNull(processed);
		assert !CollectionHelper.someNull(order);
		assert !CollectionHelper.someNull(cycles);
		assert !CollectionHelper.someNull(path);

		Iterator<T> iterator;
		List<T> cycle;

		processed.add(vertex);
		path.push(vertex);
		iterator = this.adjacency.get(vertex).iterator();
		while (iterator.hasNext()) {
			T next;

			next = iterator.next();
			if (path.contains(next)) {
				cycle = new ArrayList<T>(path);
				cycle.add(next);
				cycles.add(cycle);
			}
			if (!processed.contains(next))
				this.computeTopologicalSort(next, processed, order, cycles, path);
		}
		path.pop();
		order.add(vertex);
	}

}
