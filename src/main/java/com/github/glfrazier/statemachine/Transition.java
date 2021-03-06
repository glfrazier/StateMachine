package com.github.glfrazier.statemachine;

import com.github.glfrazier.event.Event;

/**
 * A transition from one state to another in a Moore state machine. This is the
 * three-tuple (from-state, input, to-state).
 * 
 * @author glfrazier
 *
 */
public class Transition {

	/**
	 * The State that this is a transition from.
	 */
	protected State fromState;

	/**
	 * The State that this is a transition to.
	 */
	protected State toState;

	/**
	 * The event that triggers the transition. It may be <code>null</code>, in which
	 * case any other transition from the <code>fromState</code> will be ignored.
	 */
	protected Event event;

	/**
	 * Construct a state machine transition, using a String to identify the Event
	 * that will trigger the transition.
	 * 
	 * @param fromState the state that will be transitioned from
	 * @param event     the String returned by the <code>toString</code> method of
	 *                  the event that, if received while the machine is in
	 *                  <code>fromState</code>, will cause this transition to occur
	 * @param toState   the state the machine will be in after the transition
	 */
	public Transition(State fromState, String event, State toState) {
		this.fromState = fromState;
		this.event = new EventImpl<String>(event);
		this.toState = toState;
	}

	/**
	 * Construct a state machine transition.
	 * 
	 * @param fromState the state that will be transitioned from
	 * @param event     the event that, if received while the machine is in
	 *                  <code>fromState</code>, will cause this transition to occur
	 * @param toState   the state the machine will be in after the transition
	 */
	public Transition(State fromState, Event event, State toState) {
		this.fromState = fromState;
		this.event = event;
		this.toState = toState;
	}

	/**
	 * Construct a null-transition. Since there is no event associated with this
	 * transition, as soon as the state machine enters the from-state and invokes
	 * its action, it will immediately transition to the to-state. Invoking this
	 * constructor is equivalent to using one of the three-argument constructors
	 * specifying <code>null</code> as the triggering event.
	 * 
	 * @param fromState the state that will be transitioned from
	 * @param toState   the state the machine will be in after the transition
	 */
	public Transition(State fromState, State toState) {
		this.fromState = fromState;
		this.event = null;
		this.toState = toState;
	}

	/**
	 * Get the state that this is a transition from.
	 * 
	 * @return the "from" state of the transition.
	 */
	public State getFromState() {
		return fromState;
	}

	/**
	 * Get the state that this is a transition to.
	 * 
	 * @return the "to" state of the transition.
	 */
	public State getToState() {
		return toState;
	}

	public Event getEvent() {
		return event;
	}

	@Override
	public String toString() {
		return fromState + "(" + event + ") => " + toState;
	}

}
