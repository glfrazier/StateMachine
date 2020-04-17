package glf.statemachine;

/**
 * An Event implementation that uses its name and its source to implement
 * hashCode and equals.
 * 
 * @author Greg Frazier
 *  *
 */
public class EventImpl implements StateMachine.Event {

	protected final String name;

	public EventImpl(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StateMachine.Event)) {
			return false;
		}
		StateMachine.Event e = (StateMachine.Event) o;
		return name.equals(e.getName());
	}

	@Override
	public String toString() {
		return name;
	}
}
