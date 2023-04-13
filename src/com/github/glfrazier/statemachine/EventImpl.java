package com.github.glfrazier.statemachine;

import com.github.glfrazier.event.Event;

/**
 * A helper class that allows one to easily wrap an object into a StateMachine
 * Event.
 * 
 * @author Greg Frazier
 *
 */
public class EventImpl<T> implements Event {

	private final T payload;

	private final String name;

	/**
	 * Construct an Event that holds the specified payload. The Event's string value
	 * (the output of {@link EventImpl#toString()}) is
	 * <code>payload.toString()</code>.
	 * 
	 * @param payload the item resides in the event
	 * @see #getPayload()
	 */
	public EventImpl(T payload) {
		this.payload = payload;
		this.name = null;
	}

	/**
	 * Construct an Event that holds the specified payload. The Event's string value
	 * (the output of {@link EventImpl#toString()}) is the <code>name</code>
	 * parameter.
	 * 
	 * @param payload the item resides in the event
	 * @param name    the output of {@link #toString()}&mdash; {@link #hashCode()}
	 *                and {@link #equals(Object)} use the String representation for
	 *                comparison.
	 * @see #getPayload()
	 * @see #toString()
	 */
	public EventImpl(T payload, String name) {
		this.payload = payload;
		this.name = name;
	}

	/**
	 * Construct an EventImpl with the specified payload, but whose toString()
	 * output equals <code>e.toString()</code>.
	 * 
	 * @param payload
	 * @param e
	 */
	public EventImpl(T payload, Event e) {
		this.payload = payload;
		this.name = e.toString();
	}

	/**
	 * Extract the payload from the Event.
	 * 
	 * @return the payload
	 */
	public T getPayload() {
		return payload;
	}

	/**
	 * @return <code>this.toString().hashCode()</code>
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * If <code>o</code> is <code>null</code> or is not an event, return
	 * <code>false</code>. If <code>o</code> is an event, return true if the
	 * <code>toString()</code> methods of <code>this</code> and <code>o</code>
	 * return equal strings.
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Event)) {
			return false;
		}
		return toString().equals(o.toString());
	}

	/**
	 * Returns the name of the event.
	 * 
	 * @see
	 */
	@Override
	public String toString() {
		if (name != null) {
			return name;
		}
		if (payload == null) {
			return "";
		}
		return payload.toString();
	}
}
