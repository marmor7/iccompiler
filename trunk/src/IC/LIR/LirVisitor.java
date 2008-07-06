package IC.LIR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import IC.AST.*;
import IC.LIR.ArithmeticInstruction.ArithmeticInstructionType;
import IC.LIR.ControlTransfer.ControlTransferInstructionType;
import IC.LIR.DataTransferInstruction.DataTransferInstructionType;
import IC.LIR.LibraryInstruction.LibraryInstructionType;
import IC.LIR.LogicalInstruction.LogicalInstructionType;
import IC.LIR.Op.OpType;

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

	/**
	 * Constructs a new LIR visitor.
	 * 
	 */
	public LirVisitor(String progName) {
		this.progName = progName;
		// TBD: init some fields here
	}

	public Object visit(Program program) {

		list.add(new Label(progName, "Program start"));

		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
				
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
		
		curDt = new DispatchTable(icClass.getName());
		dTables.put(icClass.getName(), curDt);

		if (icClass.hasSuperClass()) {
			curDt.setParent(dTables.get(icClass.getSuperClassName()));
		}

		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods())
			method.accept(this);

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

		curDt.addField(field);

		field.getType().accept(this);

		return null;
	}

	public Object visit(LibraryMethod method) {
		StringBuffer output = new StringBuffer();

		output.append("Declaration of library method: " + method.getName());

		output.append(method.getType().accept(this));
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));

		return null;
	}

	public Object visit(Formal formal) {
		StringBuffer output = new StringBuffer();

		output.append("Parameter: " + formal.getName());

		output.append(formal.getType().accept(this));

		return null;
	}

	public Object visit(VirtualMethod method) {

		curDt.addMethod(method);
		method.getType().accept(this);

		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements()) {
			list.add(new Comment("Line " + statement.getLine() + ": "));
			statement.accept(this);
		}

		return null;
	}

	public Object visit(StaticMethod method) {

		if (method.getName().equals("main")){
			list.add(new Label("ic_" + method.getName(), 
					"Main Method"));
		}
		else {
			curDt.addMethod(method);
			list.add(new Label(method.getName(), 
					"Method " + method.getName() + ":"));
		}

		method.getType().accept(this);

		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements()) {
			list.add(new Comment("Line " + statement.getLine() + ": "));
			statement.accept(this);
		}
		
		if (method.getName().equals("main")){
			list.add(new LibraryInstruction(dummyOp, LibraryInstructionType.Exit));
		}
		
		list.add(new Comment("Method " + method.getName() + " end"));
		
		return null;
	}

	public Object visit(Assignment assignment) {

		try//TBD - after all visitors return OP remove try-catch
		{
			Op loc =   (Op)assignment.getVariable().accept(this);
			Op value = (Op)assignment.getAssignment().accept(this);
			
			// Copy value into location:
			Instruction ins = new DataTransferInstruction(value, loc,
									DataTransferInstructionType.Move);
			ins.setOptComment("(Assignment statement)");
			list.add(ins);
					
		}finally{
			System.out.println("casting error - need to implement something"); //TMP!
		}

		return null;
	}

	public Object visit(CallStatement callStatement) {
		StringBuffer output = new StringBuffer();

		output.append("Method call statement");

		output.append(callStatement.getCall().accept(this));

		return null;
	}

	public Object visit(Return returnStatement) {
	//	if (returnStatement.getValue() == null)
		//	list.add(new)
		
		
		StringBuffer output = new StringBuffer();

		output.append("Return statement");
		if (returnStatement.hasValue())
			output.append(", with return value");
		if (returnStatement.hasValue()) {

			output.append(returnStatement.getValue().accept(this));

		}
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
		ControlTransfer ctThen = new ControlTransfer(new Op(thenLabel, OpType.Label), null, 
				 									 ControlTransferInstructionType.JumpLE);
		ctThen.setOptComment("Conditional jump when If's outcome is true");
		list.add(ctThen);
		
		//If we are here, means that If's condition is false. Goto else label if exists.
		if (ifStatement.hasElse())
		{			
			ControlTransfer ctElse = new ControlTransfer(new Op(elseLabel, OpType.Label), null, 
					 				 ControlTransferInstructionType.Jump);
			ctElse.setOptComment("Jump to else label");
			list.add(ctElse);
		}
		
		//If's then label
		list.add(new Label(thenLabel,"If's then label"));
		
		//If's then part
		ifStatement.getOperation().accept(this);
		
		//Then must jump to then label in order not to execute else's part
		ControlTransfer ctEnd = new ControlTransfer(new Op(endLabel, OpType.Label), null,
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
		ControlTransfer ct = new ControlTransfer(new Op(falseWhileLabel, OpType.Label), null, 
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
		ControlTransfer ctBreak = new ControlTransfer(new Op(this.latestWhileEndLabel, OpType.Label), null, 
				 ControlTransferInstructionType.Jump);
		ctBreak.setOptComment("Break statement. Jump to end of while statement label: " + this.latestWhileEndLabel);
		list.add(ctBreak);
		
		return null;
	}

	public Object visit(Continue continueStatement) {
		//Upon continue statement, jump to closest while start label:
		ControlTransfer ctContinue = new ControlTransfer(new Op(this.latestWhileStartLabel, OpType.Label), null, 
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
			list.add(new DataTransferInstruction( (Op)localVariable.getInitValue().accept(this) ,new Op(localVariable.getName(),OpType.Memory),DataTransferInstructionType.Move)); 			
			return new Op(localVariable.getName(),OpType.Memory);
		}

		return null;
	}

	public Object visit(VariableLocation location) {

		// Put the variable's value into a register
		Op reg = new Op(Register.getFreeReg(), OpType.Reg);
		Op var = new Op(location.getName(), OpType.Var);

		list.add(new DataTransferInstruction(var, reg,
				DataTransferInstructionType.Move));

		if (location.isExternal()) {
			// TBD: get location from dispatch vector
			location.getLocation().accept(this);
		}

		return reg;
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
		StringBuffer output = new StringBuffer();

		output.append("Call to static method: " + call.getName()
				+ ", in class " + call.getClassName());

		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));

		return null;
	}

	public Object visit(VirtualCall call) {
		StringBuffer output = new StringBuffer();

		output.append("Call to virtual method: " + call.getName());
		if (call.isExternal())
			output.append(", in external scope");

		if (call.isExternal())
			output.append(call.getLocation().accept(this));
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));

		return null;
	}

	public Object visit(This thisExpression) {
		StringBuffer output = new StringBuffer();

		output.append("Reference to 'this' instance");
		return null;
	}

	public Object visit(NewClass newClass) {
		StringBuffer output = new StringBuffer();

		output.append("Instantiation of class: " + newClass.getName());
		return null;
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
		i = new LibraryInstruction(size,reg,LibraryInstructionType.AllocateArray); 
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

		ArithmeticInstructionType AIT;
		if (binaryOp.getOperator() == IC.BinaryOps.PLUS)
			AIT = ArithmeticInstructionType.Add;
		if (binaryOp.getOperator() == IC.BinaryOps.MINUS)
			AIT = ArithmeticInstructionType.Sub;
		if (binaryOp.getOperator() == IC.BinaryOps.MULTIPLY)
			AIT = ArithmeticInstructionType.Mul;
		if (binaryOp.getOperator() == IC.BinaryOps.DIVIDE)
			AIT = ArithmeticInstructionType.Div;
		else
			// if (binaryOp.getOperator() == IC.BinaryOps.MOD)
			AIT = ArithmeticInstructionType.Mod;

		Op one = (Op) binaryOp.getFirstOperand().accept(this);
		Op two = (Op) binaryOp.getSecondOperand().accept(this);

		if (two.getOpType() != OpType.Reg)
			System.out.println("THROW NEW ERROR"); // TBD Throw error
		Instruction i = new ArithmeticInstruction(one, two, AIT);
		list.add(i);
		return two;
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
			i = new ControlTransfer(jumpto,jumpto,ControlTransferInstructionType.JumpFalse); 
			}
		if (binaryOp.getOperator() == IC.BinaryOps.NEQUAL)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransfer(jumpto,jumpto,ControlTransferInstructionType.JumpTrue); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.GT)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransfer(jumpto,jumpto,ControlTransferInstructionType.JumpLE); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.GTE)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransfer(jumpto,jumpto,ControlTransferInstructionType.JumpL); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LTE)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransfer(jumpto,jumpto,ControlTransferInstructionType.JumpG); 
		}
		if (binaryOp.getOperator() == IC.BinaryOps.LT)
		{
		LIT = LogicalInstructionType.Compare;
		i = new LogicalInstruction(one, two, LIT);
		list.add(i);
		i = new ControlTransfer(jumpto,jumpto,ControlTransferInstructionType.JumpGE); 
	
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
		Instruction i = new ArithmeticInstruction(one,one, AIT); // TBD - how do we handle arithmetic instructions that have only one parameter-  currently im passing the same op twice
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
			String strlit = (String) literal.getValue();
			String strOutput;
			if (!StringLiteral.isIn(strlit))
			{
				StringLiteral.addtoliterals(strlit);
			}
			strOutput = StringLiteral.getindexoflit(strlit);
			return (new Op(strOutput, OpType.Var));
		}

		return null;
	}

	public Object visit(ExpressionBlock expressionBlock) {
		StringBuffer output = new StringBuffer();

		output.append("Parenthesized expression");

		output.append(expressionBlock.getExpression().accept(this));

		return null;
	}
}