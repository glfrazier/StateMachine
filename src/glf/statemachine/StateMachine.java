package glf.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StateMachine {

	/**
	 * If this Event is used in a transition from a given state S, then when an
	 * Event is received while the StateMachine is in S, and there are no
	 * transitions whose Event matches the input, then the WILDCARD transition is
	 * invoked. If one thinks of the transitions from a state as being implemented
	 * by a switch statement, the WILDCARD event is the "default:" block.
	 */
	public static final Event WILDCARD_EVENT = new EventImpl("The 'Wildcard' Event");

	private String name;

	private Set<State> states;

	private Map<State, Map<Event, Transition>> stateTransitionMap;

	protected State currentState;

	private State startState;

	/**
	 * Count the number of state transitions that have occurred. Transitions to
	 * self-state, default transitions and wildcard transitions all count. Inputs
	 * that are ignored do not count.
	 */
	private long transitionCount;

	private boolean verbose;

	/**
	 * Construct a StateMachine that has the specified name, transitions, and
	 * initial state.
	 * 
	 * @param name        the name of the state machine
	 * @param transitions the transitions that compose the state machine. Note that
	 *                    the set of states is infered from the transitions.
	 * @param startState  the initial state of the machine
	 */
	public StateMachine(String name, Set<Transition> transitions, State startState) {
		this(name, startState);
		for (Transition t : transitions) {
			addTransition(t);
		}
	}

	/**
	 * Construct a StateMachine with a name and an initial state. Transitions must
	 * be added.
	 * 
	 * @see #addTransition(Transition)
	 * @param name       the name of the state machine
	 * @param startState the initial state of the machine
	 */
	public StateMachine(String name, State startState) {
		this(name);
		this.startState = startState;
	}

	public StateMachine(String name) {
		this.name = name;
		states = new HashSet<State>();
		stateTransitionMap = new HashMap<State, Map<Event, Transition>>();
	}

	/**
	 * If the state machine was created w/out a specified start state, it can be
	 * specified (or changed) by this method. Will have no effect if the state
	 * machine has already been started.
	 * 
	 * @param s
	 */
	public void setStartState(State s) {
		this.startState = s;
	}

	/**
	 * Add transitions to the state machine. This method can be invoked at any
	 * point, even after one has begun to execute the state machine.
	 * 
	 * @param t a transition from one state to another
	 */
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

	/**
	 * Causes the StateMachine to enter its initial state. Note that one can receive
	 * inputs without invoking <code>begin()</code>. If you do, the startState is
	 * entered before the input is processed.
	 * 
	 * @see #receive(Event)
	 */
	public void begin() {
		enterState(startState);
	}

	private void enterState(State state) {
		if (verbose) {
			System.out.println(this + " is entering state " + state);
		}
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

	/**
	 * Perform the transition -- invoke the action associated with the transition
	 * and then enter the to-state of the transition. The transitionCount is
	 * incremented <emph>after</emph> the action is invoked and before the state is
	 * entered.
	 * 
	 * @param t
	 */
	private void performTransition(Transition t) {
		Action action = t.getAction();
		if (action != null) {
			action.act(t);
		}
		transitionCount++;
		enterState(t.getToState());
	}

	/**
	 * Non-null transitions can only be processed when an input Event is received.
	 * 
	 * @param input the Event that is the next input to the StateMachine. If the
	 *              input triggers a Transition, the Transition's action is invoked
	 *              and the StateMachine will enter a new state, which may be the
	 *              same as the previous state.
	 */
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
			t = transitionMap.get(WILDCARD_EVENT);
			if (verbose && t != null) {
				System.out.println(currentState + " is invoking the WILDCARD transition for input " + input);
			}
		}
		if (t == null) {
			// There is no transition defined for the input in the curren state
			if (verbose) {
				System.out.println(currentState + " has no transition for input " + input);
				System.out.println("\tAll transitions:");
				for(Event key : transitionMap.keySet()) {
					System.out.println("\t\t" + key);
				}
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
		 * @param t The transition that this action is associated with. Note that the
		 *          input in the Transition is the instance that was received by the
		 *          state machine as input, NOT the instance that was used to define the
		 *          transition. While the two events are equal to each other per their
		 *          definition of <code>equals</code>, the instance that was received as
		 *          input may contain state that modifies the semantics of the Action.
		 */
		public void act(Transition t);
	}

	/**
	 * A tagging interface for Events that trigger Transitions.
	 * 
	 * @see EventImpl
	 */
	public static interface Event {

		public String getName();
		
	}


	@Override
	public String toString() {
		return name + "{" + currentState + "}";
	}

	public void setVerbose(boolean v) {
		verbose = v;
	}

	public long getTransitionCount() {
		return transitionCount;
	}
}
