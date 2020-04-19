package glf.statemachine;

import glf.statemachine.StateMachine.Action;
import glf.statemachine.StateMachine.Event;

/**
 * A transition from one state to another in a Mealy state machine. This is the
 * four-tuple (from-state, input, output, to-state).
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
	 * case it must be the only transition from the <code>fromState</code>
	 */
	protected StateMachine.Event event;

	/**
	 * The action to perform on this transition. It may be <code>null</code>, in
	 * which case there is no action invoked.
	 */
	protected Action action;

	/**
	 * Construct a state machine transition.
	 * 
	 * @param fromState the state that will be transitioned from
	 * @param event     the String returned by the <code>toString</code> method of
	 *                  the event that, if received while the machine is in
	 *                  <code>fromState</code>, will cause this transition to occur
	 * @param action    the action to take upon transition
	 * @param toState   the state the machine will be in after the transition
	 */
	public Transition(State fromState, String event, Action action, State toState) {
		this.fromState = fromState;
		this.event = new EventImpl<String>(event);
		this.action = action;
		this.toState = toState;
	}

	/**
	 * Construct a state machine transition.
	 * 
	 * @param fromState the state that will be transitioned from
	 * @param event     the event that, if received while the machine is in
	 *                  <code>fromState</code>, will cause this transition to occur
	 * @param action    the action to take upon transition
	 * @param toState   the state the machine will be in after the transition
	 */
	public Transition(State fromState, Event event, Action action, State toState) {
		this.fromState = fromState;
		this.event = event;
		this.action = action;
		this.toState = toState;
	}

	/**
	 * Construct an event-less transition. Upon entering the fromState, the state
	 * machine will immediately perform the action and transition to the toState.
	 * 
	 * @param fromState the state that will be transitioned from
	 * @param action    the action to take upon transition
	 * @param toState   the state the machine will be in after the transition
	 */
	public Transition(State fromState, Action action, State toState) {
		this.fromState = fromState;
		this.event = null;
		this.action = action;
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

	public Action getAction() {
		return action;
	}

	public Transition getUpdatedTransition(Event input) {
		return new Transition(this.fromState, input, this.action, this.toState);
	}

	@Override
	public String toString() {
		return fromState + " => " + toState;
	}

}
