# StateMachine

A State Machine (Mealy) builder, with associated DSL to define the state machine.

A Mealy state machine is one whose outputs depend on the current state and the inputs.
So, each state transition is a 4-tuple: (current state, input, output, next state).
Note that it is possible to have a state that transitions without an input--<null> is a
valid input.

* The states are instances of the State class.
* The inputs to the State Machine are implementations of StateMachine.Event, a tagging class. There is also an implementation, EventImpl, that takes a String name as an argument to its constructor and uses the name for hashCode and equals.
* The outputs are invocations of the `act(Transition)` method on an object
that implements the StateMachine.Action interface.

Thus, when one specifies the transition
```
State1:INPUT_A => OUTPUT_B,State2
```
One is saying that, when `State1` is the state machine's current state,
if an EventObject `input` is received that equals `INPUT_A`, then `OUTPUT_B.act(input)` is
invoked and the current state is set to `State2`. If the specification is
```
State1:null => OUTPUT_B,State2
```
then when the state machine's current state is set to `State1`, then `OUTPUT_B` is immediately invoked
and the state machine immediately transitions to `State2`.

There is a simple example of building and running a StateMachine in the test/ folder.
