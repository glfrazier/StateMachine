# StateMachine

A State Machine builder and execution engine.

StateMachine is an implementation of the Moore state machine model.
The outputs of a Moore state machine depend only on the state that is being entered.
(As opposed to a Mealy state machine, whose outputs depend on the current state and the input.)

* To implement the Moore model, each State has an optional State.Action that is invoked when the State is entered.

* Each State transition is defined by the 3-tuple (Current-State, Input, Next-State), implemented by the Transition class.
It is possible to have a state transition that occurs without an input. If a state machine has the 
Transition (S1, null, S2), when state S1 is entered, 
the State.Action associated with S1 is invoked and then the machine immediately transitions to S2.

* One can define a stochastic state machine via StochasticTransition objects. Where the regular (deterministic) Transition specifies 
a single next state for a given (state, input) pair, a StochasticTransition specifies an array of next states with a corresponding array
of probabilities.

There are examples of building and using state machines the test folder.
