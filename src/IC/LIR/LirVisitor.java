package IC.LIR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import IC.LIR.LogicalInstruction.LogicalInstructionType;
import IC.LIR.Op.OpType;
import IC.Semantics.TypeClass;
import IC.Semantics.TypeTable;

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
	
	private boolean isAssignmentNewClass = false;
	private boolean isString = false;
	
	private HashMap<String, Op> objects = new HashMap<String, Op>();
	private Program prog;
	private TypeTable typeTable;
	private String currentVisitedClassName;
	private ArrayList<String> localParams = new ArrayList<String>();

	/**
	 * Constructs a new LIR visitor.
	 * 
	 */
	public LirVisitor(String progName,TypeTable typetable) {
		this.progName = progName;
		typeTable =typetable;
	}

	public Object visit(Program program) {
		
		this.prog = program;
		
		for (ICClass icClass : program.getClasses())
		{
			curDt = new DispatchTable(icClass.getName());
			dTables.put(icClass.getName(), curDt);

			if (icClass.hasSuperClass()) {
				curDt.setParent(dTables.get(icClass.getSuperClassName()));
			}
			
			for (Field field : icClass.getFields())
				curDt.addField(field);
			for (Method method : icClass.getMethods()){
				if (!method.getName().equals("main")){
					curDt.addMethod(method);
				}
			}
		}
		
		for (ICClass icClass : program.getClasses())
		{
			this.currentVisitedClassName = icClass.getName(); 
			icClass.accept(this);
				
		}		
		list.add(0, new StringInstruction());
		list.add(0, new StringInstruction("######################"));
		Iterator<DispatchTable> it = dTables.values().iterator();
		while (it.hasNext()){
			list.add(0, it.next().getInstruction());
		}	
		list.add(0, new StringInstruction("# DISPATCH VECTORS"));
		list.add(0, new StringInstruction("######################"));
		
		list.add(0, new StringInstruction());
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction(StringLiteral.prettyPrint()));
		list.add(0, new StringInstruction("# STRING LITERALS"));
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction());
			
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction("# Program " + progName));
		list.add(0, new StringInstruction("######################"));
		list.add(0, new StringInstruction());
		
		return list;
	}

	public Object visit(ICClass icClass) {

		curDt = dTables.get(icClass.getName());
		
		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods())
		{
			method.accept(this);
			list.add(new ControlTransferInstruction(new Op("9999", OpType.Immediate ),ControlTransferInstructionType.Return ));	
		}

		return null;
	}

	public Object visit(PrimitiveType type) {
		
		if (type.getDimension() > 0)
			//TBD: do something here?
			;

		return null;
	}

	public Object visit(UserType type) {

		if (type.getDimension() > 0)
			//TBD: do something here?
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

		
		list.add(new Label(method.enclosingScope().getParent().getId()+"_"+method.getName(), 
				"Method " + method.getName() + ":"));
		
		method.getType().accept(this);
		localParams.clear();
		for (Formal formal : method.getFormals())
		{
			formal.accept(this);
			localParams.add(formal.getName());
		}
		for (Statement statement : method.getStatements()) {
			isString = false;
			list.add(new Comment("Line " + statement.getLine() + ": "));
			System.out.println("LINE - " + statement.getLine());//TMP
			statement.accept(this);
		}
		localParams.clear();

		return null;
	}

	public Object visit(StaticMethod method) {

		if (method.getName().equals("main")){
			list.add(new Label("ic_" + method.getName(), 
					"Main Method"));
		}
		else {
			list.add(new Label(method.getName(), 
					"Method " + method.getName() + ":"));
		}

		method.getType().accept(this);

		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements()) {
			isString = false;
			list.add(new Comment("Line " + statement.getLine() + ": "));
			System.out.println("LINE - " + statement.getLine());//TMP
			statement.accept(this);
		}
		
		if (method.getName().equals("main")){
			list.add(new LibraryInstruction(new Op("__exit(0)", OpType.FuncHeader), dummyOp));
		}
		
		list.add(new Comment("Method " + method.getName() + " end"));
		
		return null;
	}

	public Object visit(Assignment assignment) {

		try
		{
			Op loc =   (Op)assignment.getVariable().accept(this);
			Op value = (Op)assignment.getAssignment().accept(this);
			
			if (this.isAssignmentNewClass) 
			{
				this.isAssignmentNewClass = false;
				String mangledName = Utils.getObjectsMapName(loc.getName(),
															 assignment.enclosingScope().getId(),
															 this.currentVisitedClassName);
				objects.put(mangledName , value);				
			}
			
			// Copy value into location:
			if (loc.getName().contains(".")) 
			{
			Instruction ins = new DataTransferInstruction(value, loc,
									DataTransferInstructionType.MoveField);
			ins.setOptComment("(Assignment statement)");
			list.add(ins);
			}
			else
			{
				Instruction ins = new DataTransferInstruction(value, loc,
										DataTransferInstructionType.Move);
				ins.setOptComment("(Assignment statement)");
				list.add(ins);
				}
					
		}catch (Exception e){
			System.out.println("casting error - need to implement something"); //TMP!
		}

		return null;
	}

	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);		
		return null;
	}

	public Object visit(Return returnStatement) {
	//	if (returnStatement.getValue() == null)
		//	list.add(new)
		
		Op reg = (Op)returnStatement.getValue().accept(this);
		Op newregister = new Op(Register.getFreeReg(), OpType.Reg);
		list.add(new DataTransferInstruction(reg, newregister, DataTransferInstruction.DataTransferInstructionType.Move) );
		list.add(new ControlTransferInstruction(newregister,ControlTransferInstructionType.Return));
		return null;
	}

	public Object visit(If ifStatement) {
		//Set labels:
		String thenLabel = Jump.getnextjumpcounter();
		String elseLabel = Jump.getnextjumpcounter();
		String endLabel = Jump.getnextjumpcounter();
		
		//Check If's condition
		Op reg = (Op) ifStatement.getCondition().accept(this);
		
		//Compare outcome from If's condition
		//If reg == 1 then If's outcome is true, else false
		LogicalInstruction li = new LogicalInstruction(new Op("0", OpType.Immediate), reg, 
				   LogicalInstructionType.Compare);
		li.setOptComment("If's condition check");
		list.add(li);
		
		//If condition is true, jump to then label
		ControlTransferInstruction ctThen = new ControlTransferInstruction(new Op(thenLabel, OpType.Label),  
				 									 ControlTransferInstructionType.JumpLE);
		ctThen.setOptComment("Conditional jump when If's outcome is true");
		list.add(ctThen);
		
		//If we are here, means that If's condition is false. Goto else label if exists.
		if (ifStatement.hasElse())
		{			
			ControlTransferInstruction ctElse = new ControlTransferInstruction(new Op(elseLabel, OpType.Label),  
					 				 ControlTransferInstructionType.Jump);
			ctElse.setOptComment("Jump to else label");
			list.add(ctElse);
		}
		
		//If's then label
		list.add(new Label(thenLabel,"If's then label"));
		
		//If's then part
		ifStatement.getOperation().accept(this);
		
		//Then must jump to then label in order not to execute else's part
		ControlTransferInstruction ctEnd = new ControlTransferInstruction(new Op(endLabel, OpType.Label), 
													ControlTransferInstructionType.Jump);
		ctEnd.setOptComment("Jump to end label(end of then part)");
		list.add(ctEnd);
		
		if(ifStatement.hasElse())
		{
			//Else label
			list.add(new Label(elseLabel,"If's else label"));
			
			//Else code here
			ifStatement.getElseOperation().accept(this);
		}
		
		//End label
		list.add(new Label(endLabel,"End of If statement"));
		
		return null;
	}

	public Object visit(While whileStatement) {		
		//Read latest while labels:
		String previousWhileStartLabel = this.latestWhileStartLabel;
		String previousWhileEndLabel = this.latestWhileEndLabel;
		
		//Add while label to instructions list and generate new labels
		String whileLabel = Jump.getnextjumpcounter();
		String falseWhileLabel = Jump.getnextjumpcounter();
		list.add(new Label(whileLabel,"While"));
		
		//Update LirVisitor for the new labels:
		this.latestWhileStartLabel = whileLabel;
		this.latestWhileEndLabel = falseWhileLabel;
		
		//Get condition evaluation outcome
		Op reg = (Op) whileStatement.getCondition().accept(this);
		
		//Check if outcome is true(1) or false(0)
		LogicalInstruction li = new LogicalInstruction(new Op("0", OpType.Immediate), reg, 
													   LogicalInstructionType.Compare);
		li.setOptComment("While condition check");
		list.add(li);
		
		//Add to list the JumpLe instruction 
		ControlTransferInstruction ct = new ControlTransferInstruction(new Op(falseWhileLabel, OpType.Label),  
												 ControlTransferInstructionType.JumpLE);
		ct.setOptComment("Conditional jump when while statement is false");
		list.add(ct);
		
		//If while condition is true, this code will be executed
		whileStatement.getOperation().accept(this);
		
		//Add new while false condition label
		list.add(new Label(falseWhileLabel, "False while condition"));
		
		//Restore older labels:
		this.latestWhileStartLabel = previousWhileStartLabel;
		this.latestWhileEndLabel = previousWhileEndLabel;
		
		//TBD: Fix the double __ print
				
		return null;
	}

	public Object visit(Break breakStatement) {
		//Upon break call, jump to innest while end label:
		ControlTransferInstruction ctBreak = new ControlTransferInstruction(new Op(this.latestWhileEndLabel, OpType.Label),  
				 ControlTransferInstructionType.Jump);
		ctBreak.setOptComment("Break statement. Jump to end of while statement label: " + this.latestWhileEndLabel);
		list.add(ctBreak);
		
		return null;
	}

	public Object visit(Continue continueStatement) {
		//Upon continue statement, jump to closest while start label:
		ControlTransferInstruction ctContinue = new ControlTransferInstruction(new Op(this.latestWhileStartLabel, OpType.Label),  
				 ControlTransferInstructionType.Jump);
		ctContinue.setOptComment("Continue statement. Jump to the start of while statement label: " + this.latestWhileStartLabel);
		list.add(ctContinue);
		
		return null;
	}
	
	public Object visit(StatementsBlock statementsBlock) {
		StringBuffer output = new StringBuffer();

		list.add(new Comment("Line " + statementsBlock.getLine() + ": "));

		output.append("Block of statements");

		for (Statement statement : statementsBlock.getStatements())
			output.append(statement.accept(this));

		return null;
	}

	public Object visit(LocalVariable localVariable) {
		
		if (localVariable.hasInitValue()) 
		{
			Op init = (Op)localVariable.getInitValue().accept(this);
			Op var;
			if (localParams.contains(localVariable.getName()))
				var = new Op(localVariable.getName() ,OpType.Var);
			else
				var = new Op(localVariable.getName()+"_"+ this.currentVisitedClassName+"_"+localVariable.enclosingScope().getNumid() ,OpType.Var);
			
			if (this.isAssignmentNewClass) 
			{
				this.isAssignmentNewClass = false;
				String mangledName = Utils.getObjectsMapName(localVariable.getName(), 
															 localVariable.enclosingScope().getId(),
															 this.currentVisitedClassName);
				objects.put(mangledName , init);		
				list.add(new DataTransferInstruction(init, var, DataTransferInstructionType.Move));
			}
			else
			{
				list.add(new DataTransferInstruction(init, var, DataTransferInstructionType.Move));
			}
			
			return new Op(localVariable.getName(),OpType.Memory);
		}

		throw new RuntimeException("local variable Bug?");
	}

	public Object visit(VariableLocation location) {

		TypeClass tc = (TypeClass) location.enclosingScope().getVariable(location.getName()).getType();
		System.out.println(tc.getId());//TMP
		if (tc.getId().equals("string"))
		{
			isString = true;
		}
		
		if (location.isExternal()) {
			Op ret = (Op)location.getLocation().accept(this);
			location.enclosingScope().getId();
			int pos = dTables.get(location.enclosingScope().getId()).getFieldPos(location.getName());
			Op reg = new Op(ret.getName() +"."+pos ,OpType.Var);

			return reg;
		}
		else
		{
			Op var;
			if (localParams.contains(location.getName()))
				var = new Op(location.getName() ,OpType.Var);
			else
				var = new Op(location.getName()+"_"+ this.currentVisitedClassName+"_"+location.enclosingScope().getNumid() ,OpType.Var);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		
			list.add(new DataTransferInstruction(var, reg, DataTransferInstructionType.Move));
			return reg;
		}

		
	}

	public Object visit(ArrayLocation location) {
	
		Op arr = (Op)location.getArray().accept(this);
		Op place = (Op)location.getIndex().accept(this);
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		
		Instruction i = new DataTransferInstruction(arr,place,reg,DataTransferInstructionType.MoveArray);
		list.add(i);
		
		return reg;
		
	}
	
	public Object visit(StaticCall call) {
		
		String funcHeader = "";
		Op reg;
		
		if (call.getClassName().equals("Library"))
		{
			funcHeader = "__" + call.getName();
			for (int i = 0 ; i < call.getArguments().size();i++)
			{
				Op op = (Op)call.getArguments().get(i).accept(this);
				if (op.getName().contains("."))
				{
					reg = new Op(Register.getFreeReg(), OpType.Reg);
					list.add(new DataTransferInstruction(op,reg,DataTransferInstructionType.MoveField));
					op = reg;
				}
				funcHeader += "("+op.getName() + "," ;	
			}
			if (call.getArguments().size() > 0)
			{
				funcHeader = funcHeader.substring(0, funcHeader.length() - 1);
				funcHeader+= ")";
			}
				
			
			reg = new Op(Register.getFreeReg(), OpType.Reg);
			
			list.add(new LibraryInstruction(new Op(funcHeader, OpType.FuncHeader), reg));
		}
		else
		{
		
			funcHeader = "_" +call.getClassName()+"_"+call.getName()+"(";
				
			Method m = (Method) typeTable.getMethodSig(call.getName(), call.getClassName()).getNode();
			List<Formal> lst = m.getFormals(); 
			
			for (int i = 0 ; i < call.getArguments().size();i++)
			{
				Op op = (Op)call.getArguments().get(i).accept(this);
				funcHeader += lst.get(i).getName() + "=" + op.getName() ;
			}
			
			funcHeader= funcHeader + ")";
			reg = new Op(Register.getFreeReg(),OpType.Reg);
			list.add(new CallInstruction(new Op(funcHeader,OpType.FuncHeader), reg , CallInstructionType.StaticCall));
		}
		
		return reg;
	}

	public Object visit(VirtualCall call) {
		Op methodRegister, firstOp;
		int methodPos;
		DataTransferInstruction dti;
		String params = "";
		//call.getName() = function name
		
		methodRegister = new Op(Register.getFreeReg(), OpType.Reg);
		
		if (call.isExternal())
		{
			methodRegister = (Op) call.getLocation().accept(this); //Adds "Move a,R4" for example
			firstOp = null;			
		}
		else
		{
			firstOp = new Op("this", OpType.ThisType);
			
			//Move this,methodRegister
			dti = new DataTransferInstruction(firstOp,
											  methodRegister,
	    									  DataTransferInstructionType.Move);
			list.add(dti);
		}
		
		int i;
		
		Method m = (Method) typeTable.getMethodSig(call.getName(), currentVisitedClassName).getNode();
		List<Formal> lst = m.getFormals(); 
		
		params = "(";
		
		//Prepare params for instruction
		for (i = 0 ; i < call.getArguments().size();i++)
		{
			Op op = (Op)call.getArguments().get(i).accept(this);
			params += lst.get(i).getName() + "=" + op.getName();
			params+=",";
		}
		
		if(i > 0)  
		{
			params = params.substring(0, params.length() -1 );
		}
		
		params+= ")";
		
		//VirtualCall FreeReg.pos(params), FreeReg
		methodPos = curDt.getMethodPos(call.getName());
		CallInstruction ci = new CallInstruction(new Op(methodRegister.getName()+"."+methodPos+params , OpType.Reg),
												 methodRegister,
												 CallInstructionType.VirtualCall);
	
		list.add(ci);
				
		return methodRegister;
	}
	public Object visit(This thisExpression) {
	
		Op toRet = new Op(Register.getFreeReg(), OpType.Reg);
		list.add(new DataTransferInstruction(new Op("this",OpType.ThisType) ,toRet,DataTransferInstructionType.Move));
		return toRet;
	}

	public Object visit(NewClass newClass) {
		String className = newClass.getName();
		ICClass newClassClass = null; 
		int objectSize = 1; // = 1 for obj DV
		String objReg;
		String objectDVName;
		
		newClassClass = getIcClassFromName(className);
		
		assert(newClassClass != null); //Possible bug
		
		//Take amount of fields into account
		objectSize+= getClassFieldsNumber(newClassClass);
		
		//Multiply by 4 to get size in bytes:
		objectSize*=4;
	
		//Create types to add to list:
		objReg = Register.getFreeReg();
		Op reg = new Op(objReg, OpType.Reg);
		
		objectDVName = dTables.get(newClassClass.getName()).getName();
		
		LibraryInstruction li = new LibraryInstruction(new Op("__allocateObject(" + objectSize + ")", 
				OpType.FuncHeader), reg);
		li.setOptComment("Allocation of " + newClassClass.getName());
		
		Op offset = new Op("0", OpType.Immediate); //DV is always in offset 0
		Op dvOp = new Op(objectDVName, OpType.DV);
		
		DataTransferInstruction dti = new DataTransferInstruction(dvOp, reg, offset,  
																DataTransferInstructionType.MoveField);
		dti.setOptComment("Move field for DV pointer");
		
		list.add(li);
		list.add(dti);
		
		this.isAssignmentNewClass = true;
		
		return reg; 
	}

	public Object visit(NewArray newArray) {
		
		Op size = (Op) newArray.getSize().accept(this);
		
		
		Op num = new Op("4", OpType.Immediate); // size of each date type is always 4 - if its an object - its just a pointer
		Op four = new Op(Register.getFreeReg(), OpType.Reg);
		list.add(new DataTransferInstruction(num, four,
				DataTransferInstructionType.Move));
		
		
		Instruction i = new ArithmeticInstruction(four,size,ArithmeticInstructionType.Mul);
		list.add(i);
		
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		i = new LibraryInstruction(new Op("allocateArray(" + size +")", OpType.FuncHeader), reg); 
		list.add(i);
		return reg;
	}

	public Object visit(Length length) {
		
		Op one = (Op) length.getArray().accept(this);
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		DataTransferInstructionType	 AIT;
		
		AIT = DataTransferInstructionType.ArrayLength;
		Instruction i = new DataTransferInstruction(one,reg, AIT); // TBD - how do we handle arithmetic instructions that have only one paramater-  currently im passing the same op twice
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

		if (two.getOpType() != OpType.Reg)
			System.out.println("THROW NEW ERROR"); // TBD Throw error
		Instruction i;
		if (isString)
		{
			i = new LibraryInstruction(new Op("__stringCat(" + one.getName() +
					"," + two.getName()	+ ")", OpType.FuncHeader), one);
		}
		else
			i = new ArithmeticInstruction(two,one, AIT);
			
		list.add(i);
		return one;
	}

	public Object visit(LogicalBinaryOp binaryOp) {

		LogicalInstructionType LIT;
		DataTransferInstructionType  DIT;
		Op one = (Op) binaryOp.getFirstOperand().accept(this);
		Op two = (Op) binaryOp.getSecondOperand().accept(this);

		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		Op zero = new Op("0", OpType.Immediate);
		DIT = DataTransferInstructionType.Move;
		Instruction i = new DataTransferInstruction(zero, reg, DIT);
		list.add(i);
		
		
		Op jumpto = new Op(Jump.getnextjumpcounter(),OpType.Label);
		
		if (binaryOp.getOperator() == IC.BinaryOps.LAND)
			{
			LIT = LogicalInstructionType.And;
		  i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			return (two);
			}
		if (binaryOp.getOperator() == IC.BinaryOps.LOR)
			{
			LIT = LogicalInstructionType.Or;
			  i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			return (two);
			}
	
		if (binaryOp.getOperator() == IC.BinaryOps.EQUAL)
			{
			LIT = LogicalInstructionType.Compare;
			i = new LogicalInstruction(one, two, LIT);
			list.add(i);
			i = new ControlTransferInstruction(jumpto,ControlTransferInstructionType.JumpFalse); 
			}
		if (binaryOp.getOperator() == IC.BinaryOps.NEQUAL)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransferInstruction(jumpto,ControlTransferInstructionType.JumpTrue); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.GT)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransferInstruction(jumpto,ControlTransferInstructionType.JumpLE); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.GTE)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransferInstruction(jumpto,ControlTransferInstructionType.JumpL); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LTE)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransferInstruction(jumpto,ControlTransferInstructionType.JumpG); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LT)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransferInstruction(jumpto,ControlTransferInstructionType.JumpGE); 
	
		}
	
		Op one2 = new Op("1", OpType.Immediate);
		DIT = DataTransferInstructionType.Move;
		i = new DataTransferInstruction(one2, reg, DIT);
		list.add(i);
		list.add(new Label(jumpto.getName()));
		return reg;
	}

	public Object visit(MathUnaryOp unaryOp) {
		
		Op one = (Op) unaryOp.getOperand().accept(this);
		ArithmeticInstructionType AIT;
		AIT = ArithmeticInstructionType.Neg;
		Instruction i = new ArithmeticInstruction(one, AIT); // TBD - how do we handle arithmetic instructions that have only one parameter-  currently im passing the same op twice
		list.add(i);
		
		return (one);
	}

	public Object visit(LogicalUnaryOp unaryOp) {
		Op one = (Op) unaryOp.getOperand().accept(this);
		LogicalInstructionType AIT;
		AIT = LogicalInstructionType.Not;
		Instruction i = new LogicalInstruction(one,one, AIT); // TBD - how do we handle arithmetic instructions that have only one paramater-  currently im passing the same op twice
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
		if ((literal.getType() == IC.LiteralTypes.FALSE)&&(literal.getType() == IC.LiteralTypes.NULL)) {
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
			if (!StringLiteral.isIn(strlit))
			{
				StringLiteral.addtoliterals(strlit);
			}
			strOutput = StringLiteral.getindexoflit(strlit);
			Op reg = new Op(Register.getFreeReg(), OpType.Reg);
			Op strLitOp = new Op(strOutput, OpType.Var);
			list.add(new DataTransferInstruction(strLitOp,reg,DataTransferInstructionType.Move).setOptComment("assigning string lit to register"));
			return (reg);
		}

		throw new RuntimeException("Lit Exception");
	}

	public Object visit(ExpressionBlock expressionBlock) {
		

	return expressionBlock.getExpression().accept(this);

	}
	
	
	private int getClassFieldsNumber(ICClass icClass)
	{
		int fieldsNums = 0;
		ICClass superClass = null;
		//String superClassName = "";
		
		if (icClass == null)
		{
			return 0;
		}
		else
		{
			for (Field field : icClass.getFields())
			{
				field.hashCode(); //Remove compiler warning
				fieldsNums++;
			}
			
			if(icClass.hasSuperClass())
			{
				superClass = getIcClassFromName(icClass.getSuperClassName());
			}
			
			return (fieldsNums + getClassFieldsNumber(superClass));
		}
	}
	
	private ICClass getIcClassFromName(String className) {
		ICClass toRet = null;
		
		for (ICClass icClass : this.prog.getClasses())
		{
			if(icClass.getName().equals(className))
			{
				toRet = icClass;
				break;
			}
		}
		return toRet;
	}
}