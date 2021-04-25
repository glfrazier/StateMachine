package com.github.glfrazier.statemachine;

import com.github.glfrazier.event.Event;
import com.github.glfrazier.event.EventingSystem;

/**
 * This test builds a StateMachine and then puts it through its paces. Note that
 * State was subclassed so that each state could hold a variable. One can do the
 * same with Events.
 * 
 * Note that the transition from State 2 to State 3 is a "null" transition--no
 * input is required. A transition can also have a null action.
 * 
 * Note that one can set the StateMachine's verbosity to <code>true</code>, in
 * which case it prints information to System.out on each state transition.
 * 
 * @author glfrazier
 *
 */
public class Test {

	static class StateWithCount extends State {

		public int count = 0;

		public StateWithCount(String name, Action a, StateMachine m) {
			super(name, a, m);
		}

		@Override
		public String toString() {
			return name + "(count=" + count + ")";
		}
	}

	public static void main(String[] args) throws Exception {

		EventingSystem es = new EventingSystem("Test Eventing System");
		StateMachine machine = new StateMachine("Test", es);
		
		Event testEvent = new Event() {
			public String toString() {
				return "TestEvent";
			}
		};
		
		State.Action action = new State.Action() {

			@Override
			public void act(State s, Event e) {
				StateWithCount swt = (StateWithCount) s;
				if (swt.count == 4) {
					System.out.println("Done!");
					System.exit(0);
				}
				System.out.println("Entered " + swt + " in response to " + e);
				swt.count++;
				s.machine.receive(testEvent);
			}

		};	
		
		State.Action noEventAction = new State.Action() {

			@Override
			public void act(State s, Event e) {
				StateWithCount swt = (StateWithCount) s;
				if (swt.count == 4) {
					System.out.println("Done!");
					System.exit(0);
				}
				System.out.println("Entered " + swt + " in response to " + e);
				swt.count++;
			}

		};

		StateWithCount s1 = new StateWithCount("State 1", action, machine);
		StateWithCount s2 = new StateWithCount("State 2", noEventAction, machine);
		StateWithCount s3 = new StateWithCount("State 3", action, machine);

		machine.addTransition(new Transition(s1, testEvent, s2));
		machine.addTransition(new Transition(s2, s3));
		machine.addTransition(new Transition(s3, testEvent, s1));
		machine.setStartState(s1);

		es.exitOnEmptyQueue(false);
		es.setVerbose(true);
		machine.setVerbose(true);
		machine.receive(testEvent);
		es.run();

	}

}
