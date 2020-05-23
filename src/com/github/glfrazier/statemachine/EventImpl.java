package com.github.glfrazier.statemachine;

import com.github.glfrazier.Event;

/**
 * A helper class that allows one to easily wrap an object into a StateMachine
 * Event. The name of the event is the payload's toString() result. A null
 * payload will return the empty string.
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
	 * (the output of {@link EventImpl#toString()}) is specified in the
	 * <code>value</code> parameter.
	 * 
	 * @param payload the item resides in the event
	 * @param value   the output of {@link #toString()}&mdash;this is how the state
	 *                machine perceives the event
	 * @see #getPayload()
	 * @see #toString()
	 */
	public EventImpl(T payload, String value) {
		this.payload = payload;
		this.name = value;
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
	 * Returns <code>payload.toString()</code>, or "" if the payload is
	 * <code>null</code>.
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
