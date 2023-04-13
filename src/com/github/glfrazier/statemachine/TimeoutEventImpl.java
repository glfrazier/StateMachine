package com.github.glfrazier.statemachine;

/**
 * A helper class that allows one to easily wrap an object into a StateMachine
 * Timed Event. The name of the event is the payload's toString() result. A null
 * payload will return the empty string.
 * 
 * @see EventImpl
 * 
 * @author Greg Frazier
 *
 */
public class TimeoutEventImpl<T> implements StateMachine.TimedEvent {

	private final T payload;
	private final long deadline;
	private final String name;

	/**
	 * Construct a timed event that holds a payload.
	 * 
	 * @param payload  the object whose toString method will identify this event.
	 * @param deadline the last valid transition for this event
	 * @see StateMachineOld#getTransitionCount()
	 */
	public TimeoutEventImpl(T payload, long deadline) {
		this.payload = payload;
		this.deadline = deadline;
		this.name = null;
	}

	/**
	 * Construct a timed event that holds a payload.
	 * 
	 * @param payload  the object whose toString method will identify this event.
	 * @param deadline the last valid transition for this event
	 * @param value    the value returned by <code>toString()</code>. This is the
	 *                 value by which the state machine knows this event.
	 * @see StateMachineOld#getTransitionCount()
	 */
	public TimeoutEventImpl(T payload, long deadline, String value) {
		this.payload = payload;
		this.deadline = deadline;
		this.name = value;
	}

	public T getPayload() {
		return payload;
	}

	/**
	 * Returns the name of this event.
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

	@Override
	public long getTransitionDeadline() {
		return deadline;
	}
}
