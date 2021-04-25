package com.github.glfrazier.statemachine;

import com.github.glfrazier.event.Event;

/**
 * The state of a state machine.
 * 
 * @author glfrazier
 *
 */
public class State {

	protected final String name;

	protected StateMachine machine;

	protected Action action;

	/**
	 * A State that only has a name--it does not have an associated Action and it
	 * does not have a reference to the state machine that it is a part of.
	 * 
	 * @param name the name of this state. Note that the name is how a State is
	 *             known:
	 *             <code>new State("alpha").equals(new State("alpha")) == true</code>.
	 */
	public State(String name) {
		if (name == null) {
			throw new IllegalArgumentException("States must have non-null names.");
		}
		this.name = name;
	}

	/**
	 * Create a State that has an associated action.
	 * 
	 * @param name   the name of this state
	 * @param action the action to be taken when this state is entered
	 * @see #State(String)
	 */
	public State(String name, Action action) {
		this(name);
		this.action = action;
	}

	/**
	 * Create a State that has an associated action and a reference to a specific
	 * state machine that it is a member of.
	 * 
	 * @param name    the name of this state
	 * @param action  the action to be taken when this state is entered
	 * @param machine the state machine for which this is a state
	 * @see #State(String)
	 */
	public State(String name, Action action, StateMachine machine) {
		this(name, action);
		this.machine = machine;
	}

	public StateMachine getMachine() {
		return machine;
	}

	public void setMachine(StateMachine machine) {
		this.machine = machine;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof State)) {
			return false;
		}
		return name.equals(((State) o).name);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * When a State is entered, its associated Action is executed (the
	 * {@link #act(Transition)} method is invoked).
	 * 
	 * @author glfrazier
	 *
	 */
	public static interface Action {

		/**
		 * Perform the action associated with the State. Since the state is passes as a
		 * parameter, one can use the same <code>Action</code> in multiple states.
		 * 
		 * @param s The state that was entered
		 * @param e The event that caused the transition to this state
		 * 
		 */
		public void act(State s, Event e);
	}

}
