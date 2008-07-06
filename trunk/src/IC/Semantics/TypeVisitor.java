package IC.Semantics;

import java.util.ArrayList;

import IC.AST.*;

/**
 * Type building visitor
 */
public class TypeVisitor implements Visitor {
	
	private static final int NO_DIMENSION = 0;
	private TypeTable table;
	private static ASTNode root;
	private String prog;
	
	public TypeVisitor(ASTNode r, String progParam){
		root = r;
		prog = progParam;
	}
	
	public Object addClassesAndPrimitives(Program program) throws SemanticError {
		//Initialize TypeTable
		table = new TypeTable(root, prog);
		
		for (ICClass icClass : program.getClasses())
		{
			if (icClass.hasSuperClass())
			{
				table.addType(icClass.getName(), icClass.getSuperClassName(), icClass.getLine());
			}
			else
			{
				table.addType(icClass.getName(), NO_DIMENSION, icClass.getLine());
			}
		}
		
		return table;
	}
	
	public Object visit(Program program) {		
		try 
		{
			//Initialize TypeTable
			table = (TypeTable) addClassesAndPrimitives(program);
			
			for (ICClass icClass : program.getClasses())
				icClass.accept(this);	
		} 
		catch (SemanticError e) 
		{
			Utils.handleSemanticError(e);
		}
	
		return table;
	}

	public Object visit(ICClass icClass) {
		
		//Class types already added. Propagating down.
		
		//Adding class fields to TypeTable
		for (Field field : icClass.getFields())
			field.accept(this);
		
		//Adding Class methods to TypeTable
		for (Method method : icClass.getMethods())
		{
			//Visit method for type building
			method.accept(this); 
			boolean isStatic = method.getStaticState();
			ArrayList<TypeClass> params = new ArrayList<TypeClass>();
			
			for (int i = 0;i<method.getFormals().size();i++)
				params.add( table.getType( method.getFormals().get(i).getType().getName(),method.getFormals().get(i).getType().getDimension()));

			//Add method to type table
			TypeClass t = table.getType( method.getType().getName(),  method.getType().getDimension());
			table.addType(method.getName(), isStatic, t, method.getType().getDimension(),
					  params, icClass, method.getLine(), method);
		}
		
		return null;
	}

	public Object visit(PrimitiveType type) {
		//Check the type's correctness
		handleType(type);
		
		return null;
	}

	public Object visit(UserType type){		
		//Check the type's correctness
		handleType(type);
		return null;
	}

	public Object visit(Field field) {
		//Visit the field's type
		field.getType().accept(this);
		return null;
	}

	public Object visit(LibraryMethod method) {

		//Set method's static state to true
		method.setStaticState(true);
		
		//Visit return param of the method:
		method.getType().accept(this);
		
		//Visit method formals
		for (Formal formal : method.getFormals())
			formal.accept(this);
				
		return null;
	}

	public Object visit(Formal formal) {
		//Check formal's type definition
		formal.getType().accept(this);

		return null;
	}

	public Object visit(VirtualMethod method) {
		//Set method's static state to false
		method.setStaticState(false);
		
		//Visit return param of the method:
		method.getType().accept(this);
		
		//Visit method formals
		for (Formal formal : method.getFormals())
			formal.accept(this);
		
		//Visit method statements
		for (Statement statement : method.getStatements())
			statement.accept(this);
			
		return null;
	}

	public Object visit(StaticMethod method) {
		//Set method's static state to true
		method.setStaticState(true);
		
		//Visit return param of the method:
		method.getType().accept(this);
		
		//Visit method formals
		for (Formal formal : method.getFormals())
			formal.accept(this);
		
		//Visit method statements
		for (Statement statement : method.getStatements())
			statement.accept(this);
				
		return null;
	}

	public Object visit(Assignment assignment) {

		assignment.getVariable().accept(this);
		assignment.getAssignment().accept(this);
		
		return null;
	}

	public Object visit(CallStatement callStatement) {

		callStatement.getCall().accept(this);

		return null;
	}

	public Object visit(Return returnStatement) {
		
		if (returnStatement.hasValue())
		{
			returnStatement.getValue().accept(this);
		}
		return null;
	}

	public Object visit(If ifStatement) {
		
		//Visit condition
		ifStatement.getCondition().accept(this);
		
		//Visit true statements
		ifStatement.getOperation().accept(this);
	
		//If has else, visit else statements
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);

		return null;
	}

	public Object visit(While whileStatement) {
		//Visit while condition
		whileStatement.getCondition().accept(this);
		
		//Visit while block
		whileStatement.getOperation().accept(this);

		return null;
	}

	public Object visit(Break breakStatement) {
		
		//Nothing to do here for type building visitor
		return null;
	}

	public Object visit(Continue continueStatement) {
		
		//Nothing to do here for type building visitor
		return null;
	}

	public Object visit(StatementsBlock statementsBlock) {
		
		//Visit list of statements
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);

		return null;
	}

	public Object visit(LocalVariable localVariable) {
		
		//Visit variable's type
		localVariable.getType().accept(this);
		
		//Visit variable's value.
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
		}
		
		return null;
	}

	public Object visit(VariableLocation location) {
		//If location is external, visit location
		if (location.isExternal()) {
			location.getLocation().accept(this);
		}
		return null;
	}

	public Object visit(ArrayLocation location) {

		//Visit array's location index
		location.getIndex().accept(this);
		
		//Visit array
		location.getArray().accept(this);

		return null;
	}

	public Object visit(StaticCall call) {
		
		//Visit call's arguments
		for (Expression argument : call.getArguments())
			argument.accept(this);

		return null;
	}

	public Object visit(VirtualCall call) {
		
		//If call is external, visit location
		if (call.isExternal())
			call.getLocation().accept(this);
		
		//Visit call's arguments
		for (Expression argument : call.getArguments())
			argument.accept(this);

		return null;
	}

	public Object visit(This thisExpression) {
		
		//Nothing to do here
		
		return null;
	}

	public Object visit(NewClass newClass) {
		
		//Nothing to do here
		return null;
	}

	public Object visit(NewArray newArray) {
		
		//Visit array's type
		newArray.getType().accept(this);
		
		//Visit array's size
		newArray.getSize().accept(this);

		return null;
	}

	public Object visit(Length length) {
		
		//Visit length's array
		length.getArray().accept(this);

		return null;
	}

	public Object visit(MathBinaryOp binaryOp) {
		
		//Visit the op's operands
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);

		return null;
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		
		//Visit the op's operands
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);

		return null;
	}

	public Object visit(MathUnaryOp unaryOp) {
		
		//Visit the op's operand
		unaryOp.getOperand().accept(this);

		return null;
	}

	public Object visit(LogicalUnaryOp unaryOp) {
	
		//Visit the op's operand
		unaryOp.getOperand().accept(this);

		return null;
	}

	public Object visit(Literal literal) {
		
		//Nothing to do here
		
		return null;
	}

	public Object visit(ExpressionBlock expressionBlock) {
		
		//Visit the block's expression
		expressionBlock.getExpression().accept(this);

		return null;
	}
	
	/**
	 * Handle types - Check base type definition and add types with dims if base type has dim
	 * @param t Type to handle
	 */
	private void handleType(Type t)
	{
		//Type already defined (OK)
		if (checkTypeExistence(t, NO_DIMENSION))
		{
			//Dimension is not 0, need to defined Array type
			if (t.getDimension() != NO_DIMENSION)
			{				
				addTypeWithDimToTypeTable(t, t.getDimension());
			}
			//Dimension is 0, don't do nothing
			else
			{
				//Type alrady exist, do nothing
			}
		}
		//Type not defined
		else
		{
				Utils.handleSemanticError(new SemanticError(t.getLine(), "Use of undefined type: " + t.getName()));
		}
		
	}

	/**
	 * This function checks whether a type with dim exists in the TypeTable.
	 * 
	 * @param t The type to which we should check existence in the TypeTable
	 * @param dim The dim to type we want to check existence to.
	 * @return <code> true </code> iff the type exists in the TypeTable
	 */
	private boolean checkTypeExistence(Type t, int dim) 
	{
		TypeClassMold type = table.getType(t.getName(), dim);
		return (type != null);
	}
	
	/**
	 * Adds array types to type that we assume that base type already exists
	 * 
	 * @param type the base type
	 * @param dimension the max dimension we want to add
	 */
	private void addTypeWithDimToTypeTable(Type type, int dimension) 
	{
		for (int i = 0; i < type.getDimension()+1; i++)
			try 
			{						
				//Type with current dimension already exist
				if (checkTypeExistence(type, i))
				{
					//Do nothing
				}
				//Add type with dimension
				else
				{
					table.addType(type.getName(), i, type.getLine());							
				}		
			}
			catch (SemanticError e) 
			{
	    		Utils.handleSemanticError(e);
			}	
	}
}