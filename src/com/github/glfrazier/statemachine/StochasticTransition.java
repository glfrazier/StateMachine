package com.github.glfrazier.statemachine;

import static java.lang.Math.abs;
import java.util.Random;

import com.github.glfrazier.event.Event;

public class StochasticTransition extends Transition {

	protected double[] probabilities;
	protected State[] nextStates;
	private Random rand;

	private static final double EPSILON = 0.000001;

	public StochasticTransition(State fromState, Event event, double[] probabilities, State[] nextStates,
			Random random) {
		super(fromState, event, null);
		if (probabilities.length != nextStates.length) {
			throw new IllegalArgumentException("The probabilities and nextStates arrays are of differing length.");
		}
		this.probabilities = probabilities;
		this.nextStates = nextStates;
		this.rand = random;
		checkAndFixProbabilities();
	}

	public StochasticTransition(State fromState, Event event, double[] probabilities, State[] nextStates) {
		this(fromState, event, probabilities, nextStates, new Random());
	}

	private void checkAndFixProbabilities() {
		double probTotal = 0.0;
		int lastNonZeroState = -1;
		for (int i = 0; i < probabilities.length; i++) {
			if (probabilities[i] < 0.0 || probabilities[i] > 1.0) {
				throw new IllegalArgumentException(
						"Probability " + i + " is out of range [0..1]: " + probabilities[i]);
			}
			if (probabilities[i] > 0.0) {
				lastNonZeroState = i;
			}
			probTotal += probabilities[i];
		}
		if (abs(1.0 - probTotal) > EPSILON) {
			throw new IllegalArgumentException(
					"The total probabilities in this stochastic transition are more than epsilon away from 1.0.");
		}
		if (probTotal < 1.0) {
			probabilities[lastNonZeroState] += EPSILON;
		}
	}

	/**
	 * Get the state that this is a transition to.
	 * 
	 * @return the "to" state of the transition.
	 */
	public State getToState() {
		double prob = rand.nextDouble();
		double acc = 0;
		for (int i = 0; i < probabilities.length; i++) {
			acc += probabilities[i];
			if (acc > prob) {
				return nextStates[i];
			}
		}
		StringBuffer errorMsg = new StringBuffer("Did not find a state to return. Rand=" + prob + "; Probabilities:");
		double tot = 0;
		for (Double p : probabilities) {
			tot += p;
			errorMsg.append(" " + p + " (" + tot + ")");
		}
		throw new IllegalStateException(errorMsg.toString());
	}

}
