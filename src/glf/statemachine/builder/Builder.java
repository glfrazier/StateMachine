package glf.statemachine.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Parse a context-free grammar and build (the skeleton of) a StateMachine.
 * 
 * This is not intended to be a competitor to or replacement of Bison or Yacc.
 * Rather, this is intended to be a simple way to bootstrap the generation of
 * state machines.
 * 
 * @author glfrazier
 *
 */
public class Builder {

	File inFile;
	File outDir;

	public Builder(String inFileName, String outDirname) {
		inFile = new File(inFileName);
		outDir = new File(outDirname);
	}

	public void build() throws IOException {
		if (!outDir.isDirectory()) {
			System.err.println(outDir + " is not a directory.");
			System.exit(-1);
		}
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String line = in.readLine();
		Properties props = new Properties();
		Set<Statement> statements = new HashSet<>();
		boolean parsingHeader = true;
		while (line != null) {
			try {
				line = line.trim();
				int comment = line.indexOf('#');
				if (comment >= 0) {
					line = line.substring(0, comment);
				}
				if (line.isEmpty()) {
					continue;
				}
				if (parsingHeader) {
					if (line.startsWith("=")) {
						parsingHeader = false;
						continue;
					}
					String tokens[] = line.split(":");
					if (tokens.length != 2) {
						throw new IllegalArgumentException(
								"The header lines' format is <name>:<value>, ending with a line of '='s.");
					}
					props.setProperty(tokens[0], tokens[1]);
					continue;
				}
				Statement s = new Statement(line);
				statements.add(s);
			} finally {
				line = in.readLine();
			}
		}
		in.close();
		String pkg = props.getProperty("package");
		if (pkg == null) {
			System.err.println("Failed to specify a 'package' property.");
			System.exit(-1);
		}
		String cls = props.getProperty("class");
		if (cls == null) {
			System.err.println("Failed to specify a 'class' property.");
			System.exit(-1);
		}
		String[] elements = pkg.split("\\.");
		for (String e : elements) {
			outDir = new File(outDir.getPath() + "/" + e);
		}
		outDir.mkdirs();
		File f = new File(outDir.getPath() + "/" + cls + ".java");
		PrintStream out = new PrintStream(f);
		build(props, statements, out);
		out.close();
		System.out.println("Created " + f);
	}

	public static void build(Properties properties, Set<Statement> statements, PrintStream out) {
		String name = properties.getProperty("name");
		if (name == null) {
			System.err.println("The 'name' property was not specified.");
			System.exit(-1);
		}
		String initialState = null;
		List<FourTuple> transitions = new ArrayList<>();
		Set<String> states = new HashSet<>();
		Set<String> events = new HashSet<>();
		Set<String> actions = new HashSet<>();
		for (Statement s : statements) {
			String dst = s.getLHS();
			states.add(dst);
			List<Expression> rhs = s.getRHS();
			if (rhs == null) {
				if (initialState != null) {
					throw new IllegalArgumentException(
							"The grammar has (at least) two initial states: " + initialState + " and " + dst);
				}
				initialState = dst;
				continue;
			}
			for (Expression e : rhs) {
				if (e == Expression.EMPTY_EXPRESSION) {
					if (initialState != null) {
						throw new IllegalArgumentException(
								"The grammar has (at least) two initial states: " + initialState + " and " + dst);
					}
					initialState = dst;
					continue;
				}
				List<String> sequence = e.getTokens();
				String src = sequence.get(0);
				states.add(src);
				String event = (sequence.size() == 2 ? sequence.get(1) : null);
				if (event != null) {
					events.add(event);
				}
				if (e.getAction() != null) {
					actions.add(e.getAction());
				}
				transitions.add(new FourTuple(src, event, e.getAction(), dst));
			}
		}
		if (initialState == null) {
			throw new IllegalArgumentException("The grammar has no initial state specified.");
		}

		StringBuilder header = new StringBuilder(template);
		int start = header.indexOf("PACKAGE");
		header.replace(start, start + "PACKAGE".length(), properties.getProperty("package"));
		start = header.indexOf("CLASS");
		header.replace(start, start + "CLASS".length(), properties.getProperty("class"));
		out.println(header.toString());

		for (String s : states) {
			out.println("State " + s + " = new State(\"" + s + "\");");
		}
		for (String e : events) {
			out.println("StateMachine.Event " + e + " = new EventImpl(\"" + e + "\");");
		}
		for (String a : actions) {
			out.println("StateMachine.Action " + a + " = new StateMachine.Action() {");
			out.println("  public void act(Transition t) { System.out.println(t); }");
			out.println("};");
		}
		out.println("Set<Transition> transitions = new HashSet<>();");
		for (FourTuple tuple : transitions) {
			out.println("transitions.add(new Transition(" + tuple + "));");
		}
		out.println("return new StateMachine(\"" + name + "\", transitions, " + initialState + ");");
		out.println("}\n\n}");
	}

	private static final String template = "package PACKAGE; \n" //
			+ "import glf.statemachine.*;\n" //
			+ "import java.util.*;\n" //
			+ "public class CLASS {\n" //
			+ "public static StateMachine buildStateMachine() {\n";//

	private static class FourTuple {
		public FourTuple(String src, String event, String action, String dst) {
			this.src = src;
			this.event = event;
			this.action = action;
			this.dst = dst;
		}

		public final String src;
		public final String event;
		public final String action;
		public final String dst;

		public String toString() {
			return src + ", " + event + ", " + action + ", " + dst;
		}
	}

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		if (!file.exists()) {
			System.err.println(file + " does not exist.");
			FileOutputStream fout = new FileOutputStream(file);
			fout.close();
			return;
		} else {
			System.out.println("Parsing " + file.getCanonicalPath());
		}
		Builder builder = new Builder(args[0], args[1]);
		builder.build();
	}
}
