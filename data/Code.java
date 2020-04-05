package demo; 
import glf.statemachine.*;
public class Demonstration {
public static StateMachine buildStateMachine() {

State A = new State("A");
State B = new State("B");
State C = new State("C");
StateMachine.Event in0 = new EventImpl("in0")
StateMachine.Event in1 = new EventImpl("in1")
StateMachine.Action a1 = new StateMachine.Action() {
  public void act(Transition t) { System.out.println(t); }
}
StateMachine.Action a2 = new StateMachine.Action() {
  public void act(Transition t) { System.out.println(t); }
}
StateMachine.Action done = new StateMachine.Action() {
  public void act(Transition t) { System.out.println(t); }
}
StateMachine.Action a0 = new StateMachine.Action() {
  public void act(Transition t) { System.out.println(t); }
}
Set<Transition> transitions = new HashSet<>();
transitions.add(new Transition(A, in1, a1, C);
transitions.add(new Transition(C, null, done, A);
transitions.add(new Transition(B, in1, a2, C);
transitions.add(new Transition(A, in0, a0, B);
transitions.add(new Transition(B, in0, a2, A);
return new StateMachine(name, transitions, A);
}

}
