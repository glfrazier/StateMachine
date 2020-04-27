package glf.msgxchg;

import java.util.Timer;
import java.util.TimerTask;

import glf.event.Event;
import glf.event.EventingSystem;
import glf.msgxchg.Message.Type;
import glf.statemachine.EventImpl;
import glf.statemachine.State;
import glf.statemachine.State.Action;
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
	
	public static final String RESPONSE = "RESPONSE";

	public State makeRequest = new State("MakeRequest", makeRequestAction(), this);
	public State finished = new State("RequestTimedOut", timeoutAction(), this);

	private MessageExchanger mx;
	private int otherPort;
	private Thread thread;
	private Timer timer = new Timer(true);
	private Message receivedMessage;

	/**
	 * Set the state machine's name, start state, and the transitions between its
	 * states. In addition, set the reference to the MessageExchanger that it is
	 * associated with and the port of the remote MessageExchanger that it is
	 * sending messages to.
	 * 
	 * @param mx        the MX that this state machine is providing protocol
	 *                  services to.
	 * @param otherPort the port of the remote MX that this state machine is
	 *                  sending/receiving messages to/from.
	 */
	public MXStateMachine(MessageExchanger mx, EventingSystem es, int otherPort) {
		super("MXStateMachine", es);
		this.mx = mx;
		this.otherPort = otherPort;

		// Specify which state we start in
		this.setStartState(makeRequest);

		// Create the state transitions for this state machine

		// As long as we keep getting responses, keep making requests.
		this.addTransition(new Transition(makeRequest, RESPONSE, makeRequest));

		// If a request times out, we are finished
		this.addTransition(new Transition(makeRequest, getTimeoutEvent(), finished));

	}

	/**
	 * Use the invocation of the machine's begin() method to grab the thread in
	 * which the state machine is being processed. The timeout transition needs to
	 * be able to interrupt this thread.
	 */
	@Override
	public void begin() {
		thread = Thread.currentThread();
		super.begin();
	}

	private Action makeRequestAction() {
		return new Action() {

			@Override
			public void act(State s, Event event) {
				// The event that triggers this transition will have the received message as its
				// payload. Get the payload.
				// Unless this is the transition from the initial state, in which case the event
				// is null.
				int count = 0;
				if (event != null) {
					@SuppressWarnings("unchecked")
					EventImpl<Message> ei = (EventImpl<Message>) event;
					Message response = ei.getPayload();
					count = response.getValue();
					System.out.println(mx + " received count=" + count + " from the other node.");
				}
				// The action in this transition is to create and send a new request.
				Message newRequest = new Message(Type.REQUEST, count, mx.getPort());
				try {
					mx.send(newRequest, otherPort);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				// We also generate a timeout, in case the other side never responds to our
				// message. Note the use of the state machine getTimeoutEvent method, which
				// ensures both the correct name for the event and the correct deadline.
				final StateMachine.TimedEvent timeout = s.getMachine().getTimeoutEvent();
				TimerTask tt = new TimerTask() {

					@Override
					public void run() {
						// The timer's task is to deliver the timeout event to the state machine. Note
						// that, since this is a TimedEvent, the event will be ignored by the state
						// machine if the machine has already transitioned. I.e., it will be ignored if
						// a response was received from the other mx.
						s.getMachine().receive(timeout);
					}

				};
				timer.schedule(tt, 1000); // time out after one second
			}

		};
	}

	private Action timeoutAction() {
		return new Action() {

			@Override
			public void act(State s, Event e) {
				System.out.println(mx + " timed out waiting for a response.");
				// We are transitioning from PROCESS_REQUEST to TIMEOUT, because we did not
				// receive a response to a request before the timeout expired. The thread that
				// the staet machine is running in is waiting for a packet that will never
				// arrive. Interrupt it, so that the thread can terminate.
				MXStateMachine machine = (MXStateMachine)s.getMachine();
				machine.thread.interrupt();
			}

		};
	}

}
