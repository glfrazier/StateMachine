package glf.statemachine;

/**
 * The state of a state machine.
 * 
 * @author glfrazier
 *
 */
public class State {

	protected String name;
	
	public State(String name) {
		if (name == null) {
			throw new IllegalArgumentException("States must have non-null names.");
		}
		this.name = name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof State)) {
			return false;
		}
		return name.equals(((State)o).name);
	}
}
