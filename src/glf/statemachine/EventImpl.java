package glf.statemachine;

/**
 * An Event implementation that uses its name and its source to implement hashCode and
 * equals.
 * 
 * @author glfrazier
 *
 */
public class EventImpl implements StateMachine.Event {

	protected String name;

	public EventImpl(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventImpl)) {
			return false;
		}
		EventImpl e = (EventImpl) o;
		return name.equals(e.name);
	}

	@Override
	public String toString() {
		return name;
	}
}
