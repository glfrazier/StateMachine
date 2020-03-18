package glf.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StateMachine {

	private String name;

	private Set<State> states;

	private Map<State, Map<Event, Transition>> stateTransitionMap;

	private State currentState;

	private State startState;

	private boolean verbose;

	public StateMachine(String name, Set<Transition> transitions, State startState) {
		this(name, startState);
		for (Transition t : transitions) {
			addTransition(t);
		}
	}

	public StateMachine(String name, State startState) {
		this.name = name;
		states = new HashSet<State>();
		this.startState = startState;
		stateTransitionMap = new HashMap<State, Map<Event, Transition>>();
	}

	public void addTransition(Transition t) {
		State fromState = t.getFromState();
		states.add(fromState);
		Map<Event, Transition> transitionMap = stateTransitionMap.get(fromState);
		if (transitionMap == null) {
			transitionMap = new HashMap<Event, Transition>();
			stateTransitionMap.put(t.getFromState(), transitionMap);
		}
		if (t.getInput() == null) {
			if (!transitionMap.isEmpty()) {
				throw new IllegalArgumentException("Defining a null-input-transition from state " + fromState
						+ " when there are other transitions from that state.");
			}
		} else {
			if (transitionMap.get(null) != null) {
				throw new IllegalArgumentException("Defining a transition from state " + fromState
						+ " when there is already a null-transition defined from that state.");
			}
		}
		transitionMap.put(t.getInput(), t);
	}

	public void begin() {
		enterState(startState);
	}

	private void enterState(State state) {
		currentState = state;
		Map<Event, Transition> transitionMap = stateTransitionMap.get(currentState);
		if (transitionMap == null) {
			// The state machine is in a terminal state
			return;
		}
		Transition t = transitionMap.get(null);
		if (t == null) {
			// There will be no further activity until an input is received
			return;
		}
		if (verbose) {
			System.out.println(currentState + " has a null transition " + t);
		}
		performTransition(t);
	}

	private void performTransition(Transition t) {
		Action action = t.getAction();
		if (action != null) {
			action.act(t);
		}
		enterState(t.getToState());
	}

	public void receive(Event input) {
		if (currentState == null) {
			if (verbose) {
				System.out.println(this + " will enter its start state before processing inputs.");
			}
			enterState(startState);
		}
		if (verbose) {
			System.out.println(this + " received input " + input);
		}
		Map<Event, Transition> transitionMap = stateTransitionMap.get(currentState);
		if (transitionMap == null) {
			// The state machine is in a terminal state
			if (verbose) {
				System.out.println(currentState + " is a terminal state.");
			}
			return;
		}
		Transition t = transitionMap.get(input);
		if (t == null) {
			// There is no transition defined for the input in the curren state
			if (verbose) {
				System.out.println(currentState + " has no transition for input " + input);
			}
			return;
		}
		t = t.getUpdatedTransition(input);
		performTransition(t);
	}

	/**
	 * Implemented by all objects that provide state machine actions.
	 * 
	 * @author glfrazier
	 *
	 */
	public static interface Action {

		/**
		 * Generate the output associated with the transition.
		 * 
		 * @param t
		 *            The transition that this action is associated with. Note
		 *            that the input in the Transition is the instance that was
		 *            received by the state machine as input, NOT the instance
		 *            that was used to define the transition. While the two
		 *            events are equal to each other per their definition of
		 *            <code>equals</code>, the instance that was received as
		 *            input may contain state that modifies the semantics of the
		 *            Action.
		 */
		public void act(Transition t);
	}

	/**
	 * A tagging interface for Events that trigger Transitions.
	 * 
	 * @see EventImpl
	 */
	public static interface Event {

	}

	@Override
	public String toString() {
		return name + "{" + currentState + "}";
	}

	public void setVerbose(boolean v) {
		verbose = v;
	}

}
