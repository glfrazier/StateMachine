package glf.statemachine;

/**
 * A helper class that allows one to easily wrap an object into a StateMachine
 * Event. The name of the event is the payload's toString() result. A null
 * payload will return the empty string.
 * 
 * @author Greg Frazier
 *
 */
public class EventImpl<T> implements StateMachine.Event {

	private final T payload;

	/**
	 * Construct an Event that holds the specified payload.
	 * 
	 * @param payload the item resides in the event
	 * @see #getPayload()
	 */
	public EventImpl(T payload) {
		this.payload = payload;
	}

	/**
	 * Extract the payload from the Event.
	 * 
	 * @return the payload
	 */
	public T getPayload() {
		return payload;
	}

	/**
	 * Returns <code>payload.toString()</code>, or "" if the payload is
	 * <code>null</code>.
	 */
	@Override
	public String toString() {
		if (payload == null) {
			return "";
		}
		return payload.toString();
	}
}
