package damlang;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import damlang.Expr.Assign;
import damlang.Expr.Binary;
import damlang.Expr.Grouping;
import damlang.Expr.Literal;
import damlang.Expr.Logical;
import damlang.Expr.Unary;
import damlang.Expr.Variable;
import damlang.Stmt.Block;
import damlang.Stmt.Expression;
import damlang.Stmt.If;
import damlang.Stmt.Let;
import damlang.Stmt.Print;
import damlang.Stmt.While;
import damlang.Stmt.Read;

public class DamGenerator implements Expr.Visitor<String>, Stmt.Visitor<String> {
	private DamEnvironment env = new DamEnvironment();
	private List<Stmt> statements;
	private List<String> ins = new ArrayList<>();
	
	private Map<Expr, String> t = new HashMap<>();
	private Map<String, String> javat = new HashMap<>();
	private Map<Object, String> conditionLabels = new HashMap<>();
	private int labelCounter = 0;
	
	
	private PrintWriter writer;
	
	private String jasminFilePath;

	public DamGenerator(List<Stmt> statements) {
		this.statements = statements;
		
		javat.put("double", "F");
		javat.put("str", "Ljava/lang/String;");
		javat.put("bool", "Z");
		
		env.define("args", "str"); // Note, we don't have arrays yet, so we'll just pretend it's a single string.
	}

	/**
	 * Creates textual bytecode file and then uses Jasmin to
	 * generate the actual Java classfile.  It also removes the
	 * Jasmin file before exiting.
	 * @param absoluteStem
	 */
	public void generate(String absoluteStem) {
		// Visit the statements (and expressions) to generate the instructions in 'ins'.
		for (Stmt s : statements) {
			s.accept(this);
		}

		// Write the file, including 'ins'.
		writeClassfile(absoluteStem);
	}
	
	private void writeClassfile(String absoluteStem) {		
		jasminFilePath = absoluteStem + ".j";
		
		String javaClassName = absoluteStem;
		int slash = absoluteStem.lastIndexOf(File.separator);
		if (slash >= 0) {
			javaClassName = absoluteStem.substring(slash + 1);
		}
		
		try {
			writer = new PrintWriter(jasminFilePath);
			writeHeader(javaClassName);
			writeCtor();
			writeMainStart();
			for (String inst : ins) {
				writer.println("\t" + inst);
			}
			writeMainEnd();
		} catch (IOException ioe) {
			DamCompiler.error("Error generating bytecode. " + ioe.getMessage());
		} finally {
			writer.close();
		}
		
		// Run jasmin on our .j file to create the .class file.
		jasmin.Main jasminMain = new jasmin.Main();
		jasminMain.run(new String[]{jasminFilePath});
		
		// Remove the .j and then move the .class to the same location as the .dam file.
		//new File(jasminFilePath).delete();
		Path sourceClass = Paths.get(javaClassName + ".class");
		Path targetClass = Paths.get(absoluteStem + ".class");
		try {
			Files.move(sourceClass, targetClass, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
            DamCompiler.error("Fatal error: " + e.getMessage());
        } 
	}
	
	private void writeHeader(String javaClassName) {
		writer.println(".class public " + javaClassName + "\n"
				+ ".super java/lang/Object");
	}
	
	private void writeCtor() {
		writer.println(".method public <init>()V\n"
				+ "    aload_0\n"
				+ "    invokenonvirtual java/lang/Object/<init>()V\n"
				+ "    return\n"
				+ ".end method");
	}
	
	private void writeMainStart() {
		writer.println(".method public static main([Ljava/lang/String;)V\n"
				+ "    .limit stack " + (ins.size()/2) + "\n"
				+ "    .limit locals " + (env.numVars()+1)); 
	}

	private void writeMainEnd() {
		writer.println("    return\n"
				+ ".end method");
	}

	@Override
	public String visitBlockStmt(Block stmt) {
		for (Stmt st : stmt.statements) {
			st.accept(this);
		}

		return null;
	}

	@Override
	public String visitExpressionStmt(Expression stmt) {
		stmt.expression.accept(this);

		return null;
	}

	@Override
	public String visitIfStmt(If stmt) {
		String thenLabel = "THEN" + labelCounter;
		String elseLabel = "ELSE" + labelCounter;
		String endLabel = "END" + labelCounter++;

		if (stmt.elseBranch == null) {
			conditionLabels.put(stmt.condition, endLabel);
		} else {
			conditionLabels.put(stmt.condition, elseLabel);
		}
		stmt.condition.accept(this);
		ins.add(thenLabel + ":");
		stmt.thenBranch.accept(this);

		if (stmt.elseBranch != null) {
			ins.add("goto " + endLabel);
			ins.add(elseLabel + ":");
			stmt.elseBranch.accept(this);
		}

		ins.add(endLabel + ":");
		return null;
	}

	@Override
	public String visitPrintStmt(Print stmt) {
		ins.add("getstatic java/lang/System/out Ljava/io/PrintStream;\n");

		stmt.expression.accept(this);
		
		String exprType = t.get(stmt.expression);
		String javaType = javat.get(exprType);

		ins.add("invokevirtual java/io/PrintStream/println("
				+ javaType + ")V");

		return null;
	}

	@Override
	public String visitReadStmt(Read stmt){
		// Check if the variable is already defined in the environment.
		// It should return an DamCompiler error when the variable does not exist.
		env.get(stmt.name);

		// Assign the type of the variable as "str" because we are reading a string as input
		env.assign(stmt.name, "str");

		// Save the result of the variable
		int varIndex = env.getIndex(stmt.name);

		// Create a Scanner object
		ins.add("new java/util/Scanner\n");
		ins.add("dup\n");

		// Access System.in
		ins.add("getstatic java/lang/System/in Ljava/io/InputStream;\n");

		// Invoke the constructor of the Scanner class to initialize it with System.in
		ins.add("invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V\n");

		// Push to the stack the appropriate value
		ins.add("invokevirtual java/util/Scanner/nextLine()Ljava/lang/String;\n");

		// Store the result of the input in the variable at the given index.
		ins.add("astore " + varIndex);

		return null;
	}

	@Override
	public String visitLetStmt(Let stmt) {
		stmt.initializer.accept(this);
		String rhsType = t.get(stmt.initializer);
		env.define(stmt.name.lexeme, rhsType);
		int varIndex = env.getIndex(stmt.name);

		if (rhsType.equals("double")) {
			ins.add("fstore " + varIndex);
		} else if (rhsType.equals("str")) {
			ins.add("astore " + varIndex);
		} else if (rhsType.equals("bool")) {
			ins.add("istore " + varIndex);
		}
		return null;
	}

	@Override
	public String visitWhileStmt(While stmt) {
		// Create labels for the start and end of the loop.
		String startLabel = "START" + labelCounter;
		String endLabel = "END" + labelCounter++;

		// Set up where the loop starts
		ins.add(startLabel + ":");

		// Store the endLabel for evualion purposes
		conditionLabels.put(stmt.condition, endLabel);

		// Visit the condition
		stmt.condition.accept(this);

		// If the condition is true, visit the body of the loop
		stmt.body.accept(this);

		// After you are done executing the body, branch back to the startLabel
		ins.add("goto " + startLabel);

		// Update the endlabel as the program keeps looping.
		ins.add(endLabel + ":");

		return null;
	}

	@Override
	public String visitBinaryExpr(Binary expr) {
		expr.left.accept(this);
		expr.right.accept(this);
		String ltype = t.get(expr.left);
		String rtype = t.get(expr.right);
		if (! ltype.equals(rtype)) {
			DamCompiler.error("Type mismatch on line " + expr.operator.line
					+ ".  Cannot apply " + expr.operator.lexeme + " to '"
					+ ltype + "' and '" + rtype + "'.");
		}
		
		switch (expr.operator.type) {
		case TokenType.PLUS:
		case TokenType.MINUS:
		case TokenType.STAR:
		case TokenType.SLASH:
			if (ltype.equals("double")) {
				if (expr.operator.type == TokenType.PLUS)		ins.add("fadd");
				else if (expr.operator.type == TokenType.MINUS)	ins.add("fsub");
				else if (expr.operator.type == TokenType.STAR)	ins.add("fmul");
				else if (expr.operator.type == TokenType.SLASH)	ins.add("fdiv");
			} else if (ltype.equals("str")) {
				if (expr.operator.type == TokenType.PLUS) {
					ins.add("invokevirtual java/lang/String/concat("
							+ "Ljava/lang/String;)Ljava/lang/String;");
				} else {
					DamCompiler.error("Cannot apply " + expr.operator.lexeme + " to str.");
				}
			}
			
			t.put(expr, ltype);
			break;

			case TokenType.BANG_EQUAL:
			case TokenType.EQUAL_EQUAL:
			case TokenType.GREATER:
			case TokenType.GREATER_EQUAL:
			case TokenType.LESS:
			case TokenType.LESS_EQUAL:
				t.put(expr, "bool");

				if (ltype.equals("double")){
					ins.add("fcmpl");

					String jumpLabel = conditionLabels.get(expr);
					if (expr.operator.type == TokenType.BANG_EQUAL)		ins.add("ifeq " + jumpLabel);
					if (expr.operator.type == TokenType.EQUAL_EQUAL)	ins.add("ifne " + jumpLabel);
					if (expr.operator.type == TokenType.GREATER)		ins.add("ifle " + jumpLabel);
					if (expr.operator.type == TokenType.GREATER_EQUAL)	ins.add("iflt " + jumpLabel);
					if (expr.operator.type == TokenType.LESS)			ins.add("ifge " + jumpLabel);
					if (expr.operator.type == TokenType.LESS_EQUAL)		ins.add("ifgt " + jumpLabel);
				} else if (ltype.equals("str")) {

				} else if (ltype.equals("bool")) {

				}
				break;
			default:
		}
		
		
		return null;
	}

	@Override
	public String visitGroupingExpr(Grouping expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitLiteralExpr(Literal expr) {
		if (expr.value instanceof Double) {
			t.put(expr, "double");
			ins.add("ldc " + expr.value);
		} else if (expr.value instanceof String) {
			t.put(expr, "str");
			ins.add("ldc \"" + expr.value + "\"");
		} else if (expr.value instanceof Boolean) {
			t.put(expr, "bool");
			if (expr.value.equals(false)){
				ins.add("iconst_0");
			}else {
				ins.add("iconst_1");
			}
		}
		return null;
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		String type = env.get(expr.name);
		t.put(expr, type);
		
		if (type.equals("double")) {
			ins.add("fload " + env.getIndex(expr.name));
		} else if (type.equals("str")) {
			ins.add("aload " + env.getIndex(expr.name));
		} else if (type.equals("bool")) {
			ins.add("iload " + env.getIndex(expr.name));
		}
		return null;
	}

	@Override
	public String visitUnaryExpr(Unary expr) {
		expr.right.accept(this);
		String rtype = t.get(expr.right);

		if (expr.operator.type == TokenType.BANG) {
			if (rtype.equals("bool")){
				ins.add("iconst_1"); // Push '1' onto the stack (to XOR with)
				ins.add("ixor"); // XOR the value 1 to negate it
				t.put(expr, rtype); // Updates the type of the operator
			} else {
				DamCompiler.error("Cannot apply '!' to non-boolean type.");
			}
		}

		else if (expr.operator.type == TokenType.MINUS) {
			if (rtype.equals("double")) {
					ins.add("fneg");
					t.put(expr, rtype); // Updates the type of the operator.
				} else{
					DamCompiler.error("Csnnot negate a non double type.");
				}
			}

		return null;
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		// Visit the right side value
		expr.right.accept(this);

		// Get the type of the right side value
		String rhsType = t.get(expr.right);

		// Check if the variable is already defined in the environment.
		// It should return an DamCompiler error when the variable does not exist.
		env.get(expr.name);

		// Update the value
		env.assign(expr.name, rhsType);

		// Find the index of the variable
		int varIndex = env.getIndex(expr.name);

		// Store the result of the input in the variable at the given index.
		if (rhsType.equals("double")) {
			ins.add("fstore " + varIndex);
		} else if (rhsType.equals("str")) {
			ins.add("astore " + varIndex);
		} else if (rhsType.equals("bool")) {
			ins.add("istore " + varIndex);
		}


		return null;
	}

}
