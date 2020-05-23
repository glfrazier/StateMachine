package com.github.glfrazier.msgxchg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.github.glfrazier.EventingSystem;
import com.github.glfrazier.msgxchg.Message.Type;
import com.github.glfrazier.statemachine.EventImpl;

public class MessageExchanger {

	private String name;
	DatagramSocket socket;
	int port;

	public MessageExchanger(String name) {
		this.name = name;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(this + " opened socket " + socket.getLocalPort());
		port = socket.getLocalPort();
	}

	/**
	 * Respond to three messages and then return.
	 * 
	 * @throws Exception if any invoked method throws an Exception
	 */
	public void beListener() throws Exception {
		for (int i = 0; i < 3; i++) {
			Message m = receive();
			if (m.type != Type.REQUEST) {
				System.err.println("We are supposed to be listening for requests!");
				System.exit(-1);
			}
			Thread.sleep(250);
			Message response = new Message(Type.RESPONSE, m.getValue() + 1, this.port);
			send(response, m.getPort());
		}
	}

	public String toString() {
		return name;
	}

	public void send(Message msg, int port) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(msg);
		oout.close();
		byte[] buffer = bout.toByteArray();
		DatagramPacket p = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), port);
		socket.send(p);
	}

	public Message receive() throws Exception {
		byte[] buffer = new byte[1024];
		DatagramPacket p = new DatagramPacket(buffer, buffer.length);
		socket.receive(p);
		ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
		ObjectInputStream oin = new ObjectInputStream(bin);
		Message m = (Message) oin.readObject();
		oin.close();
		return m;
	}

	public int getPort() {
		return port;
	}

	public static void main(String args[]) throws InterruptedException {
		boolean verbose = args.length > 0 && args[0].equals("-v");
		EventingSystem es = new EventingSystem("Eventing for MX");
		es.setVerbose(verbose);
		es.exitOnEmptyQueue(false);
		MessageExchanger mx1 = new MessageExchanger("mx1");
		MessageExchanger mx2 = new MessageExchanger("mx2");
		MXStateMachine machine = new MXStateMachine(mx2, es, mx1.getPort());
		machine.setVerbose(verbose);

		// Put each of the MessageExchangers into its own thread

		// The first one responds to three messages and then terminates
		Thread t1 = new Thread() {
			public void run() {
				try {
					mx1.beListener();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t1.start();
		
		Thread t3 = new Thread(es);
		t3.setDaemon(true);
		t3.start();

		// The second one is under the control of an MXStateMachine
		Thread t2 = new Thread() {

			/**
			 * The receive method on the DatagramSocket is not interrupted by the thread's
			 * interrupt method. So close the socket on an interrupt.
			 */
			@Override
			public void interrupt() {
				super.interrupt();
				mx2.socket.close();
			}

			@Override
			public void run() {
				try {
					mx2.processStateMachine(machine);
				} catch (Exception e) {
					System.out.println("mx2 terminating due to exception " + e);
				}
				System.out.println("mx2 state machine completed.");
			}
		};
		t2.start();
		t2.join();
		System.out.println("t2 terminated.");
		t1.join();
		System.out.println("t1 terminated. Exiting main().");
	}

	protected void processStateMachine(MXStateMachine machine) {
		machine.begin();
		while (!Thread.interrupted()// && machine.getCurrentState() != machine.finished
		) {
			Message m = null;
			try {
				m = receive();
			} catch (Exception e) {
				return;
			}
			// When we receive a message, we wrap it in an event and hand it to the state
			// machine for processing.
			machine.receive(new EventImpl<Message>(m, MXStateMachine.RESPONSE));
		}
	}
}
