package AST.Visitor;

import java.util.*;

import TypeNodes.*;

import AST.*;
import Symtab.*;

public class CodeTranslateVisitor implements Visitor {

	private List<String> code;
	private String currentClass;
	private String currentMethod;
	private TypeVisitor declaredTypes;
	private Map<String, Integer> currentMethodParameters;
	private Map<String, Integer> currentMethodVariables;
	private int lastLabel;
	private Map<String, Map> vTable;
	private String lastSeenType;

	public CodeTranslateVisitor(TypeVisitor declaredTypes) {
		super();
		this.code = new ArrayList<String>();
		this.currentClass = null;
		this.currentMethod = null;
		this.currentMethodParameters = null;
		this.declaredTypes = declaredTypes;
		this.lastLabel = 0;
		this.vTable = null;
		this.lastSeenType = null;
	}

	public List<String> getCode() {
		return code;
	}

	private String getLabel() {
		String rv = "L" + lastLabel;
		++lastLabel;
		return rv;
	}

	/*
	 * vTable: key=class name, value=(method name=>slot number)
	 */
	private void createVTables() {
		this.vTable = new HashMap<String, Map>();

		Map classes = this.declaredTypes.getClasses();
		Iterator cls_it = classes.entrySet().iterator();
		while(cls_it.hasNext()) {
			Map.Entry clsMapEntry = (Map.Entry)cls_it.next();

			String clsName = (String)clsMapEntry.getKey();
			ClassNode clsNode = (ClassNode)clsMapEntry.getValue();

			Map methods = collectVtableMethods(clsName, clsNode, classes);

			List<ClassNode> clsRel = createClsRelList(clsNode, classes);
			HashMap<String, Integer> clsVTable = constructTableEntry(clsName,
					methods,
					clsRel,
					clsNode);
			this.vTable.put(clsName, clsVTable);
		}
	}

	/*
	 * clsRel is used to start formatting vtable entry based on older classes
	 *
	 *      VTable Representation:
	 *          Foo$$:
	 *              .long 0 # null parent   [slot 0]
	 *              .long Foo$MethodA       [slot 1]
	 *              .long Foo$MethodB       [slot 2]
	 *          Bar$$
	 *              .long Foo$$             [slot 0]
	 *              .long Foo$MethodA       [slot 1]
	 *              .long Bar$MethodB       [slot 2]
	 *
	 * @return: HashMap, key=method, value=slot #
	 */
	private HashMap<String, Integer> constructTableEntry(String clsName,
														 Map<String, String> methods,
														 List<ClassNode> clsRel,
														 ClassNode clsNode) {
		HashMap<String, Integer> clsVTable = new HashMap<String, Integer>();
		int slotNumber = 1;

		String clsParent = "";
		if(clsNode instanceof ClassWithParentNode)
			clsParent = ((ClassWithParentNode)clsNode).getParent() + "$$";
		else
			clsParent = "0";
		code.add(clsName + "$$:");
		code.add("    .long " + clsParent);

		HashSet<String> recordedMethods = new HashSet<String>();

		List<ClassNode> clsRelList = clsRel;
		Iterator clsRel_it = clsRelList.iterator();
		while(clsRel_it.hasNext()) {
			ClassNode currCls = (ClassNode)clsRel_it.next();
			Map<String, MethodNode> currMeth = getMethods(currCls);
			Iterator currMeth_it = currMeth.keySet().iterator();
			while(currMeth_it.hasNext()) {
				String meth = (String)currMeth_it.next();
				if(!recordedMethods.contains(meth)) {
					String methEntry = methods.get(meth);
					code.add("    .long " + methEntry);
					clsVTable.put(meth, new Integer(slotNumber++));
					recordedMethods.add(meth);
				}
			}
		}
		return clsVTable;
	}

	/*
	 * Collects all the methods visible by a class, as well as the code
	 *      source of each method
	 */
	private Map<String, String> collectVtableMethods(String clsName,
													 ClassNode clsNode,
													 Map classes) {
		Map<String, String> methods = new HashMap<String, String>();

		String currClsName = clsName;
		ClassNode currCls = clsNode;

		while(currCls instanceof ClassWithParentNode) {
			Map<String, MethodNode> currMethods = getMethods(currCls);
			Iterator method_it = currMethods.keySet().iterator();
			while(method_it.hasNext()) {
				String methodName = (String)method_it.next();
				if(!methods.containsKey((String)methodName)) {
					methods.put(methodName, currClsName + "$" + methodName);
				}
			}

			currClsName = ((ClassWithParentNode)currCls).getParent();
			currCls = (ClassNode)classes.get(currClsName);
		}

		// finish adding methods from base class...
		Map<String, MethodNode> currMethods = getMethods(currCls);
		Iterator method_it = currMethods.keySet().iterator();
		while(method_it.hasNext()) {
			String methodName = (String)method_it.next();
			if(!methods.containsKey(methodName)) {
				methods.put(methodName, currClsName + "$" + methodName);
			}
		}
		return methods;
	}

	/*
	 * Get all methods associated with a class
	 */
	private Map<String, MethodNode> getMethods(ClassNode cls) {
		Map<String, MethodNode> methods = new HashMap<String, MethodNode>();

		Map<String, Node> allAttrs = cls.getMembers();
		Iterator allAttrs_it = allAttrs.entrySet().iterator();
		while(allAttrs_it.hasNext()) {
			Map.Entry attr_entry = (Map.Entry)allAttrs_it.next();
			if(attr_entry.getValue() instanceof MethodNode) {
				String methodName = (String)attr_entry.getKey();
				MethodNode method = (MethodNode)attr_entry.getValue();
				methods.put(methodName, method);
			}
		}
		return methods;
	}

	/*
	 * Construct List: This is used to backtrack to make sure we vtable
	 *      entries are aligned uniformly according the the ancestors.
	 *
	 *      [GreatGrandpa] -> [Grandpa] -> [pa] -> [me]
	 *          ^
	 *        HEAD
	 */
	private List<ClassNode> createClsRelList(ClassNode clsNode, Map classes) {
		ClassNode currClsNode = clsNode;
		Map<String, ClassNode> classMap = classes;

		List<ClassNode> clsRel = new LinkedList<ClassNode>();
		clsRel.add(clsNode);

		while(currClsNode instanceof ClassWithParentNode) {
			ClassNode nextClsNode = classMap.get(((ClassWithParentNode)
					currClsNode).getParent());
			clsRel.add(0, nextClsNode);
			currClsNode = nextClsNode;
		}
		return clsRel;
	}

	public Map<String, Integer> getMethodParameterOffsets(
			String className,
			String methodName) {
		Map<Integer, String> parameterPositions =
				((MethodNode)(
						declaredTypes
								.getClasses()
								.get(className)
								.getMembers()
								.get(methodName)))
						.getParameterPositions();
		Map<String, Integer> rv = new HashMap<String, Integer>();

		for (Map.Entry<Integer, String> entry :
				parameterPositions.entrySet()) {
			rv.put(entry.getValue(), entry.getKey());
		}

		return rv;
	}

	public Map<String, Integer> getMethodVariableOffsets(
			String className,
			String methodName) {
		String[] localVariables =
				((MethodNode)(
						declaredTypes
								.getClasses()
								.get(className)
								.getMembers()
								.get(methodName)))
						.getLocalVariables()
						.keySet()
						.toArray(new String[0]);
		Arrays.sort(localVariables);

		Map<String, Integer> rv = new HashMap<String, Integer>();
		int i = 0;
		for (String localvar : localVariables) {
			rv.put(localVariables[i], i);
			++i;
		}

		return rv;
	}

	public Map<String, Integer> getInstanceVariableOffsets(
			String className) {
		Map<String, ClassNode> classes = declaredTypes.getClasses();

		Map<String, Integer> rv = new HashMap<String, Integer>();
		int currentPosition = 1; // 0 has the vtable pointer
		ClassNode klass = classes.get(className);

		while (true) {
			for (Map.Entry<String, Node> entry :
					klass.getMembers().entrySet()) {
				if (!(entry.getValue() instanceof MethodNode)) {
					rv.put(entry.getKey(), currentPosition++);
				}
			}

			if (klass instanceof ClassWithParentNode) {
				klass = classes.get(((ClassWithParentNode)klass).getParent());
			} else {
				break;
			}
		}

		return rv;
	}

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
	}

	public void visit(Program n) {
		createVTables();

		n.m.accept(this);

		ClassDeclList classDeclarations = n.cl;
		int size = classDeclarations.size();
		for (int i = 0; i < size; ++i) {
			classDeclarations.get(i).accept(this);
		}
	}

	public void visit(MainClass n) {
		code.add("    .text");
		code.add("    .global asm_main");
		code.add("");
		code.add("asm_main:");
		code.add("pushq %rbp");
		code.add("movq %rsp, %rbp");
		n.s.accept(this);
		code.add("    ret");
	}

	public void visit(ClassDeclSimple n) {
		currentClass = n.i.s;

		VarDeclList variables = n.vl;
		int variablesCount = 0;
		if (variables != null) {
			variablesCount = variables.size();
		}
		for (int i = 0; i < variablesCount; ++i) {
			variables.get(i).accept(this);
		}

		MethodDeclList methods = n.ml;
		int methodsCount = 0;
		if (methods != null) {
			methodsCount = methods.size();
		}
		for (int i = 0; i < methodsCount; ++i) {
			methods.get(i).accept(this);
		}

		currentClass = null;
	}

	public void visit(ClassDeclExtends n) {
		currentClass = n.i.s;

		VarDeclList variables = n.vl;
		int variablesCount = 0;
		if (variables != null) {
			variablesCount = variables.size();
		}
		for (int i = 0; i < variablesCount; ++i) {
			variables.get(i).accept(this);
		}

		MethodDeclList methods = n.ml;
		int methodsCount = 0;
		if (methods != null) {
			methodsCount = methods.size();
		}
		for (int i = 0; i < methodsCount; ++i) {
			methods.get(i).accept(this);
		}

		currentClass = null;
	}

	public void visit(VarDecl n) { }

	public void visit(MethodDecl n) {
		currentMethod = n.i.s;

		code.add(currentClass + "$" + currentMethod + ":");
		code.add("    pushq %rbp");
		code.add("    movq %rsp, %rbp");
		if (n.vl != null) {
			code.add("    subq $" + (8 * n.vl.size()) + ", %rsp");
		}
		code.add("    pushq %rcx");

		currentMethodParameters = new HashMap<String, Integer>();
		FormalList params = n.fl;
		int paramsCount = 0;
		if (params != null) {
			paramsCount = params.size();
		}
		for (int i = 0; i < paramsCount; ++i) {
			currentMethodParameters.put(params.get(i).i.s, i);
		}

		currentMethodVariables = new HashMap<String, Integer>();
		VarDeclList localVariables = n.vl;
		int variablesCount = 0;
		if (localVariables != null) {
			variablesCount = localVariables.size();
		}
		for (int i = 0; i < variablesCount; ++i) {
			currentMethodVariables.put(localVariables.get(i).i.s, i);
		}

		StatementList stmts = n.sl;
		int stmtsCount = 0;
		if (stmts != null) {
			stmtsCount = stmts.size();
		}
		for (int i = 0; i < stmtsCount; ++i) {
			stmts.get(i).accept(this);
		}

		// Return value. Left at rax.
		n.e.accept(this);

		if (n.vl != null) {
			code.add("    addq $" + (1 + 8 * n.vl.size()) + ", %rsp");
		}
		code.add("    movq %rbp, %rsp");
		code.add("    popq %rbp");
		code.add("    ret");

		currentMethodParameters = null;
		currentMethod = null;
	}

	public void visit(Formal n) { }

	public void visit(IntArrayType n) { }

	public void visit(BooleanType n) { }

	public void visit(IntegerType n) { }

	public void visit(IdentifierType n) { }

	public void visit(Block n) {
		StatementList stmts = n.sl;
		int size = stmts.size();
		for (int i = 0; i < size; ++i) {
			stmts.get(i).accept(this);
		}
	}

	public void visit(If n) {
		String labelElse = getLabel();
		String labelEnd = getLabel();

		n.e.accept(this);
		code.add("    cmpq $0, %rax");
		code.add("    je " + labelElse);
		n.s1.accept(this);
		code.add("    jmp " + labelEnd);
		code.add(labelElse + ":");
		n.s2.accept(this);
		code.add(labelEnd + ":");
	}

	public void visit(While n) {
		String labelStart = getLabel();
		String labelTest = getLabel();

		code.add("    jmp " + labelTest);
		code.add(labelStart + ":");
		n.s.accept(this);
		code.add(labelTest + ":");
		n.e.accept(this);
		code.add("    cmpq $0, %rax");
		code.add("    jne " + labelStart);
	}

	public void visit(Print n) {
		n.e.accept(this);
		code.add("    pushq %rax");
		code.add("    call put");
		code.add("    addq $8, %rsp");
		code.add("    movq (%rsp), %rcx");
	}

	public void visit(Assign n) {
		n.e.accept(this);

		String nid = n.i.s;

		Map<String, Integer> paramOffsets =
				getMethodParameterOffsets(currentClass, currentMethod);
		Map<String, Integer> localVars =
				getMethodVariableOffsets(currentClass, currentMethod);
		Map<String, Integer> instanceVars =
				getInstanceVariableOffsets(currentClass);

		if (paramOffsets.containsKey(nid)) {
			int offset = 8 * (1 + paramOffsets.size() - paramOffsets.get(nid));
			code.add("    # parameter " + nid);
			code.add("    movq %rax, " + offset + "(%rbp)");
		} else if (localVars.containsKey(nid)) {
			code.add("    # local var " + nid);
			code.add("    movq %rax, " +
					(-8 * (1 + localVars.get(nid))) +
					"(%rbp)");
		} else if (instanceVars.containsKey(nid)) {
			code.add("    # instance var " + nid);
			code.add("    movq %rax, " +
					(8 * instanceVars.get(nid)) +
					"(%rcx)");
		} else {
			System.err.println("SHOULD NOT HAVE GOTTEN HERE!!");
		}
	}

	public void visit(ArrayAssign n) {
		n.e2.accept(this);
		code.add("    pushq %rax");

		n.e1.accept(this);
		code.add("    pushq %rax");

		String nid = n.i.s;

		Map<String, Integer> paramOffsets =
				getMethodParameterOffsets(currentClass, currentMethod);
		Map<String, Integer> localVars =
				getMethodVariableOffsets(currentClass, currentMethod);
		Map<String, Integer> instanceVars =
				getInstanceVariableOffsets(currentClass);

		code.add("    popq %rdx");
		code.add("    popq %rax");
		if (paramOffsets.containsKey(nid)) {
			int offset = 8 * (1 + paramOffsets.size() - paramOffsets.get(nid));
			code.add("    # parameter " + nid);
			code.add("    movq " + offset + "(%rbp), %rcx");
		} else if (localVars.containsKey(nid)) {
			code.add("    # local var " + nid);
			code.add("    movq " +
					(-8 * (1 + localVars.get(nid))) +
					"(%rbp), %rcx");
		} else if (instanceVars.containsKey(nid)) {
			code.add("    # instance var " + nid);
			code.add("    movq " +
					(8 * instanceVars.get(nid)) +
					"(%rcx), %rcx");
		} else {
			System.err.println("SHOULD NOT HAVE GOTTEN HERE!!");
		}
		code.add("    shl $2, %rdx");
		code.add("    addq %rcx, %rdx");
		code.add("    # DEBUG rdx now contains array item addr");
		code.add("    movq %rax, (%rdx)");
		code.add("    movq (%rsp), %rcx");
	}

	public void visit(And n) {
		String labelFalse = getLabel();
		String labelEnd = getLabel();

		n.e1.accept(this);
		code.add("    cmpq $0, %rax");
		code.add("    je " + labelFalse);

		n.e2.accept(this);
		code.add("    cmpq $0, %rax");
		code.add("    je " + labelFalse);

		code.add("    movq $1, %rax");
		code.add("    jmp " + labelEnd);

		code.add(labelFalse + ":");
		code.add("    movq $0, %rax");

		code.add(labelEnd + ":");
	}

	public void visit(LessThan n) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		n.e1.accept(this);
		code.add("    pushq %rax");
		n.e2.accept(this);
		code.add("    popq %rdx");
		code.add("    cmpq %rax, %rdx");
		code.add("    jl " + labelTrue);
		code.add("    movq $0, %rax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movq $1, %rax");
		code.add(labelEnd + ":");
	}

	public void visit(Plus n) {
		n.e1.accept(this);
		code.add("    pushq %rax");
		n.e2.accept(this);
		code.add("    popq %rdx");
		code.add("    addq %rdx, %rax");
	}

	public void visit(Minus n) {
		n.e1.accept(this);
		code.add("    pushq %rax");
		n.e2.accept(this);
		code.add("    movq %rax, %rdx");
		code.add("    popq %rax");
		code.add("    subq %rdx, %rax");
	}

	public void visit(Times n) {
		n.e1.accept(this);
		code.add("    pushq %rax");
		n.e2.accept(this);
		code.add("    popq %rdx");
		code.add("    imul %rdx, %rax");
	}

	public void visit(ArrayLookup n) {
		n.e1.accept(this);
		code.add("    pushq %rax");
		n.e2.accept(this);
		code.add("    popq %rdx");
		code.add("    shl $2, %rax");
		code.add("    # DEBUG rax now contains array item addr");
		code.add("    addq %rdx, %rax");
		code.add("    movq (%rax), %rax");
	}

	public void visit(ArrayLength n) {
		n.e.accept(this);
		code.add("    movq -8(%rax), %rax");
	}

	public void visit(Call n) {
		// Add parameters to stack
		ExpList params = n.el;
		if (params != null) {
			for (int i = params.size() - 1; i >= 0; --i) {
				params.get(i).accept(this);
				code.add("    pushq %rax");
			}
		}
		// Get invocant
		n.e.accept(this);
		code.add("    movq %rax, %rcx");

		String typeOfReturnValue = null;
		Map<String, ClassNode> classes = declaredTypes.getClasses();
		if (classes.containsKey(lastSeenType)) {
			Node retType = null;
			ClassNode klass = classes.get(lastSeenType);
			while (klass != null) {
				Map<String, Node> members = klass.getMembers();
				if (members.containsKey(n.i.s)) {
					retType = ((MethodNode)(members.get(n.i.s))).getReturnType();
					break;
				} else if (klass instanceof ClassWithParentNode) {
					klass = classes.get(((ClassWithParentNode)klass).getParent());
				} else {
					break;
				}
			}

			if (retType instanceof ClassNode) {
				typeOfReturnValue = ((ClassNode)retType).getName();
			}

		}

		Map<String, Integer> clsVTable;
		clsVTable = (Map<String, Integer>)this.vTable.get(lastSeenType);
		int slotNumber = (Integer)clsVTable.get(n.i.s);

		code.add("    movq (%rax), %rax");
		code.add("    addq $" + (slotNumber * 8) + ", %rax");
		code.add("    movq (%rax), %rax");
		code.add("    call *%rax");

		if (params != null) {
			code.add("    addq $" + (8 * params.size()) + ", %rsp");
		}
		code.add("    movq (%rsp), %rcx");

		if (typeOfReturnValue != null) {
			lastSeenType = typeOfReturnValue;
		}
	}

	public void visit(IntegerLiteral n) {
		code.add("    movq $" + n.i + ", %rax");
	}

	public void visit(True n) {
		code.add("    movq $1, %rax");
	}

	public void visit(False n) {
		code.add("    movq $0, %rax");
	}

	public void visit(IdentifierExp n) {
		String nid = n.s;

		Map<String, Integer> paramOffsets =
				getMethodParameterOffsets(currentClass, currentMethod);
		Map<String, Integer> localVars =
				getMethodVariableOffsets(currentClass, currentMethod);
		Map<String, Integer> instanceVars =
				getInstanceVariableOffsets(currentClass);

		Map<String, ClassNode> classes = declaredTypes.getClasses();
		ClassNode klass = classes.get(currentClass);
		MethodNode method = (MethodNode)(klass.getMembers().get(currentMethod));

		if (paramOffsets.containsKey(nid)) {
			int offset = 8 * (1 + paramOffsets.size() - paramOffsets.get(nid));
			code.add("    # parameter " + nid);
			code.add("    movq " + offset + "(%rbp), %rax");

			Node node = method.getParameters().get(paramOffsets.get(nid));
			lastSeenType = node.whoami;
		} else if (localVars.containsKey(nid)) {
			code.add("    # local var " + nid);
			code.add("    movq " +
					(-8 * (1 + localVars.get(nid))) +
					"(%rbp), %rax");

			Node node = method.getLocalVariables().get(nid);
			lastSeenType = node.whoami;
		} else if (instanceVars.containsKey(nid)) {
			code.add("    # instance var " + nid);
			code.add("    movq " +
					(8 * instanceVars.get(nid)) +
					"(%rcx), %rax");

			while (true) {
				for (Map.Entry<String, Node> entry :
						klass.getMembers().entrySet()) {
					Node val = entry.getValue();
					if (entry.getKey().equals(nid) &&
							val.getType() == NodeType.CLASS) {
						lastSeenType = val.whoami;
						break;
					}
				}

				if (klass instanceof ClassWithParentNode) {
					klass = classes.get(((ClassWithParentNode)klass).getParent());
				} else {
					break;
				}
			}
		} else {
			System.err.println("SHOULD NOT HAVE GOTTEN HERE!!");
		}
	}

	public void visit(This n) {
		code.add("    movq %rcx, %rax");

		lastSeenType = currentClass;
	}

	public void visit(NewArray n) {
		n.e.accept(this);
		code.add("    pushq %rax");
		code.add("    addq $1, %rax");
		code.add("    shl $2, %rax");
		code.add("    pushq %rax");
		code.add("    call malloc");
		code.add("    addq $8, %rsp");
		code.add("    popq %rdx");
		code.add("    movq %rdx, (%rax)");
		code.add("    movq (%rsp), %rcx");
		code.add("    addq $8, %rax");
	}

	public void visit(NewObject n) {
		int objectSize = 8; // Space for vtable pointer

		Map<String, ClassNode> classes =
				declaredTypes.getClasses();
		ClassNode klass = classes.get(n.i.s);
		while (true) {
			for (Node member : klass.getMembers().values()) {
				if (!(member instanceof MethodNode)) {
					objectSize += 8;
				}
			}

			if (klass instanceof ClassWithParentNode) {
				klass = classes.get(((ClassWithParentNode)klass).getParent());
			} else {
				break;
			}
		}

		code.add("    pushq $" + objectSize);
		code.add("    call malloc");
		code.add("    addq $8, %rsp");
		code.add("    movq (%rsp), %rcx");
		code.add("    leaq " + n.i.s + "$$, %rbx");
		code.add("    movq %rbx, (%rax)");

		lastSeenType = n.i.s;
	}

	public void visit(Not n) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		n.e.accept(this);
		code.add("    cmpq $0, %rax");
		code.add("    je " + labelTrue);
		code.add("    movq $0, %rax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movq $1, %rax");
		code.add(labelEnd + ":");
	}

	public void visit(Identifier n) {
	}
}