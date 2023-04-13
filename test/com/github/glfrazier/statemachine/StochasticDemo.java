package com.github.glfrazier.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.github.glfrazier.event.Event;
import com.github.glfrazier.event.EventingSystem;
import com.github.glfrazier.statemachine.StateMachine.EventEqualityMode;

public class StochasticDemo {

	static final Event EVENT = new EventImpl("STATE_CHANGE");

	static State.Action action = new State.Action() {

		@Override
		public void act(StateMachine sm, State s, Event e) {
			System.out.println("\tEntered " + s + " in response to " + e);
		}

	};

	public static void main(String[] args) throws Exception {
		System.out.println("Beginning the stochastic demo.");
		EventingSystem es = new EventingSystem(false);
		es.exitOnEmptyQueue(false);
		Thread t = new Thread(es);
		t.setDaemon(true);
		t.start();

		StateMachine machine = new StateMachine("Stochastic Demo", EventEqualityMode.EQUALS, es);
		Random rand = new Random();

		State s0 = new State("State 0", action);
		State s1 = new State("State 1", action);
		State s2 = new State("State 2", action);
		State s3 = new State("State 3", action);

		machine.setStartState(s0);

		double[] s0Probs = { 0.6, 0.4 };
		State[] s0NextStates = { s0, s1 };
		machine.addTransition(new StochasticTransition(s0, EVENT, s0Probs, s0NextStates));

		double[] s1Probs = { 0.3, 0.3, 0.4 };
		State[] s1NextStates = { s0, s1, s2 };
		machine.addTransition(new StochasticTransition(s1, EVENT, s1Probs, s1NextStates));

		double[] s2Probs = { 0.2, 0.4, 0.4 };
		State[] s2NextStates = { s1, s2, s3 };
		machine.addTransition(new StochasticTransition(s2, EVENT, s2Probs, s2NextStates));

		double[] s3Probs = { 0.7, 0.3 };
		State[] s3NextStates = { s2, s3 };
		machine.addTransition(new StochasticTransition(s3, EVENT, s3Probs, s3NextStates));

		for (int i = 0; i < 20; i++) {
			es.scheduleEvent(machine, EVENT);
			Thread.sleep(1000);
		}
		System.out.println("Twenty transitions completed. Terminating.");
	}

}
