package com.github.glfrazier.msgxchg;

import java.io.Serializable;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static enum Type {REQUEST, RESPONSE};
	
	Type type;
	
	int i;
	
	int port;
	
	public Message(Type type, int value, int port) {
		this.type = type;
		this.i = value;
		this.port = port;
	}
	
	public int getValue() {
		return i;
	}
	
	public int getPort() {
		return port;
	}
	
	public String toString() {
		return type.toString();
	}

}
