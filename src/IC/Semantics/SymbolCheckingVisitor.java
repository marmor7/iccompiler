package IC.Semantics;

import java.util.Map;
import IC.AST.*;
import IC.Semantics.SymbolTable.ScopeKind;

/**
 * Symbol building visitor - runs after Type visitor
 */
public class SymbolCheckingVisitor implements Visitor {
	
	private SymbolTable globScope;
	private SymbolTable curScope;
	private TypeTable   types;
	private Map<String, ICClass> classesList;
	private int loopcounter=0;
	
	public SymbolCheckingVisitor(TypeTable typeTable, SymbolTable sTable, Map<String, ICClass> clist){
		types = typeTable;
		globScope = sTable;
		classesList = clist;
	}
	
	public TypeTable getTypes()
	{
		return types;
	}
	
	public SymbolTable getScope()
	{
		return globScope;
	}
	
	public Map<String, ICClass> getClasses()
	{
		return classesList;
	}
	
	public Object visit(Program program) {
		program.setScope(globScope); 		
		
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		
		return globScope;
	}

	public Object visit(ICClass icClass) {
	
		//Start a new scope
		curScope = icClass.enclosingScope();
		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods())
			method.accept(this);
		
		//Return to global scope
		curScope = globScope;
		
		return null;
	}

	public Object visit(PrimitiveType type){
		type.setScope(curScope);
		
		//Check type is in TypeTable
		TypeClassMold t = types.getType(type.getName(), type.getDimension());
		if (t == null)
			Utils.handleSemanticError(new SemanticError(type.getLine(), 
					"Undefined type " + type.getName()));

		return null;
	}

	public Object visit(UserType type) {
		type.setScope(curScope);

		//Check type is in TypeTable
		TypeClassMold t = types.getType(type.getName(), type.getDimension());
		if (t == null)
			Utils.handleSemanticError(new SemanticError(type.getLine(), 
					"Undefined type " + type.getName()));
		
		return null;
	}

	public Object visit(Field field) {
		field.setScope(curScope);
		
		field.getType().accept(this);
		
		return curScope.getField(field.getName());
	}

	public Object visit(LibraryMethod method) {
		
		SymbolClass class1 = curScope.getMethod(method.getName());
		if (class1 == null)
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
				"library method not defined " + method.getName()));
		
		method.getType().accept(this); 
		
		// Start a new scope for this method
		curScope = curScope.getChild(method.getName());
		
		for (Formal formal : method.getFormals())
			formal.accept(this);
		
		//Return to enclosing scope
		curScope = curScope.getParent();
		
		return class1;
	}

	public Object visit(Formal formal) {

		formal.setScope(curScope);
		
		formal.getType().accept(this);
		
		TypeClass t = (TypeClass)types.getType(formal.getType().getName(), formal.getType().getDimension());
		if (t == null)
			Utils.handleSemanticError(new SemanticError(formal.getLine(), 
					"formal's type not defined"));
		
		return curScope.getParameter(formal.getName());
	}

	public Object visit(VirtualMethod method) {

		SymbolClass class1 = curScope.getMethod(method.getName());
		
		method.getType().accept(this); 
		
		// get scope for this method
		curScope = curScope.getChild(method.getName());
		
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements())
			statement.accept(this);
		
		//Return to enclosing scope
		curScope = curScope.getParent();
		
		return class1;
	}

	public Object visit(StaticMethod method) {

		SymbolClass class1 = curScope.getMethod(method.getName());
		method.getType().accept(this); 
		
		// Start a new scope for this method
		curScope = curScope.getChild(method.getName());
		
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements())
			statement.accept(this);
		
		//Return to enclosing scope
		curScope = curScope.getParent();
		
		return class1;
	}

	public Object visit(Assignment assignment) {
		assignment.setScope(curScope);

		assignment.getVariable().accept(this);
		assignment.getAssignment().accept(this);

		return null;
	}

	public Object visit(CallStatement callStatement) {
		callStatement.setScope(curScope);

		return callStatement.getCall().accept(this);
	}

	public Object visit(Return returnStatement) {
		returnStatement.setScope(curScope);
		
		if (returnStatement.hasValue()) {
			return returnStatement.getValue().accept(this);
		}
		return null;
	}

	public Object visit(If ifStatement) {
		ifStatement.setScope(curScope);

		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);

		return null;
	}

	public Object visit(While whileStatement) {
		loopcounter++;
		whileStatement.setScope(curScope);
		
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);

		loopcounter--;
		return null;
		
	}

	public Object visit(Break breakStatement) {
		breakStatement.setScope(curScope);
			
		if (loopcounter <= 0)
			Utils.handleSemanticError(new SemanticError(breakStatement.getLine(), 
					"Break statement not inside a loop"));

		return null;
	}

	public Object visit(Continue continueStatement) {
		continueStatement.setScope(curScope);

		if (loopcounter <= 0)
			Utils.handleSemanticError(new SemanticError(continueStatement.getLine(), 
					"Continue Statement not inside a loop"));

		return null;
	}

	public Object visit(StatementsBlock statementsBlock) {
		// Start a new scope for this method
		curScope = curScope.getChild("_block_" + statementsBlock.getLine() + "_" + curScope.getId());
		
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);
		
		//Return to enclosing scope
		curScope = curScope.getParent();
		
		return null;
	}

	public Object visit(LocalVariable localVariable) {
		localVariable.setScope(curScope);
		
		SymbolClass class1 = curScope.getVariable(localVariable.getName());
		if (class1 == null)
			Utils.handleSemanticError(new SemanticError(localVariable.getLine(), 
					"Local variable '" + localVariable.getName() + "' not defined"));
		
		localVariable.getType().accept(this);
		
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
		}

		return class1;
	}

	public Object visit(VariableLocation location) {

		//Verify variable had been declared
		SymbolTable temp = curScope;
		
		if (location.isExternal()) {
			
			 SymbolClass sym = (SymbolClass) location.getLocation().accept(this);
			if (sym == null)
				Utils.handleSemanticError(new SemanticError(location.getLine(), 
						"external location illegal sym:'" + sym + "')"));
			
			TypeClass tc = (TypeClass) sym.getType();
			
			ICClass c = classesList.get(tc.getId());
			if (c == null)
				Utils.handleSemanticError(new SemanticError(location.getLine(), 
						"external location illegal (c:'" + c + "')"));
			temp = c.enclosingScope();
			location.setScope(temp);
			
		}
		else
		{
			location.setScope(curScope);
		}
		
		SymbolClass class1 = temp.searchVariable(location.getName());
		if ((temp != null) && (class1 == null))
			Utils.handleSemanticError(new SemanticError(location.getLine(), 
					"variable '" + location.getName() + "' used without being declared."));
		
		return class1;
	}

	public Object visit(ArrayLocation location) {
		location.setScope(curScope);
		
		SymbolClass class1 = (SymbolClass) location.getArray().accept(this);
		location.getIndex().accept(this);

		return class1;
	}

	public Object visit(StaticCall call) {
		call.setScope(curScope);

		for (Expression argument : call.getArguments())
			argument.accept(this);

		return null;
	}

	public Object visit(VirtualCall call) {

		SymbolTable temp = curScope;
		
		if (call.isExternal()) {
			
			SymbolClass sym = (SymbolClass) call.getLocation().accept(this);
			
			if (sym == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						"external location illegal sym:'" + sym + "')"));
			
			TypeClass tc = (TypeClass) sym.getType();
			
			ICClass c = classesList.get(tc.getId());
			if (c == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						"external location illegal (c:'" + c + "')"));
			temp = c.enclosingScope();
			call.setScope(temp);
		}
		else
		{
			call.setScope(curScope);
		}
		
		for (Expression argument : call.getArguments())
			argument.accept(this);
		
		SymbolClass class1 = temp.searchMethod(call.getName());
		if ((temp != null) && (class1 == null))
			Utils.handleSemanticError(new SemanticError(call.getLine(), 
					"method '" + call.getName() + "' used without being declared."));

		return null;
	}

	public Object visit(This thisExpression) {
		thisExpression.setScope(curScope);
		
		SymbolTable scopeit = curScope;
		
		while (scopeit != globScope)
		{
			if (scopeit.getKind() == ScopeKind.METHOD)
			{
				MethodSigType mst = types.getMethodSig(scopeit.getId(), scopeit.getParent().getId());
				if (!mst.getStatic())
					return scopeit.searchVariable("this");
			}
			scopeit = scopeit.getParent();
		}
		Utils.handleSemanticError(new SemanticError(thisExpression.getLine(), 
			"'this' keyword used in static method"));

		return null;
	}

	public Object visit(NewClass newClass) {
		
		newClass.setScope(curScope);

		ICClass c = classesList.get(newClass.getName());
		if ((c == null) || (c.enclosingScope() == null))
			Utils.handleSemanticError(new SemanticError(newClass.getLine(), "class not defined"));
				
		SymbolClass class1 = c.enclosingScope().searchVariable("this");
		if (class1 == null)
			Utils.handleSemanticError(new SemanticError(newClass.getLine(), 
					"class not defined"));
		
		return class1;
	}

	public Object visit(NewArray newArray) {
		newArray.setScope(curScope);
		
		newArray.getType().accept(this);
		newArray.getSize().accept(this);

		return null;
	}

	public Object visit(Length length) {
		length.setScope(curScope);
		
		length.getArray().accept(this);

		return null;
	}

	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.setScope(curScope);
		
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);

		return null;
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.setScope(curScope);
		
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);

		return null;
	}

	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.setScope(curScope);

		unaryOp.getOperand().accept(this);

		return null;
	}

	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.setScope(curScope);
		
		unaryOp.getOperand().accept(this);

		return null;
	}

	public Object visit(Literal literal) {
		literal.setScope(curScope);
		
		return null;
	}

	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.setScope(curScope);

		return expressionBlock.getExpression().accept(this);
	}
}