package glf.statemachine;

public class Test {
	
	static class StateWithCount extends State {
		
		public int count = 0;
		
		public StateWithCount(String name) {
			super(name);
		}
		
		@Override
		public String toString() {
			return name + "(" + count + ")";
		}
	}

	public static void main(String[] args) throws Exception {

		StateWithCount s1 = new StateWithCount("State 1");
		StateWithCount s2 = new StateWithCount("State 2");
		StateWithCount s3 = new StateWithCount("State 3");

		final StateMachine machine = new StateMachine("Test", s1);
		StateMachine.Action action = new StateMachine.Action() {

			@Override
			public void act(Transition t) {
				StateWithCount swt = (StateWithCount)t.getFromState();
				if (swt.count == 8) {
					System.out.println("Done!");
					System.exit(0);
				}
				StateWithCount dst = (StateWithCount)t.getToState();
				dst.count = swt.count + 1;
				System.out.println("Transition: " + t.getFromState() + " ==> " + t.getToState());
			}

		};
		StateMachine.Event e = new EventImpl("Event");

		Transition t1 = new Transition(s1, e, action, s2);
		machine.addTransition(t1);
		Transition t2 = new Transition(s2, e, action, s3);
		machine.addTransition(t2);
		Transition t3 = new Transition(s3, e, action, s1);
		machine.addTransition(t3);
		
		machine.setVerbose(true);
		while(true) {
			machine.receive(e);
			Thread.sleep(1000);
		}

	}

}
