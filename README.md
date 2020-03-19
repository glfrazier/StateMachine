# StateMachine

A State Machine (Mealy) builder, with associated DSL to define the state machine.

A Mealy state machine is one whose outputs depend on the current state and the inputs.
So, each state transition is a 4-tuple: (current state, input, output, next state).
Note that it is possible to have a state transition that occurs without an input--<null> is a
valid input specification for a Transition.

* The states are instances of the State class.
* The inputs to the State Machine are objects that implement StateMachine.Event, a tagging class.
There is an implementation of Event, EventImpl, that takes a String name as an argument to its
constructor and uses the name for hashCode and equals.
* The State Machine outputs are invocations of the `act(Transition)` method on an object
that implements the StateMachine.Action interface. `null` is a legal value for the output of
a Transition--no output is generated.

Thus, when one specifies the transition
```
(State1, INPUT_A, OUTPUT_B, State2)
```
One is saying that, when `State1` is the state machine's current state,
if an Event `input` is received that equals `INPUT_A`, then `OUTPUT_B.act(transition)` is
invoked and the current state is set to `State2`. Note that `act` is passed the entire Transition;
further, it is the 4-tuple whose input-field is the Event instance that instigated the transition,
not the instance that was used to define the Transition when building the state machine.

If the specification is
```
(State1, null, OUTPUT_B, State2)
```
then when the state machine's current state is set to `State1`, then `OUTPUT_B` is immediately invoked
and the state machine immediately transitions to `State2`.

There is a simple example of building and running a StateMachine in the test/ folder.
