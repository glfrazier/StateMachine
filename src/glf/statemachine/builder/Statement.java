package glf.statemachine.builder;

import java.util.ArrayList;
import java.util.List;

public class Statement {

	public static final Statement EMPTY_EXPRESSION = new Statement();
	private static final String[] EMPTY_ARRAY = { "" };
	String lhs;
	List<Expression> rhs;

	private Statement() {
	}

	public Statement(String string) {
		String[] sides = string.split(":=");
		if (sides.length > 2) {
			throw new IllegalArgumentException(
					"Badly-formatted statement, should be: <lhs> := <rhs>. {" + string + "}");
		}
		lhs = sides[0].trim();
		String[] elements = null;
		if (sides.length == 1) {
			elements = EMPTY_ARRAY;
		} else {
			elements = sides[1].split("\\|");
		}
		rhs = new ArrayList<Expression>(elements.length);
		for (String e : elements) {
			rhs.add(Expression.getExpression(e));
		}
	}

	public String getLHS() {
		return lhs;
	}

	public List<Expression> getRHS() {
		return rhs;
	}
}
