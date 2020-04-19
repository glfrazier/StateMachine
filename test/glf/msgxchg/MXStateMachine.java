package glf.msgxchg;

import java.util.Timer;
import java.util.TimerTask;

import glf.msgxchg.Message.Type;
import glf.statemachine.EventImpl;
import glf.statemachine.State;
import glf.statemachine.StateMachine;
import glf.statemachine.Transition;

/**
 * A three-state state machine:
 * <ol>
 * <li>The initial state transitions to the process-response state without an
 * input. A request is sent to the other node in the transition.</li>
 * <li>The process response state transitions to itself when it receives a
 * response. A new request is sent to the other node in the transition. In other
 * words, this state loops to itself indefinitely as long as the other node
 * responds to requests.</li>
 * <li>The process request state transitions to the timeout state if the other
 * node does not respond to a request before the timeout occurs.</li>
 * </ol>
 * 
 * @author Greg Frazier
 *
 */
public class MXStateMachine extends StateMachine {

	public static final State INITIAL_STATE = new State("InitialState");
	public static final State PROCESS_RESPONSE = new State("ProcessResponse");
	public static final State TIMEOUT = new State("RequestTimedOut");

	private MessageExchanger mx;
	private int otherPort;
	private Thread thread;
	private Timer timer = new Timer(true);

	/**
	 * A state machine has a name, a start state, and a set of transitions between
	 * its states. This constructor sets those things up. In addition, it holds a
	 * reference to the MessageExchanger that it is associated with and the port of
	 * the remote MessageExchanger that it is sending messages to.
	 * 
	 * @param mx        the MX that this state machine is providing protocol
	 *                  services to.
	 * @param otherPort the port of the remote MX that this state machine is
	 *                  sending/receiving messages to/from.
	 */
	public MXStateMachine(MessageExchanger mx, int otherPort) {
		super("MXStateMachine");
		this.mx = mx;
		this.otherPort = otherPort;
				
		this.addTransition(new Transition(INITIAL_STATE, makeRequestAction(this), PROCESS_RESPONSE));
		this.addTransition(new Transition(PROCESS_RESPONSE, new EventImpl<String>(Message.Type.RESPONSE.toString()),
				makeRequestAction(this), PROCESS_RESPONSE));
		this.addTransition(new Transition(PROCESS_RESPONSE, getTimeoutEvent(), timeoutAction(this), TIMEOUT));
		this.setStartState(INITIAL_STATE);
	}

	/**
	 * Grab the thread in which the state machine is being processed. Used by the
	 * timeout transition to interrupt the thread, which presumably is blocked on a
	 * socket read.
	 */
	@Override
	public void begin() {
		thread = Thread.currentThread();
		super.begin();
	}

	private Action makeRequestAction(MXStateMachine machine) {
		return new Action() {

			@Override
			public void act(Transition t) {
				Event event = t.getEvent();
				int count = 0;
				if (event != null) {
					@SuppressWarnings("unchecked")
					EventImpl<Message> ei = (EventImpl<Message>) event;
					Message response = ei.getPayload();
					count = response.getValue();
					System.out.println(mx + " received count=" + count + " from the other node.");
				}
				Message newRequest = new Message(Type.REQUEST, count, mx.getPort());
				try {
					mx.send(newRequest, otherPort);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				final StateMachine.TimedEvent timeout = machine.getTimeoutEvent();
				TimerTask tt = new TimerTask() {

					@Override
					public void run() {
						machine.receive(timeout);
					}

				};
				timer.schedule(tt, 1000); // time out on receiving a response in one second
			}

		};
	}

	private Action timeoutAction(MXStateMachine machine) {
		return new Action() {

			@Override
			public void act(Transition t) {
				System.out.println(mx + " timed out waiting for a response.");
				// We are transitioning from PROCESS_REQUEST to TIMEOUT, because we did not
				// receive a response to a request before the timeout expired. The thread that
				// the staet machine is running in is waiting for a packet that will never
				// arrive. Interrupt it, so that the thread can terminate.
				machine.thread.interrupt();
			}

		};
	}

}
