package com.github.glfrazier.statemachine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.glfrazier.event.Event;
import com.github.glfrazier.event.EventProcessor;
import com.github.glfrazier.event.EventingSystem;

public class StateMachine implements EventProcessor {

	/**
	 * The mode by which the state machine compares an {@link Event} it receives
	 * (see {@link #process(Event, EventingSystem)} and {@link #receive(Event)}) to
	 * the events specified in the state {@link Transition}s.
	 * 
	 * Note that, if one constructs events as static constants of anonymous classes,
	 * and uses those events throughout the state machine, then all the three modes
	 * will work.
	 */
	public static enum EventEqualityMode {
		/**
		 * Transition.event.equals(event). If you do not override equals(Object) in you
		 * event class, this is the Object.equals(Object) method, which is equivalent to
		 * "==".
		 */
		EQUALS,
		/** Transition.event.toString().equals(event.toString()) */
		STRING_EQUALS,
		/** Transition.event.getClass().equals(event.getClass()) */
		CLASS_EQUALS
	};

	/**
	 * If this event (or its name or its class) is the trigger in a transition from
	 * a given state S, then when an Event is received while the StateMachine is in
	 * S, and there are no transitions whose Event matches the input, then the
	 * WILDCARD transition is invoked. If one thinks of using a switch statement to
	 * select the transition from a state, the transition triggered by the WILDCARD
	 * is the "default:" block for that switch statement.
	 */
	public static final Event WILDCARD_EVENT = new Event() {
		public static final String NAME = "*";

		public String toString() {
			return NAME;
		}
	};

	/**
	 * A useful event.
	 */
	public static final Event SUCCESS_EVENT = new Event() {
		public static final String NAME = "success";

		public String toString() {
			return NAME;
		}
	};

	/**
	 * A useful event.
	 */
	public static final Event FAILURE_EVENT = new Event() {
		public static final String NAME = "failure";

		public String toString() {
			return NAME;
		}
	};

	/**
	 * The default event name for timeout events.
	 * 
	 * @see #getTimeoutEvent()
	 */
	public static final String TIMEOUT = "TIMEOUT";

	private String name;

	private Set<State> states;

	private Map<State, Map<Object, Transition>> stateTransitionMap;

	protected State currentState;

	private State startState;

	/**
	 * Count the number of state transitions that have occurred. Transitions to
	 * self-state, default transitions and wildcard transitions all count. Inputs
	 * that are ignored do not count.
	 */
	private long transitionCount;

	protected boolean verbose;

	private Set<StateMachineTracker> callbacks = Collections.synchronizedSet(new HashSet<>());
	private boolean callbacksInvoked;

	protected EventingSystem eventingSystem;

	private final EventEqualityMode eventEqualityMode;

	/**
	 * Construct a StateMachine that has the specified name, transitions, and
	 * initial state.
	 * 
	 * @param name        the name of the state machine
	 * @param transitions the transitions that compose the state machine. Note that
	 *                    the set of states is infered from the transitions.
	 * @param startState  the initial state of the machine
	 */
	public StateMachine(String name, EventEqualityMode mode, EventingSystem es, Set<Transition> transitions,
			State startState) {
		this(name, mode, es, startState);
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
	public StateMachine(String name, EventEqualityMode mode, EventingSystem es, State startState) {
		this(name, mode, es);
		this.startState = startState;
	}

	public StateMachine(String name, EventEqualityMode mode, EventingSystem es) {
		this.name = name;
		this.eventEqualityMode = mode;
		this.eventingSystem = es;
		states = new HashSet<State>();
		stateTransitionMap = new HashMap<>();
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
		if (t.getTriggerType() != eventEqualityMode && t.getTriggerType() != null) {
			throw new IllegalArgumentException("Attempted to add transition " + t + " with trigger type "
					+ t.getTriggerType() + " to state machine of mode " + eventEqualityMode + ".");
		}
		State fromState = t.getFromState();
		states.add(fromState);
		Map<Object, Transition> transitionMap = stateTransitionMap.get(fromState);
		if (transitionMap == null) {
			transitionMap = new HashMap<>();
			stateTransitionMap.put(t.getFromState(), transitionMap);
		}
		Object trigger = t.getTrigger();
		if (trigger == null) {
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
		transitionMap.put(trigger, t);
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
	public synchronized void begin() {
		if (currentState == null) {
			enterState(startState, null);
		}
	}

	private void enterState(State state, Event e) {
		if (verbose) {
			System.out.println(this + " is entering state (" + state + ")");
			System.out.flush();
		}
		State oldCurrentState = currentState;
		currentState = state;
		State.Action action = currentState.getAction();
		if (action != null) {
			action.act(this, currentState, e);
		}
		Map<Object, Transition> transitionMap = stateTransitionMap.get(currentState);
		if (transitionMap == null || transitionMap.isEmpty()) {
			// The state machine is in a terminal state
			synchronized (callbacks) {
				if (callbacksInvoked) {
					new Exception("Callbacks invoked a 2nd time! oldCurrentState=" + oldCurrentState + ", currentState="
							+ currentState + ", e=" + e).printStackTrace();
				}
				for (StateMachineTracker tracker : callbacks) {
					tracker.stateMachineEnded(this);
				}
				callbacksInvoked = true;
			}
			return;
		}
		// Check for a null-transition (a transition that does not require an event
		// to trigger it).
		Transition transition = transitionMap.get(null);
		if (transition == null) {
			// There will be no further activity until an input is received
			return;
		}
		if (verbose) {
			System.out.println("*" + currentState + ") has a null transition " + transition);
			System.out.flush();
		}
		State toState = transition.getToState();
		// This state has a null-transition.
		performTransition(toState, null);
	}

	/**
	 * Perform the transition -- invoke the action associated with the transition
	 * and then enter the to-state of the transition. The transitionCount is
	 * incremented <emph>after</emph> the action is invoked and before the state is
	 * entered.
	 * 
	 * @param toState
	 */
	private void performTransition(State toState, Event e) {
		transitionCount++;
		enterState(toState, e);
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

	public synchronized void process(Event event, EventingSystem es, long time) {
		if (currentState == null) {
			if (verbose) {
				System.out.println(this + " will enter its start state before processing inputs.");
				System.out.flush();
			}
			enterState(startState, null);
		}
		if (verbose) {
			System.out.println(this + " received input <" + event + "> of class <" + event.getClass() + ">");
		}
		if (event instanceof TimedEvent) {
			TimedEvent te = (TimedEvent) event;
			if (transitionCount >= te.getTransitionDeadline()) {
				if (verbose) {
					System.out.println("<" + event + "> ignored because its deadline has expired.");
					System.out.flush();
				}
				return;
			} else {
				if (verbose) {
					System.out.println("<" + event + "> will be processed. transitionCount=" + transitionCount
							+ ", deadline=" + te.getTransitionDeadline());
				}
			}
		}
		Map<Object, Transition> transitionMap = stateTransitionMap.get(currentState);
		if (transitionMap == null || transitionMap.isEmpty()) {
			// The state machine is in a terminal state
			if (verbose) {
				System.out.println("(" + currentState + ") is a terminal state.");
				System.out.flush();
			}
			return;
		}
		Transition t = null;
		State toState = null;
		switch (eventEqualityMode) {
		case EQUALS:
			t = transitionMap.get(event);
			toState = (t == null ? null : t.getToState());
			break;
		case STRING_EQUALS:
			t = transitionMap.get(event.toString());
			toState = (t == null ? null : t.getToState());
			break;
		case CLASS_EQUALS:
			t = transitionMap.get(event.getClass());
			toState = (t == null ? null : t.getToState());
			break;
		}
		if (toState == null) {
			t = (eventEqualityMode == EventEqualityMode.CLASS_EQUALS
					? transitionMap.get(WILDCARD_EVENT.getClass())
					: transitionMap.get(WILDCARD_EVENT));
			toState = (t == null ? null : t.getToState());
			if (verbose && toState != null) {
				System.out.println(
						"(" + currentState + ") is invoking the WILDCARD transition for input <" + event + ">");
			}
		}
		if (toState == null) {
			// There is no transition defined for the input in the current state
			if (verbose) {
				System.out.println("(" + currentState + ") has no transition for input " + event + " (class "
						+ event.getClass() + ").");
				System.out.println("\tAll transitions:");
				for (Object key : transitionMap.keySet()) {
					System.out.println("\t\t" + key + " (" + key.getClass() + ")");
				}
				System.out.flush();
			}
			return;
		}
		if (verbose) {
			System.out.println(this + " transitioning to " + toState + " in response to event <" + event + ">.");
		}
		performTransition(toState, event);
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
		 * This method is invoked when the tracked state machine enters a terminal
		 * state.
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
	 * See {@link com.github.glfrazier.msgxchg.MXStateMachine} for a state machine
	 * that uses timeouts.
	 * 
	 * @see StateMachine#TIMEOUT
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
		return getTimeoutEvent(TIMEOUT);
	}

	/**
	 * Obtain a timeout event with a specific String identifier that will expire
	 * (become invalid) after the next transition.
	 * 
	 * @param name the name assigned to the event.
	 * @return a timeout event with the specified name.
	 * @see #getTimeoutEvent()
	 */
	public TimedEvent getTimeoutEvent(String n) {
		final long deadline = transitionCount + 1;
		return new TimeoutEventImpl<String>(n, deadline);
	}

	@Override
	public String toString() {
		return name + "[currentState = (" + currentState + ")]";
	}

	public void setVerbose(boolean v) {
		verbose = v;
	}

	public long getTransitionCount() {
		return transitionCount;
	}

	public void scheduleTimeout(long timeDeltaMS) {
		if (eventingSystem == null) {
			throw new UnsupportedOperationException(
					"Timeouts can only be scheduled in state machines that are constructed with event systems.");
		}
		TimedEvent timeout = this.getTimeoutEvent();
		eventingSystem.scheduleEventRelative(this, timeout, timeDeltaMS);
	}

	public void scheduleTimeout(String eventName, long timeDeltaMS) {
		if (eventingSystem == null) {
			throw new UnsupportedOperationException(
					"Timeouts can only be scheduled in state machines that are constructed with event systems.");
		}
		TimedEvent timeout = this.getTimeoutEvent(eventName);
		eventingSystem.scheduleEventRelative(this, timeout, timeDeltaMS);
	}

	public void scheduleTimeout(TimedEvent event, long timeDeltaMS) {
		if (eventingSystem == null) {
			throw new UnsupportedOperationException(
					"Timeouts can only be scheduled in state machines that are constructed with event systems.");
		}
		Event timeout = this.getTimeoutEvent();
		eventingSystem.scheduleEventRelative(this, timeout, timeDeltaMS);
	}
}
