package glf.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import glf.event.Event;
import glf.event.EventProcessor;
import glf.event.EventingSystem;

public class StateMachine implements EventProcessor {

	/**
	 * If this Event is used in a transition from a given state S, then when an
	 * Event is received while the StateMachine is in S, and there are no
	 * transitions whose Event matches the input, then the WILDCARD transition is
	 * invoked. If one thinks of the transitions from a state as being implemented
	 * by a switch statement, the WILDCARD event is the "default:" block.
	 */
	public static final String WILDCARD_EVENT = "*";

	private String name;

	private Set<State> states;

	private Map<State, Map<String, Transition>> stateTransitionMap;

	protected State currentState;

	private State startState;

	/**
	 * Count the number of state transitions that have occurred. Transitions to
	 * self-state, default transitions and wildcard transitions all count. Inputs
	 * that are ignored do not count.
	 */
	private long transitionCount;

	protected boolean verbose;

	private Set<StateMachineTracker> callbacks = new HashSet<>();

	protected EventingSystem eventingSystem;

	/**
	 * Construct a StateMachine that has the specified name, transitions, and
	 * initial state.
	 * 
	 * @param name        the name of the state machine
	 * @param transitions the transitions that compose the state machine. Note that
	 *                    the set of states is infered from the transitions.
	 * @param startState  the initial state of the machine
	 */
	public StateMachine(String name, EventingSystem es, Set<Transition> transitions, State startState) {
		this(name, es, startState);
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
	public StateMachine(String name, EventingSystem es, State startState) {
		this(name, es);
		this.startState = startState;
	}

	public StateMachine(String name, EventingSystem es) {
		this.name = name;
		this.eventingSystem = es;
		states = new HashSet<State>();
		stateTransitionMap = new HashMap<State, Map<String, Transition>>();
	}

	/**
	 * If the state machine was created w/out a specified start state, it can be
	 * specified (or changed) by this method. Will have no effect if the state
	 * machine has already been started.
	 * 
	 * @param s the initial state for the state machine
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
		Map<String, Transition> transitionMap = stateTransitionMap.get(fromState);
		if (transitionMap == null) {
			transitionMap = new HashMap<String, Transition>();
			stateTransitionMap.put(t.getFromState(), transitionMap);
		}
		if (t.getEvent() == null) {
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
		String key = (t.getEvent() == null ? null : t.getEvent().toString());
		transitionMap.put(key, t);
	}

	/**
	 * Obtain the current state of the state machine
	 * 
	 * @return the current state
	 */
	public State getCurrentState() {
		return currentState;
	}

	/**
	 * Causes the StateMachine to enter its initial state. If this method is not
	 * invoked, the state machine will enter its start state when the first input is
	 * received. While it is only necessary to invoke this method if the state
	 * machine itself initiates processing, it is good practice to invoke it.
	 * 
	 * @see #receive(Event)
	 */
	public void begin() {
		enterState(startState, null);
	}

	private void enterState(State state, Event e) {
		if (verbose) {
			System.out.println(this + " is entering state " + state);
		}
		currentState = state;
		State.Action action = currentState.getAction();
		if (action != null) {
			action.act(currentState, e);
		}
		Map<String, Transition> transitionMap = stateTransitionMap.get(currentState);
		if (transitionMap == null || transitionMap.isEmpty()) {
			// The state machine is in a terminal state
			for (StateMachineTracker tracker : callbacks) {
				tracker.stateMachineEnded(this);
			}
			return;
		}
		// Check for a null-transition (a transition that does not require an event
		// to trigger it).
		Transition t = transitionMap.get(null);
		if (t == null) {
			// There will be no further activity until an input is received
			return;
		}
		if (verbose) {
			System.out.println(currentState + " has a null transition " + t);
		}
		// This state has a null-transition.
		performTransition(t, null);
	}

	/**
	 * Perform the transition -- invoke the action associated with the transition
	 * and then enter the to-state of the transition. The transitionCount is
	 * incremented <emph>after</emph> the action is invoked and before the state is
	 * entered.
	 * 
	 * @param t
	 */
	private void performTransition(Transition t, Event e) {
		transitionCount++;
		enterState(t.getToState(), e);
	}

	/**
	 * Events trigger state transitions in the state machine. If {@link #begin()}
	 * was not invoked on this machine, the start state is entered before the first
	 * event is processed.
	 * 
	 * @param event the Event that is the next input to the StateMachine. If the
	 *              input triggers a Transition, the Transition's action is invoked
	 *              and the StateMachine will enter the next state.
	 */
	public void receive(Event event) {
		eventingSystem.scheduleEvent(this, event);
	}
	
	public void process(Event event, EventingSystem es) {
		if (currentState == null) {
			if (verbose) {
				System.out.println(this + " will enter its start state before processing inputs.");
			}
			enterState(startState, null);
		}
		if (verbose) {
			System.out.println(this + " received input " + event);
		}
		if (event instanceof TimedEvent) {
			TimedEvent te = (TimedEvent) event;
			if (transitionCount > te.getTransitionDeadline()) {
				if (verbose) {
					System.out.println(event + " ignored because its deadline has expired.");
				}
				return;
			}
		}
		Map<String, Transition> transitionMap = stateTransitionMap.get(currentState);
		if (transitionMap == null) {
			// The state machine is in a terminal state
			if (verbose) {
				System.out.println(currentState + " is a terminal state.");
			}
			return;
		}
		Transition t = transitionMap.get(event.toString());
		if (t == null) {
			t = transitionMap.get(WILDCARD_EVENT);
			if (verbose && t != null) {
				System.out.println(currentState + " is invoking the WILDCARD transition for input " + event);
			}
		}
		if (t == null) {
			// There is no transition defined for the input in the current state
			if (verbose) {
				System.out.println(currentState + " has no transition for input " + event);
				System.out.println("\tAll transitions:");
				for (String key : transitionMap.keySet()) {
					System.out.println("\t\t" + key);
				}
			}
			return;
		}
		performTransition(t, event);
	}

	public void registerCallback(StateMachineTracker callback) {
		callbacks.add(callback);
	}

	/**
	 * A callback method, invoked when the state machine enters a terminal state.
	 * 
	 * @see #registerCallback(StateMachineTracker)
	 */
	public static interface StateMachineTracker {

		/**
		 * This method is invoked when the tracked state machine enters a terminal state.
		 * 
		 * @param machine the state machine being tracked.
		 * @see #registerCallback(StateMachineTracker)
		 */
		public void stateMachineEnded(StateMachine machine);
	}

	/**
	 * Implemented by events that must be processed by a specific point in the state
	 * machine's life cycle. A timed event that is received when state machine's
	 * transition count is greater than the timed event's deadline is ignored.
	 * <p>
	 * This interface is specifically intended to support timeout events. Consider a
	 * state that is waiting to process an input that may never arrive. A timeout
	 * event may be issued; if the timeout is received before the waited-upon event
	 * occurs, an appropriate action is taken. Now consider that the state machine
	 * may implement a loop such that this state is entered multiple times, and so
	 * there may be multiple timeout events pending for the state machine. If the
	 * timeout event generated at iteration <em>i</em> is processed in iteration
	 * <em>i+1</em>, it would cause an erroneous timeout. By implementing the
	 * {@link TimedEvent} interface, the timeout will be ignored if it is received
	 * after the targeted state transition has occurred.
	 * <p>
	 * See {@link glf.msgxchg.MXStateMachine} for a state machine that uses
	 * timeouts.
	 * 
	 * @see StateMachine#getTimeoutEvent()
	 * @see StateMachine#getTimeoutEvent(String)
	 * 
	 * @author Greg Frazier
	 *
	 */
	public static interface TimedEvent extends Event {

		/**
		 * Obtain the deadline for this event&mdash;the last valid transition count. If
		 * this event is received after this point, it is ignored.
		 * 
		 * @return the deadline for this event.
		 */
		public long getTransitionDeadline();

	}

	/**
	 * Obtain a timeout event that will expire after the next transition.
	 * 
	 * @return a timeout event named "TIMEOUT".
	 * @see #getTimeoutEvent(String)
	 */
	public TimedEvent getTimeoutEvent() {

		final long deadline = transitionCount + 1;

		return new TimedEvent() {

			@Override
			public long getTransitionDeadline() {
				return deadline;
			}

			public String toString() {
				return "TIMEOUT";
			}

		};
	}

	/**
	 * Obtain a timeout event with a specific String identifier that will expire
	 * after the next transition.
	 * 
	 * @param name the name assigned to the event.
	 * @return a timeout event with the specified name.
	 * @see #getTimeoutEvent()
	 */
	public TimedEvent getTimeoutEvent(final String name) {

		final long deadline = transitionCount + 1;

		return new TimedEvent() {

			@Override
			public long getTransitionDeadline() {
				return deadline;
			}

			public String toString() {
				return name;
			}

		};
	}

	@Override
	public String toString() {
		return name + "{currentState = " + currentState + "}";
	}

	public void setVerbose(boolean v) {
		verbose = v;
	}

	public long getTransitionCount() {
		return transitionCount;
	}
}
