/**
 * An example of a state machine used to implement a protocol. The
 * MessageExchanger sends UDP Datagrams to other MessageExchanger instances. It
 * operates in two modes: as a listener or as a sender. The listener responds to
 * three messages and terminates. The sender uses the MXStateMachine to
 * implement a three-state protocol.
 */
package glf.msgxchg;