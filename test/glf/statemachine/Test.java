package glf.statemachine;

import java.util.HashSet;
import java.util.Set;

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

		public StateWithCount(String name, Action a) {
			super(name, a);
		}

		@Override
		public String toString() {
			return name + "(count=" + count + ")";
		}
	}

	public static void main(String[] args) throws Exception {

		State.Action action = new State.Action() {

			@Override
			public void act(State s, StateMachine.Event e) {
				StateWithCount swt = (StateWithCount) s;
				if (swt.count == 4) {
					System.out.println("Done!");
					System.exit(0);
				}
				System.out.println("Entered " + swt + " in response to " + e);
				swt.count++;
			}

		};

		StateWithCount s1 = new StateWithCount("State 1", action);
		StateWithCount s2 = new StateWithCount("State 2", action);
		StateWithCount s3 = new StateWithCount("State 3", action);

		StateMachine.Event e = new StateMachine.Event() {
			public String toString() {
				return "TestEvent";
			}
		};

		Transition t1 = new Transition(s1, e, s2);
		Transition t2 = new Transition(s2, s3);
		Transition t3 = new Transition(s3, e, s1);

		Set<Transition> set = new HashSet<>();
		set.add(t1);
		set.add(t2);
		set.add(t3);

		StateMachine machine = new StateMachine("Test", set, s1);
		machine.setVerbose(true);
		while (true) {
			machine.receive(e);
			Thread.sleep(1000);
		}

	}

}
