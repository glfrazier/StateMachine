package glf.statemachine;

/**
 * A helper class that allows one to easily wrap an object into a StateMachine
 * Timed Event. The name of the event is the payload's toString() result. A null
 * payload will return the empty string.
 * 
 * @see EventImpl
 * 
 * @author Greg Frazier
 *
 */
public class TimeoutEventImpl<T> implements StateMachine.TimedEvent {

	private final T payload;
	private final long deadline;

	/**
	 * Construct a timed event that holds a payload. 
	 * 
	 * @param payload  the object whose toString method will identify this event.
	 * @param deadline the last valid transition for this event
	 * @see StateMachine#getTransitionCount()
	 */
	public TimeoutEventImpl(T payload, long deadline) {
		this.payload = payload;
		this.deadline = deadline;
	}

	public T getPayload() {
		return payload;
	}

	/**
	 * Returns the name of this event.
	 */
	@Override
	public String toString() {
		if (payload == null) {
			return "";
		}
		return payload.toString();
	}

	@Override
	public long getTransitionDeadline() {
		return deadline;
	}
}
