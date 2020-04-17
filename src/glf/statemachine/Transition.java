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
	 * The event that triggers the transition. It may be <code>null</code>, in
	 * which case it must be the only transition from the <code>fromState</code>
	 */
	protected Event input;

	/**
	 * The action to perform on this transition. It may be <code>null</code>, in
	 * which case there is no action invoked.
	 */
	protected Action action;

	public Transition(State fromState, Event input, Action action, State toState) {
		this.fromState = fromState;
		this.input = input;
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

	public Event getInput() {
		return input;
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
