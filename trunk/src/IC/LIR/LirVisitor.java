package IC.LIR;

import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java_cup.internal_error;

import IC.LiteralTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.LIR.ArithmeticInstruction.ArithmeticInstructionType;
import IC.LIR.CallInstruction.CallInstructionType;
import IC.LIR.ControlTransferInstruction.ControlTransferInstructionType;
import IC.LIR.DataTransferInstruction.DataTransferInstructionType;
import IC.LIR.LibraryInstruction.LibraryInstructionType;
import IC.LIR.LogicalInstruction.LogicalInstructionType;
import IC.LIR.Op.OpType;
import IC.Semantics.MethodSigType;
import IC.Semantics.TypeClass;
import IC.Semantics.TypeTable;
import IC.Semantics.SymbolClass.SymbolKind;

/**
 * Visitor which translates the AST tree to LIR functions
 */
public class LirVisitor implements Visitor {

	private String progName = "";
	private ArrayList<Instruction> list = new ArrayList<Instruction>();
	private HashMap<String, DispatchTable> dTables = new HashMap<String, DispatchTable>();
	private DispatchTable curDt = null;
	private String latestWhileStartLabel = "";
	private String latestWhileEndLabel = "";
	private Op dummyOp = new Op("Rdummy", OpType.Reg);

	// Global Variables for specific issues
	private boolean isAssignmentNewClass = false;
	private boolean isString = false;
	private String currentVisitedClassName;
	private Op assignmentLocation;
	private int assignmentIndex;
	private TypeClass curTypeClass;

	private HashMap<String, Op> objects = new HashMap<String, Op>();
	private Program prog;
	private TypeTable typeTable;

	private ArrayList<String> localParams = new ArrayList<String>();
	
	//Error labels:
	private String errorLabelNullReference = "_null_reference";
	private String errorLabelIllegalArrayLocation = "_illegal_arr_loc";
	private String errorLabelDevByZero = "_dev_by_zero";
	private String errorLabelArrayNegativeAllocationSize = "_arr_neg_alloc_size";
	private boolean lval;

	/**
	 * Constructs a new LIR visitor.
	 * 
	 */
	public LirVisitor(String progName, TypeTable typetable) {
		this.progName = progName;
		typeTable = typetable;
	}

	public Object visit(Program program) {

		this.prog = program;

		for (ICClass icClass : program.getClasses()) {
			curDt = new DispatchTable(icClass.getName());
			dTables.put(icClass.getName(), curDt);

			if (icClass.hasSuperClass()) {
				curDt.setParent(dTables.get(icClass.getSuperClassName()));
			}

			for (Field field : icClass.getFields())
				curDt.addField(field);
			for (Method method : icClass.getMethods()) {
				if (!method.getName().equals("main")) {
					curDt.addMethod(method);
				}
			}
		}

		for (ICClass icClass : program.getClasses()) {
			this.currentVisitedClassName = icClass.getName();
			icClass.accept(this);

		}
		list.add(0, new StringInstruction());
		list.add(0, new StringInstruction("######################"));
		Iterator<DispatchTable> it = dTables.values().iterator();
		while (it.hasNext()) {
			list.add(0, it.next().getInstruction());
		}
		list.add(0, new StringInstruction("# DISPATCH VECTORS"));
		list.add(0, new StringInstruction("######################"));

		list.add(0, new StringInstruction());
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction(StringLiteral.prettyPrint()));
		
		list.add(0,new StringInstruction(errorLabelDevByZero.substring(1) + ":" + 
										 " \"Devision by zero. Program will now exit. \""));
		
		list.add(0,new StringInstruction(errorLabelIllegalArrayLocation.substring(1) + ":" + 
		 " \"Illegal array location. Program will now exit. \""));
		
		list.add(0,new StringInstruction(errorLabelNullReference.substring(1) + ":" + 
		 " \"Null reference. Program will now exit. \""));
		
		list.add(0,new StringInstruction(errorLabelArrayNegativeAllocationSize.substring(1) + ":" + 
		 " \"Negative or zero array size allocation. Program will now exit. \""));
		
		list.add(0, new StringInstruction("# STRING LITERALS"));
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction());

		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction("# Program " + progName));
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction());
		
		addErrorLabelsToList();
		
		return list;
	}

	public Object visit(ICClass icClass) {

		curDt = dTables.get(icClass.getName());

		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods()) {
			method.accept(this);
			list.add(new ControlTransferInstruction(new Op("9999",
					OpType.Immediate), ControlTransferInstructionType.Return));
		}

		return null;
	}

	public Object visit(PrimitiveType type) {

		if (type.getDimension() > 0)
			// TBD: do something here?
			;

		return null;
	}

	public Object visit(UserType type) {

		if (type.getDimension() > 0)
			// TBD: do something here?
			;

		return null;
	}

	public Object visit(Field field) {

		field.getType().accept(this);

		return null;
	}

	public Object visit(LibraryMethod method) {

		return null;
	}

	public Object visit(Formal formal) {

		return null;
	}

	public Object visit(VirtualMethod method) {

		list.add(new Label(method.enclosingScope().getParent().getId() + "_"
				+ method.getName(), "Method " + method.getName() + ":"));

		method.getType().accept(this);
		localParams.clear();
		for (Formal formal : method.getFormals()) {
			formal.accept(this);
			localParams.add(formal.getName());
		}
		for (Statement statement : method.getStatements()) {
			isString = false;
			list.add(new Comment("Line " + statement.getLine() + ": "));
			System.out.println("LINE - " + statement.getLine());// TMP
			statement.accept(this);
			Register.reset();
		}
		localParams.clear();

		return null;
	}

	public Object visit(StaticMethod method) {

		if (method.getName().equals("main")) {
			list.add(new Label("ic_" + method.getName(), "Main Method"));
		} else {
			list.add(new Label( this.currentVisitedClassName + "_" + method.getName(), "Method " + method.getName()
					+ ":"));
		}

		method.getType().accept(this);

		localParams.clear();
		for (Formal formal : method.getFormals()) {
			formal.accept(this);
			localParams.add(formal.getName());
		}
		
		for (Statement statement : method.getStatements()) {
			isString = false;
			list.add(new Comment("Line " + statement.getLine() + ": "));
			System.out.println("LINE - " + statement.getLine());// TMP
			statement.accept(this);
			Register.reset();
		}

		if (method.getName().equals("main")) {
			list.add(new LibraryInstruction(new Op("__exit(0)",
					OpType.FuncHeader), dummyOp));
		}

		list.add(new Comment("Method " + method.getName() + " end"));
		
		localParams.clear();
		return null;
	}

	public Object visit(Assignment assignment) {

		int oldAssigmentIndex;
		Op oldAssignmentLoc;

		
		assignmentLocation = null;
		assignmentIndex = -1;
		int assignmentListIndex = -1;

		try {
			lval = true;
			Op loc = (Op) assignment.getVariable().accept(this);
			lval = false;
			assignmentListIndex = list.size() - 1;
			// Save location and current index
			oldAssigmentIndex = assignmentIndex;
			oldAssignmentLoc = assignmentLocation;

			Op value = (Op) assignment.getAssignment().accept(this);

			if (this.isAssignmentNewClass) {
				this.isAssignmentNewClass = false;
				String mangledName = Utils.getObjectsMapName(loc.getName(),
						assignment.enclosingScope().getId(),
						this.currentVisitedClassName);
				objects.put(mangledName, value);
			}

			// Copy value into location:
		/*	if (loc.getName().contains(".")) {
				Instruction ins = new DataTransferInstruction(value, loc,
						DataTransferInstructionType.MoveField);
				ins.setOptComment("(Assignment statement)");
				list.add(ins);
			} */ 
			//else {
			//	list.remove(assignmentListIndex);
				/*
				 * Instruction ins = new DataTransferInstruction(value,
				 * loc,DataTransferInstructionType.Move);
				 * ins.setOptComment("(Assignment statement)"); list.add(ins);
				 */
			//}

			
			assert(oldAssignmentLoc != null);
			
			list.add(new Comment("TMP 1"));
			
			smartMove(value, oldAssignmentLoc);
			/*if (oldAssigmentIndex > -1)
			{
				list.add(new DataTransferInstruction(value,oldAssignmentLoc,DataTransferInstructionType.MoveArray).setOptComment("assigning val to loc"));
			}
			else
			{
				if(oldAssignmentLoc.getName().contains(".") || value.getName().contains("."))
				{
					if (value.getName().contains("."))
					{
						addNullReferenceCheckToList(value);
						list.add(new DataTransferInstruction(value,
								oldAssignmentLoc, DataTransferInstructionType.MoveField)
								.setOptComment("assigning val to loc"));
					}
					else
					{
						if (oldAssignmentLoc.getOpType() == OpType.Memory)
						{ 
							Op newreg = new Op(Register.getFreeReg(),OpType.Reg);
							Op varname = new Op(oldAssignmentLoc.getName().substring(0, oldAssignmentLoc.getName().indexOf("."))
									, OpType.Memory);
							int varpos = Integer.parseInt(oldAssignmentLoc.getName().substring(oldAssignmentLoc.getName().indexOf(".")+1));
							list.add(new DataTransferInstruction(varname,newreg,DataTransferInstructionType.Move));
							
							oldAssignmentLoc = new Op(newreg.getName() +"."+ varpos , OpType.Memory);
						}
						addNullReferenceCheckToList(oldAssignmentLoc);
						list.add(new DataTransferInstruction(value,
								oldAssignmentLoc, DataTransferInstructionType.MoveField)
								.setOptComment("assigning val to loc"));
					}
				}
				else
					list.add(new DataTransferInstruction(value,
						oldAssignmentLoc, DataTransferInstructionType.Move)
						.setOptComment("assigning val to loc"));
			}
			*/
			oldAssigmentIndex = -1;
			oldAssignmentLoc = null;

		} catch (Exception e) {
			System.out.println("casting error - need to implement something: " + e.getMessage()); // TMP!
		}

		return null;
	}

	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);
		return null;
	}

	public Object visit(Return returnStatement) {
		// if (returnStatement.getValue() == null)
		// list.add(new)
		if (!returnStatement.hasValue())
			return null;
		Op reg = (Op) returnStatement.getValue().accept(this);
		Op newregister = new Op(Register.getFreeReg(), OpType.Reg);
		
		//list.add(new DataTransferInstruction(reg, newregister,
			//	DataTransferInstruction.DataTransferInstructionType.Move));
		smartMove(reg, newregister);
	
		list.add(new ControlTransferInstruction(newregister,
				ControlTransferInstructionType.Return));
		return null;
	}

	public Object visit(If ifStatement) {
		// Set labels:
		String thenLabel = Jump.getNextJumpCounter();
		String elseLabel = Jump.getNextJumpCounter();
		String endLabel = Jump.getNextJumpCounter();

		// Check If's condition
		Op reg = (Op) ifStatement.getCondition().accept(this);

		// Compare outcome from If's condition
		// If reg == 1 then If's outcome is true, else false
		LogicalInstruction li = new LogicalInstruction(new Op("0",
				OpType.Immediate), reg, LogicalInstructionType.Compare);
		li.setOptComment("If's condition check");
		list.add(li);

		// If condition is true, jump to then label
		ControlTransferInstruction ctThen = new ControlTransferInstruction(
				new Op(elseLabel, OpType.Label),
				ControlTransferInstructionType.JumpTrue);
		ctThen.setOptComment("Conditional jump when If's outcome is true");
		list.add(ctThen);

	

		// If's then label
		list.add(new Label(thenLabel, "If's then label"));

		// If's then part
		ifStatement.getOperation().accept(this);

		// Then must jump to then label in order not to execute else's part
		ControlTransferInstruction ctEnd = new ControlTransferInstruction(
				new Op(endLabel, OpType.Label),
				ControlTransferInstructionType.Jump);
		ctEnd.setOptComment("Jump to end label(end of then part)");
		list.add(ctEnd);

		// Else label
		list.add(new Label(elseLabel, "If's else label"));
		
		if (ifStatement.hasElse()) {
			// Else code here
			ifStatement.getElseOperation().accept(this);
		}

		// End label
		list.add(new Label(endLabel, "End of If statement"));

		return null;
	}

	public Object visit(While whileStatement) {
		// Read latest while labels:
		String previousWhileStartLabel = this.latestWhileStartLabel;
		String previousWhileEndLabel = this.latestWhileEndLabel;

		// Add while label to instructions list and generate new labels
		String whileLabel = Jump.getNextJumpCounter();
		String falseWhileLabel = Jump.getNextJumpCounter();
		list.add(new Label(whileLabel, "While"));

		// Update LirVisitor for the new labels:
		this.latestWhileStartLabel = whileLabel;
		this.latestWhileEndLabel = falseWhileLabel;

		// Get condition evaluation outcome
		Op reg = (Op) whileStatement.getCondition().accept(this);

		// Check if outcome is true(1) or false(0)
		LogicalInstruction li = new LogicalInstruction(new Op("0",
				OpType.Immediate), reg, LogicalInstructionType.Compare);
		li.setOptComment("While condition check");
		list.add(li);

		// Add to list the JumpLe instruction
		ControlTransferInstruction ct = new ControlTransferInstruction(new Op(
				latestWhileEndLabel, OpType.Label),
				ControlTransferInstructionType.JumpTrue);
		ct.setOptComment("Conditional jump when while statement is false");
		list.add(ct);

		// If while condition is true, this code will be executed
		whileStatement.getOperation().accept(this);

		list.add(new ControlTransferInstruction( new Op(latestWhileStartLabel,OpType.Label) ,ControlTransferInstructionType.Jump));
		// Add new while false condition label
		list.add(new Label(falseWhileLabel, "False while condition"));

		// Restore older labels:
		this.latestWhileStartLabel = previousWhileStartLabel;
		this.latestWhileEndLabel = previousWhileEndLabel;

		// TBD: Fix the double __ print

		return null;
	}

	public Object visit(Break breakStatement) {
		// Upon break call, jump to innest while end label:
		ControlTransferInstruction ctBreak = new ControlTransferInstruction(
				new Op(this.latestWhileEndLabel, OpType.Label),
				ControlTransferInstructionType.Jump);
		ctBreak
				.setOptComment("Break statement. Jump to end of while statement label: "
						+ this.latestWhileEndLabel);
		list.add(ctBreak);

		return null;
	}

	public Object visit(Continue continueStatement) {
		// Upon continue statement, jump to closest while start label:
		ControlTransferInstruction ctContinue = new ControlTransferInstruction(
				new Op(this.latestWhileStartLabel, OpType.Label),
				ControlTransferInstructionType.Jump);
		ctContinue
				.setOptComment("Continue statement. Jump to the start of while statement label: "
						+ this.latestWhileStartLabel);
		list.add(ctContinue);

		return null;
	}

	public Object visit(StatementsBlock statementsBlock) {
		StringBuffer output = new StringBuffer();

		list.add(new Comment("Line " + statementsBlock.getLine() + ": "));

		output.append("Block of statements");

		for (Statement statement : statementsBlock.getStatements())
		{
			isString = false;
			output.append(statement.accept(this));
			Register.reset();
		}
		return null;
	}

	private String varFormatter(String name, String currClassName, int scopeId) {
		return name + "_" + currClassName + "_" + scopeId;
	}

	public Object visit(LocalVariable localVariable) {

		if (localVariable.hasInitValue()) {
			Op init = (Op) localVariable.getInitValue().accept(this);
			Op var;
			if (localParams.contains(localVariable.getName()))
				var = new Op(localVariable.getName(), OpType.Memory);
			else
				var = new Op(varFormatter(localVariable.getName(),
						this.currentVisitedClassName, localVariable
								.enclosingScope().getNumid(localVariable.getName())), OpType.Memory);
			if (this.isAssignmentNewClass) {
				this.isAssignmentNewClass = false;
				String mangledName = Utils.getObjectsMapName(localVariable
						.getName(), localVariable.enclosingScope().getId(),
						this.currentVisitedClassName);
				objects.put(mangledName, init);
				smartMove(init, var);
				//list.add(new DataTransferInstruction(init, var,
				//		DataTransferInstructionType.Move));
			} else {
				//list.add(new DataTransferInstruction(init, var,
					//	DataTransferInstructionType.Move));
				smartMove(init, var);
			}

			return new Op(localVariable.getName(), OpType.Memory);

		}
		return new Op(localVariable.getName(), OpType.Memory);
		// throw new RuntimeException("local variable Bug?");
	}

	
public Object visit(VariableLocation location) {

		TypeClass tc = (TypeClass) location.enclosingScope().searchVariable(
				location.getName()).getType();
		
		//taking care of fields
		if ((location.enclosingScope().searchVariable(location.getName()).getKind() == SymbolKind.FIELD) && (!location.isExternal()) )  
		{
			
			Op firstOp = new Op("this", OpType.ThisType);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
			// Move this,methodRegister
			//DataTransferInstruction dti = new DataTransferInstruction(firstOp, reg,
				//	DataTransferInstructionType.Move);
			smartMove(firstOp, reg);
			//list.add(dti);
			DispatchTable dt = dTables.get(location.enclosingScope().searchVariableReturnScope(location.getName()).getId()) ;
			int pos = dt.getFieldPos(location.getName());
			Op reg2 = new Op(reg.getName() + "." + pos, OpType.Memory);
			Op reg3 = new Op(Register.getFreeReg(), OpType.Reg);
			addNullReferenceCheckToList(reg2);
			//dti = new DataTransferInstruction(reg2, reg3,
			//		DataTransferInstructionType.MoveField);
			//list.add(dti.setOptComment("### 1"));
			smartMove(reg2, reg3);
			assignmentLocation = reg2;
			return reg3;
			
		}
		System.out.println(tc.getId());// TMP
		if (tc.getId().equals("string")) {
			isString = true;
		}

		if (location.isExternal()) {
			Op ret = (Op) location.getLocation().accept(this);
			location.enclosingScope().getId();
			int pos = dTables.get(location.enclosingScope().getId())
					.getFieldPos(location.getName());
			Op reg = new Op(ret.getName() + "." + pos, OpType.Memory);

			assignmentLocation = reg;

			return reg;
		} else {
			Op var;
			if (localParams.contains(location.getName()))
				var = new Op(location.getName(), OpType.Memory);
			else
				var = new Op(varFormatter(location.getName(),
						this.currentVisitedClassName, location.enclosingScope()
								.getNumid(location.getName())), OpType.Memory);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);

			assignmentLocation = var;

			if (!lval)
			{
				smartMove(var, reg);
		//	list.add(new DataTransferInstruction(var, reg, 
		//			DataTransferInstructionType.Move));
			return reg;
			}
			else return var;
		}

	}

	public Object visit(ArrayLocation location) {

		Op arr = (Op) location.getArray().accept(this);
		Op place = (Op) location.getIndex().accept(this); //Index of array access
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		
		//Runtime check from access an array at legal position
		//Get array size:
		testArrayLength(arr, place);	
		
		Op newarr = new Op(arr.getName() + "[" + place.getName() + "]",
				OpType.Reg);
		list.add(new Comment("TMP 2"));
		smartMove(newarr, reg);
		//TMP list.add(new DataTransferInstruction(newarr, reg, DataTransferInstructionType.MoveArray).setOptComment("### 1"));

		assignmentLocation = newarr;
		assignmentIndex = 1;

		return reg;
	}

	public Object visit(StaticCall call) {

		String funcHeader = "";
		Op reg;

		if (call.getClassName().equals("Library")) {
			funcHeader = "__" + call.getName();
			for (int i = 0; i < call.getArguments().size(); i++) {
				Op op = (Op) call.getArguments().get(i).accept(this);
				if (op.getName().contains(".")) {
					reg = new Op(Register.getFreeReg(), OpType.Reg);
					addNullReferenceCheckToList(op);
				//	list.add(new DataTransferInstruction(op, reg,DataTransferInstructionType.MoveField).setOptComment("### 2"));
					smartMove(op, reg );
					op = reg;
				}
				funcHeader += "(" + op.getName() + ",";
			}
			if (call.getArguments().size() > 0) {
				funcHeader = funcHeader.substring(0, funcHeader.length() - 1);
				funcHeader += ")";
			}

			reg = new Op(Register.getFreeReg(), OpType.Reg);

			list.add(new LibraryInstruction(new Op(funcHeader,
					OpType.FuncHeader), reg));
		} else {

			funcHeader = "_" + call.getClassName() + "_" + call.getName() + "(";

			Method m = (Method) typeTable.getMethodSig(call.getName(),
					call.getClassName()).getNode();
			List<Formal> lst = m.getFormals();

			int i;
			for (i = 0; i < call.getArguments().size(); i++) {
				Op op = (Op) call.getArguments().get(i).accept(this);
				funcHeader += lst.get(i).getName() + "=" + op.getName();
				funcHeader += ",";
			}

			if (i > 0) {
				funcHeader = funcHeader.substring(0, funcHeader.length() - 1);
			}

			funcHeader = funcHeader + ")";
			reg = new Op(Register.getFreeReg(), OpType.Reg);
			list.add(new CallInstruction(new Op(funcHeader, OpType.FuncHeader),
					reg, CallInstructionType.StaticCall));
		}

		return reg;
	}

	public Object visit(VirtualCall call) {
		Op methodRegister, firstOp;
		int methodPos = -1;
		DataTransferInstruction dti;
		String params = "";
		// call.getName() = function name

		methodRegister = new Op(Register.getFreeReg(), OpType.Reg);
		
		int i;
		MethodSigType mst = (MethodSigType) call.enclosingScope().searchMethod(call.getName()).getType();
		
		Method m = (Method) mst.getNode(); 
		
//		Method m = (Method) typeTable.getMethodSig(call.getName(),currentVisitedClassName).getNode();
		List<Formal> lst = m.getFormals();

		params = "(";
		// Prepare params for instruction
		for (i = 0; i < call.getArguments().size(); i++) {
			Op op = (Op) call.getArguments().get(i).accept(this);
			params += lst.get(i).getName() + "=" + op.getName();
			params += ",";
		}

		if (i > 0) {
			params = params.substring(0, params.length() - 1);
		}

		params += ")";

		if (call.isExternal()) 
		{
			methodRegister = (Op) call.getLocation().accept(this); // Adds // "Move	// a,R4" for // example
			firstOp = null;
			DispatchTable dt = dTables.get(call.enclosingScope().getId());
			methodPos = dt.getMethodPos(call.getName());
		} 
		else
		{
			firstOp = new Op("this", OpType.ThisType);
			// Move this,methodRegister
		//	dti = new DataTransferInstruction(firstOp, methodRegister,
			//		DataTransferInstructionType.Move);
			//list.add(dti);
			smartMove(firstOp, methodRegister);
			methodPos = curDt.getMethodPos(call.getName());
		}

		// VirtualCall FreeReg.pos(params), FreeReg
		
		CallInstruction ci = new CallInstruction(new Op(methodRegister
				.getName()
				+ "." + methodPos + params, OpType.Reg), methodRegister,
				CallInstructionType.VirtualCall);

		list.add(ci);

		return methodRegister;
	}

	public Object visit(This thisExpression) {

		Op toRet = new Op(Register.getFreeReg(), OpType.Reg);
		list.add(new DataTransferInstruction(new Op("this", OpType.ThisType),
				toRet, DataTransferInstructionType.Move));
		//smartMove(new Op("this", OpType.ThisType),toRet);
		return toRet;
	}

	public Object visit(NewClass newClass) {
		String className = newClass.getName();
		ICClass newClassClass = null;
		int objectSize = 1; // = 1 for obj DV
		String objReg;
		String objectDVName;

		newClassClass = getIcClassFromName(className);

		assert (newClassClass != null); // Possible bug

		// Take amount of fields into account
		objectSize += getClassFieldsNumber(newClassClass);

		// Multiply by 4 to get size in bytes:
		objectSize *= 4;

		// Create types to add to list:
		objReg = Register.getFreeReg();
		Op reg = new Op(objReg, OpType.Reg);

		objectDVName = dTables.get(newClassClass.getName()).getName();

		LibraryInstruction li = new LibraryInstruction(new Op(
				"__allocateObject(" + objectSize + ")", OpType.FuncHeader), reg);
		li.setOptComment("Allocation of " + newClassClass.getName());

		Op offset = new Op("0", OpType.Immediate); // DV is always in offset 0
		Op dvOp = new Op(objectDVName, OpType.DV);

		DataTransferInstruction dti = new DataTransferInstruction(dvOp, reg,
				offset, DataTransferInstructionType.MoveField);
		dti.setOptComment("Move field for DV pointer");
//		smartMove(dvOp, reg);
		list.add(li);
		list.add(dti);

		this.isAssignmentNewClass = true;

		return reg;
	}

	public Object visit(NewArray newArray) {

		Op size = (Op) newArray.getSize().accept(this);

		Op num = new Op("4", OpType.Immediate); // size of each date type is
												// always 4 - if its an object -
												// its just a pointer
		Op four = new Op(Register.getFreeReg(), OpType.Reg);
		//list.add(new DataTransferInstruction(num, four,
			//	DataTransferInstructionType.Move));
		smartMove(num, four);

		Instruction i = new ArithmeticInstruction(four, size,
				ArithmeticInstructionType.Mul);
		list.add(i);
		
		//Runtime check for array size
		//Compare array size with zero
		list.add(new LogicalInstruction(new Op("0", OpType.Immediate), size, LogicalInstructionType.Compare));
		
		//Jump to label if size is smaller then 1
		list.add(new ControlTransferInstruction(new Op(errorLabelArrayNegativeAllocationSize, OpType.Label), 
												ControlTransferInstructionType.JumpLE));

		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		i = new LibraryInstruction(new Op("__allocateArray(" + size + ")",
				OpType.FuncHeader), reg);
		list.add(i);
		return reg;
	}

	public Object visit(Length length) {

		Op one = (Op) length.getArray().accept(this);
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		list.add(new Comment("TMP 3"));
		smartMove(one, reg);
		Instruction i = new DataTransferInstruction(reg, reg, DataTransferInstructionType.ArrayLength); 
		list.add(i);

		return (reg);
	}

	public Object visit(MathBinaryOp binaryOp) {

		ArithmeticInstructionType AIT = null;
		if (binaryOp.getOperator() == IC.BinaryOps.PLUS)
			AIT = ArithmeticInstructionType.Add;
		if (binaryOp.getOperator() == IC.BinaryOps.MINUS)
			AIT = ArithmeticInstructionType.Sub;
		if (binaryOp.getOperator() == IC.BinaryOps.MULTIPLY)
			AIT = ArithmeticInstructionType.Mul;
		if (binaryOp.getOperator() == IC.BinaryOps.DIVIDE)
			AIT = ArithmeticInstructionType.Div;
		if (binaryOp.getOperator() == IC.BinaryOps.MOD)
			AIT = ArithmeticInstructionType.Mod;

		Op one = (Op) binaryOp.getFirstOperand().accept(this);
		Op two = (Op) binaryOp.getSecondOperand().accept(this);
		

		Op oneReg = new Op(Register.getFreeReg(), OpType.Reg);
		Op twoReg = new Op(Register.getFreeReg(), OpType.Reg);

		smartMove(one , oneReg);
		smartMove(two , twoReg);
		
		one = oneReg;
		two = twoReg;
		Op toRet = one;
		if (two.getOpType() != OpType.Reg)
			System.out.println("THROW NEW ERROR"); // TBD Throw error
		Instruction i;
		if (isString) {
			i = new LibraryInstruction(new Op("__stringCat(" + one.getName()
					+ "," + two.getName() + ")", OpType.FuncHeader), toRet);
		}
		else 
		{
			if(AIT.equals(ArithmeticInstructionType.Div))
			{
				//Runtime check - devision by zero				
				//Compare array size with zero
				list.add(new LogicalInstruction(new Op("0", OpType.Memory), two, LogicalInstructionType.Compare));
				
				//Jump to label if size is smaller then 1
				list.add(new ControlTransferInstruction(new Op(errorLabelDevByZero, OpType.Label), 
														ControlTransferInstructionType.JumpTrue));
							
			}
						//New instruction
			i = new ArithmeticInstruction(two, one, AIT);
			toRet = one;
		}

		list.add(i);
		return toRet;
	}

	public Object visit(LogicalBinaryOp binaryOp) {

		LogicalInstructionType LIT;
		DataTransferInstructionType DIT;
		Op one = (Op) binaryOp.getFirstOperand().accept(this);
		Op two = (Op) binaryOp.getSecondOperand().accept(this);

		Op oneReg = new Op(Register.getFreeReg(), OpType.Reg);
		Op twoReg = new Op(Register.getFreeReg(), OpType.Reg);

		smartMove(one , oneReg);
		smartMove(two , twoReg);
		
		one = oneReg;
		two = twoReg;
		
		
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		Op zero = new Op("0", OpType.Immediate);
//		DIT = DataTransferInstructionType.Move;
		Instruction i;// = new DataTransferInstruction(zero, reg, DIT);
//		list.add(i);
		smartMove(zero, reg);
		Op jumpto = new Op(Jump.getNextJumpCounter(), OpType.Label);

		if (binaryOp.getOperator() == IC.BinaryOps.LAND) {
			LIT = LogicalInstructionType.And;
			i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			return (two);
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LOR) {
			LIT = LogicalInstructionType.Or;
			i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			return (two);
		}

		if (binaryOp.getOperator() == IC.BinaryOps.EQUAL) {
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,
					ControlTransferInstructionType.JumpFalse);
			list.add(i);
		}
		if (binaryOp.getOperator() == IC.BinaryOps.NEQUAL) {
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,
					ControlTransferInstructionType.JumpTrue);
			list.add(i);
		}
		if (binaryOp.getOperator() == IC.BinaryOps.GT) {
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(two, one, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,
					ControlTransferInstructionType.JumpLE);
			list.add(i);
		}
		if (binaryOp.getOperator() == IC.BinaryOps.GTE) {
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(two, one, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,
					ControlTransferInstructionType.JumpL);
			list.add(i);
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LTE) {
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(two, one, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,
					ControlTransferInstructionType.JumpG);
			list.add(i);
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LT) {
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(two, one, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,
					ControlTransferInstructionType.JumpGE);
			list.add(i);

		}

		Op one2 = new Op("1", OpType.Immediate);
		DIT = DataTransferInstructionType.Move;
		i = new DataTransferInstruction(one2, reg, DIT);
		//list.add(i);
		smartMove(one2, reg);
		list.add(new Label(jumpto.getName()));
		return reg;
	}

	public Object visit(MathUnaryOp unaryOp) {

		Op one = (Op) unaryOp.getOperand().accept(this);
		ArithmeticInstructionType AIT;
		AIT = ArithmeticInstructionType.Neg;
		Instruction i = new ArithmeticInstruction(one, AIT); // TBD - how do
																// we handle
																// arithmetic
																// instructions
																// that have
																// only one
																// parameter-
																// currently im
																// passing the
																// same op twice
		list.add(i);

		return (one);
	}

	public Object visit(LogicalUnaryOp unaryOp) {
		Op one = (Op) unaryOp.getOperand().accept(this);
		LogicalInstructionType AIT;
		AIT = LogicalInstructionType.Xor;
		Instruction i = new LogicalInstruction(new Op("1", OpType.Immediate), one, AIT); 
		list.add(i);

		return (one);
	}

	public Object visit(Literal literal) {
		if (literal.getType() == IC.LiteralTypes.INTEGER) {
			Op num = new Op((String) literal.getValue(), OpType.Immediate);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
			list.add(new DataTransferInstruction(num, reg,
					DataTransferInstructionType.Move));
			return reg;
		}

		if (literal.getType() == IC.LiteralTypes.TRUE) {
			Op num = new Op("1", OpType.Immediate);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
			list.add(new DataTransferInstruction(num, reg,
					DataTransferInstructionType.Move));
			return reg;
		}
		if (literal.getType() == IC.LiteralTypes.FALSE)
		{
			Op num = new Op("0", OpType.Immediate);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
			list.add(new DataTransferInstruction(num, reg,
					DataTransferInstructionType.Move));
			return reg;
		}

		if (literal.getType() == IC.LiteralTypes.STRING) {
			isString = true;
			String strlit = (String) literal.getValue();
			String strOutput;
			if (!StringLiteral.isIn(strlit)) {
				StringLiteral.addtoliterals(strlit);
			}
			strOutput = StringLiteral.getIndexOfLit(strlit);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
			Op strLitOp = new Op(strOutput, OpType.Memory);
			list.add(new DataTransferInstruction(strLitOp, reg,
					DataTransferInstructionType.Move)
					.setOptComment("assigning literal to reg"));
			return (reg);
		}
		
		if(literal.getType() == IC.LiteralTypes.NULL)
		{
			Op reg = new Op(Register.getFreeReg(), OpType.Immediate);
			list.add(new DataTransferInstruction(new Op("0", OpType.Immediate), reg, DataTransferInstructionType.Move));
			return reg;
		}

		throw new RuntimeException("Lit Exception");
	}

	public Object visit(ExpressionBlock expressionBlock) {

		return expressionBlock.getExpression().accept(this);

	}

	/**
	 * Gets the amount of fields from a class (including base)
	 * @param icClass Class name
	 * @return
	 */
	private int getClassFieldsNumber(ICClass icClass) {
		int fieldsNums = 0;
		ICClass superClass = null;
		// String superClassName = "";

		if (icClass == null) {
			return 0;
		} else {
			for (Field field : icClass.getFields()) {
				field.hashCode(); // Remove compiler warning
				fieldsNums++;
			}

			if (icClass.hasSuperClass()) {
				superClass = getIcClassFromName(icClass.getSuperClassName());
			}

			return (fieldsNums + getClassFieldsNumber(superClass));
		}
	}

	/**
	 * Gets a class object from name
	 * @param className
	 * @return
	 */
	private ICClass getIcClassFromName(String className) {
		ICClass toRet = null;

		for (ICClass icClass : this.prog.getClasses()) {
			if (icClass.getName().equals(className)) {
				toRet = icClass;
				break;
			}
		}
		return toRet;
	}
	
	/**
	 * Adds common error labels to lit code
	 */
	private void addErrorLabelsToList()
	{
		list.add(new StringInstruction(""));
		list.add(new StringInstruction("######################"));
		list.add(new StringInstruction("# Error Labels"));
		list.add(new StringInstruction("######################"));
		list.add(new StringInstruction(""));
		
		//Add null reference
		list.add(new Label(errorLabelNullReference));
		list.add(new LibraryInstruction(new Op("__println(" + errorLabelNullReference.substring(1) +")", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new LibraryInstruction(new Op("__exit(1)", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new StringInstruction(""));
		
		//Add illegal array position
		list.add(new Label(errorLabelIllegalArrayLocation));
		list.add(new LibraryInstruction(new Op("__println(" + errorLabelIllegalArrayLocation.substring(1) +")", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new LibraryInstruction(new Op("__exit(1)", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new StringInstruction(""));
		
		//Add division by zero
		list.add(new Label(errorLabelDevByZero));
		list.add(new LibraryInstruction(new Op("__println(" + errorLabelDevByZero.substring(1) +")", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new LibraryInstruction(new Op("__exit(1)", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new StringInstruction(""));
		
		//Add negative array allocation size
		list.add(new Label(errorLabelArrayNegativeAllocationSize));
		list.add(new LibraryInstruction(new Op("__println(" + errorLabelArrayNegativeAllocationSize.substring(1) +")", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new LibraryInstruction(new Op("__exit(1)", OpType.Label), new Op("Rdummy", OpType.Reg)));
		list.add(new StringInstruction(""));
	}
	
	/**
	 * Adds the null reference check the function call point 
	 * @param reg
	 */
	private void addNullReferenceCheckToList(Op reg)
	{
		addNullReferenceCheckToList(reg, list);
	}
	
	/**
	 * Adds the null reference check to list for a specific register
	 * @param reg
	 * @param list
	 */
	private void addNullReferenceCheckToList(Op reg, ArrayList<Instruction> list)
	{
		if(reg.getName().contains("."))
		{
			String regName = reg.getName().substring(0, reg.getName().indexOf("."));
			list.add(new LogicalInstruction(new Op("0", OpType.Immediate), new Op(regName, OpType.Reg), LogicalInstructionType.Compare));
		}
		else
		{
			list.add(new LogicalInstruction(new Op("0", OpType.Immediate), reg, LogicalInstructionType.Compare));			
		}
		
		list.add(new ControlTransferInstruction(new Op(errorLabelNullReference, OpType.Label), 
				ControlTransferInstructionType.JumpTrue));
	}
	
	/**
	 * Adds correctness test for array length call
	 * @param arrayObject
	 * @param place
	 */
	private void testArrayLength(Op arrayObject, Op place)
	{
		testArrayLength(arrayObject, place, list);
	}
	
	/**
	 * Tests array length for specific registers
	 * @param arrayObject
	 * @param place
	 * @param tlist
	 */
	private void testArrayLength(Op arrayObject, Op place, ArrayList<Instruction> tlist)
	{
		Op arrSize = new Op(Register.getFreeReg(), OpType.Reg);
		Op zeroReg = new Op(Register.getFreeReg(), OpType.Reg);
		//list.add(new DataTransferInstruction(new Op("0", OpType.Immediate), zeroReg, DataTransferInstructionType.Move));
		smartMove(new Op("0", OpType.Immediate), zeroReg);
		
		Op arr = new Op(Register.getFreeReg(), OpType.Reg);
		tlist.add(new Comment("TMP 4"));
		smartMove(arrayObject, arr);
		
		tlist.add(new LogicalInstruction(arr, arrSize, 
				LogicalInstructionType.ArrayLength).setOptComment("Getting array length"));

		tlist.add(new LogicalInstruction(place, arrSize, LogicalInstructionType.Compare));
		tlist.add(new ControlTransferInstruction(new Op(errorLabelIllegalArrayLocation, OpType.Label), 
				ControlTransferInstructionType.JumpL));		
		tlist.add(new LogicalInstruction(place, zeroReg, LogicalInstructionType.Compare));
		tlist.add(new ControlTransferInstruction(new Op(errorLabelIllegalArrayLocation, OpType.Label), 
												ControlTransferInstructionType.JumpG));	
	}
	
	/**
	 * Smart move for registers to support depth of nesting
	 * @param src
	 * @param dst
	 */
	private void smartMove(Op src, Op dst)
	{
		list.add(new Comment("new smartMove on: " + src.getName() + ", " + dst.getName()));
		boolean var = !dst.getName().startsWith("R");
		boolean lit = (src.getOpType() == OpType.Immediate);
		ArrayList<Instruction> templist = new ArrayList<Instruction>();
		ArrayList<Instruction> reverselist = new ArrayList<Instruction>();
		
		Op srcOp = peel(src,templist,false);
		
		Op dstOp;
		if (!var)
			dstOp = peel(dst,templist,false);
		else
		{
			Op peeledDst = peel(dst,templist, true);

			dstOp = peeledDst;
		}
		
		safeMove(srcOp, dstOp,templist);
		
		list.addAll(templist);
		
		
		int stopInt = (lit ? 0 : -1);
		for(int i = templist.size()-2; i>stopInt;i--)
		{
			if (templist.get(i) instanceof DataTransferInstruction  )
			{
					
				list.add(new DataTransferInstruction(templist.get(i).getOp2(), templist.get(i).getOp1(), (DataTransferInstructionType) templist.get(i).getInstructionType()).setOptComment("reversed"));
			}
		}
	}
	
	/**
	 * Peels of "." and arrays from registers
	 * @param op
	 * @param list
	 * @param dst
	 * @return
	 */
	private Op peel(Op op, ArrayList<Instruction> list, boolean dst)
	{
		if (op.getName().lastIndexOf(".") > op.getName().lastIndexOf("["))
			return peelDot(op,list,dst);
		if (op.getName().lastIndexOf(".") < op.getName().lastIndexOf("["))
			return peelBrack(op,list,dst);
		if ((!op.getName().startsWith("R")) && (!dst))
		{
			Op toRet = new Op( Register.getFreeReg()  ,OpType.Reg );
			safeMove(op, toRet,list);
			return toRet;
		}
		
		return op;
	}
	
	/**
	 * Peels dot from registers
	 * @param op
	 * @param tlist
	 * @param dst
	 * @return
	 */
	private Op peelDot(Op op, ArrayList<Instruction> tlist, boolean dst)
	{	
		Op onePeel = new Op( op.getName().substring(0, op.getName().lastIndexOf("."))  ,OpType.Reg );
		Op toRet = new Op( Register.getFreeReg()  ,OpType.Reg );
		Op peeled = peel(onePeel,tlist,false);
		addNullReferenceCheckToList(peeled, tlist);
		tlist.add(new DataTransferInstruction(new Op( peeled.getName()+op.getName().substring(op.getName().lastIndexOf(".")), OpType.Reg ), 
					toRet,DataTransferInstructionType.MoveField)
					.setOptComment("SMART MOVED BITCH - field style"));
		return toRet;
	}
	
	/**
	 * Peels of brackets from registers
	 * @param op
	 * @param tlist
	 * @param dst
	 * @return
	 */
	private Op peelBrack(Op op, ArrayList<Instruction> tlist, boolean dst)
	{	
		Op onePeel = new Op( op.getName().substring(0, op.getName().lastIndexOf("["))  ,OpType.Reg );
		Op toRet = new Op( Register.getFreeReg()  ,OpType.Reg );
		Op peeled = peel(onePeel,tlist,false);
		tlist.add(new DataTransferInstruction(new Op( peeled.getName()+op.getName().substring( op.getName().lastIndexOf("[")) ,OpType.Reg ) ,toRet,DataTransferInstructionType.MoveArray).setOptComment("SMART MOVED BITCH - Array Style"));
		return toRet;
	}
	
	/**
	 * Safe move used by smart move and peel
	 * @param source
	 * @param dest
	 * @param tlist
	 */
	private void safeMove(Op source,Op dest, ArrayList<Instruction> tlist)
	{
		DataTransferInstructionType dt = DataTransferInstructionType.Move;
		if (source.getName().contains(".") || (dest.getName().contains(".")))
		{
			dt = DataTransferInstructionType.MoveField;
			if (source.getName().contains("."))
				addNullReferenceCheckToList(source, tlist);
			else
				addNullReferenceCheckToList(dest, tlist);
		}
		if (source.getName().contains("[") || (dest.getName().contains("]")))
		{
			dt = DataTransferInstructionType.MoveArray;
			if (source.getName().contains("["))
			{
				Op arr = new Op(source.getName().substring(0, source.getName().lastIndexOf("[")), OpType.Other);
				Op index = new Op(source.getName().substring(source.getName().lastIndexOf("]")+1), OpType.Other);
				testArrayLength(arr, index);
			}
			else
			{
				Op dst = new Op(dest.getName().substring(0, dest.getName().lastIndexOf("[")), OpType.Other);
				Op index = new Op(dest.getName().substring(dest.getName().lastIndexOf("]")+1), OpType.Other);
				testArrayLength(dst, index);
			}
		}
		
		tlist.add(new DataTransferInstruction(source,dest,dt).setOptComment("brought to you by safe/smartmove"));
	}
	
	/**
	 * Helper method for debugging needs. Currently not in use
	 * @param opName
	 * @return
	 */
	private int howSafe(String opName)
	{
		int dot = opName.lastIndexOf(".");
		int brk = opName.lastIndexOf("[");
		
		if ((dot >= 0) || (brk >= 0))
		{
			if (dot > brk)
			{
				
				return howSafe(opName.substring(0, dot)) + 1;
			}
			else
			{
				return howSafe(opName.substring(0, brk)) + 1;
			}
		}
		if (opName.indexOf('R') == 0)
			return 0;
		
		return 1;
	}
}