package com.github.glfrazier.statemachine.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Expression {
	
	public static Expression EMPTY_EXPRESSION = new Expression();

	List<String> tokens;
	String action;

	private static Pattern pattern = Pattern.compile("(?<first>\\w+)(\\s*\\.\\s*(?<second>\\w+))?(\\s*\\(\\s*(?<action>\\w+)\\s*\\))?");
	
	private Expression() {
		
	}

	private Expression(String inString) {
		Matcher m = pattern.matcher(inString.trim());
		if (!m.matches()) {
			throw new IllegalArgumentException("Input <" + inString + "> does not match <state{.input}{(action)}>");
		}
		tokens = new ArrayList<String>(2);
		tokens.add(m.group("first"));
		if (m.group("second") != null) {
			tokens.add(m.group("second"));
		}
		if (m.group("action") != null) {
			action = m.group("action");
		}
	}
	
	public static Expression getExpression(String inString) {
		if (inString == null) return EMPTY_EXPRESSION;
		if (inString.trim().equals("")) return EMPTY_EXPRESSION;
		return new Expression(inString);
	}

	public List<String> getTokens() {
		return tokens;
	}

	public String getAction() {
		return action;
	}

}
