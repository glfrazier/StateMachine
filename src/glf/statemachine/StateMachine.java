package glf.statemachine;

import java.util.EventObject;
import java.util.Map;
import java.util.Set;

public class StateMachine {

	protected Set<State> states;
	
	protected Map<State, Map<EventObject, Transition>> transitionMap;
	
}
