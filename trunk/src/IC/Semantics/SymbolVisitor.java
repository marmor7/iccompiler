package IC.Semantics;

import java.util.HashMap;
import java.util.Map;
import IC.AST.*;
import IC.Semantics.SymbolTable.ScopeKind;

/**
 * Symbol building visitor - runs after Type visitor
 */
public class SymbolVisitor implements Visitor {
	
	private SymbolTable globScope;
	private SymbolTable curScope;
	private TypeTable   types;
	private Map<String, ICClass> classesList;
	private boolean roundOne = true;
	
	public SymbolVisitor(TypeTable typeTable, String progName){
		types = typeTable;
		
		// Initialize SymbolTables
		globScope = new SymbolTable(progName, ScopeKind.GLOBAL, null);
	}
	
	public Object visit(Program program) {
		program.setScope(globScope);
		
		classesList = new HashMap<String, ICClass>(); 		
		
		roundOne = true;
		
		for (ICClass icClass : program.getClasses())
		{
			classesList.put(icClass.getName(), icClass);
			icClass.accept(this);
		}
		
		roundOne = false;
		
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		
		//Now check those symbols
		SymbolCheckingVisitor scheck = new SymbolCheckingVisitor(types, globScope, classesList);
		for (ICClass icClass : program.getClasses())
			icClass.accept(scheck);
		
		types = scheck.getTypes();
		globScope = scheck.getScope();
		classesList = scheck.getClasses();
		
		return globScope;
	}

	public Object visit(ICClass icClass) {
	
		//Start a new scope
		if (roundOne)
		{
			if (icClass.hasSuperClass()){
				SymbolTable parentScope = classesList.get(icClass.getSuperClassName()).enclosingScope();
				curScope = new SymbolTable(icClass.getName(), ScopeKind.CLASS, parentScope);
				parentScope.addChild(icClass.getName(), curScope);
			}
			else {
				curScope = new SymbolTable(icClass.getName(), ScopeKind.CLASS, globScope);
				globScope.addChild(icClass.getName(), curScope);
			}
			
			icClass.setScope(curScope);
			
			
			TypeClass t = (TypeClass)types.getType(icClass.getName(), 0);
			if (t==null){
				Utils.handleSemanticError(new SemanticError(icClass.getLine(), 
						"class not defined: " + icClass.getName()));
			}
			
			curScope.insertVariable("this", t);
			
			globScope.insertClass(icClass.getName(), t);
		}

		if (!roundOne)
		{
			curScope = icClass.enclosingScope();
			for (Field field : icClass.getFields())
				field.accept(this);
			for (Method method : icClass.getMethods())
				method.accept(this);
		}
		
		//Return to global scope
		curScope = globScope;
		
		return null;
	}

	public Object visit(PrimitiveType type){
		type.setScope(curScope);
		
		//Check type is in TypeTable
		TypeClassMold t = types.getType(type.getName(), type.getDimension());
		if (t == null)
			Utils.handleSemanticError(new SemanticError(type.getLine(), "Undefined type " + type.getName()));

		return null;
	}

	public Object visit(UserType type) {
		type.setScope(curScope);

		//Check type is in TypeTable
		TypeClassMold t = types.getType(type.getName(), type.getDimension());
		if (t == null)
			Utils.handleSemanticError(new SemanticError(type.getLine(), "Undefined type " + type.getName()));
		
		return null;
	}

	public Object visit(Field field) {
		field.setScope(curScope);
		
		field.getType().accept(this);
		
		//Check that there's no other fields or methods by that name
		if ((curScope.searchField(field.getName()) != null) || (curScope.searchMethod(field.getName()) != null))
			Utils.handleSemanticError(new SemanticError(field.getLine(), "Field name already defined " + field.getName()));
		
		return curScope.insertField(field.getName(), types.getType(field.getType().getName(), field.getType().getDimension()));
	}

	public Object visit(LibraryMethod method) {
		
		SymbolClass class1 = curScope.insertMethod(method.getName(), types.getMethodSig(method.getName(), curScope.getId()));
		
		method.getType().accept(this); 
		
		// Start a new scope for this method
		SymbolTable newScope = new SymbolTable(method.getName(), ScopeKind.METHOD, curScope);
		method.setScope(newScope);
		curScope.addChild(method.getName(), newScope);
		curScope = newScope;
		
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
		if (t==null)
			Utils.handleSemanticError(new SemanticError(formal.getLine(), 
					"formal's type not defined"));
		
		if (curScope.getParameter(formal.getName() ) != null)
			Utils.handleSemanticError(new SemanticError(formal.getLine(), 
					"parameter defined twice: "+formal.getName()));
			
		return curScope.insertParameter(formal.getName(), t);
	}

	public Object visit(VirtualMethod method) {

		if ((curScope.searchField(method.getName()) != null) || (curScope.searchMethod(method.getName()) != null))
		{
			//Check overriding:
			if ((curScope.getField(method.getName()) == null) && (curScope.getMethod(method.getName()) == null))
			{
				SymbolClass sym1 = curScope.searchMethod(method.getName());
				if (sym1 != null)
				{
					MethodSigType sig1 = (MethodSigType) sym1.getType();
					MethodSigType sig2 = types.getMethodSig(method.getName(), curScope.getId());
					if (!sig1.equals(sig2)) //NOT a legal overriding
						Utils.handleSemanticError(new SemanticError(method.getLine(), 
								"Method name already defined (wrong overriding) " + method.getName()));
				}
				else 
					Utils.handleSemanticError(new SemanticError(method.getLine(), 
							"Method name already in use " + method.getName()));
			}
			else
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						"Method name already defined " + method.getName()));
		}

		SymbolClass class1 = curScope.insertMethod(method.getName(), types.getMethodSig(method.getName(), curScope.getId()));
		
		method.getType().accept(this); 
		
		// Start a new scope for this method
		SymbolTable newScope = new SymbolTable(method.getName(), ScopeKind.METHOD, curScope);
		method.setScope(newScope);
		curScope.addChild(method.getName(), newScope);
		curScope = newScope;
		
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements())
			statement.accept(this);
		
		//Return to enclosing scope
		curScope = curScope.getParent();
		
		return class1;
	}

	public Object visit(StaticMethod method) {
		if ((curScope.getField(method.getName()) != null) || (curScope.getMethod(method.getName()) != null))
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					"Method name already defined " + method.getName()));

		SymbolClass class1 = curScope.insertMethod(method.getName(), types.getMethodSig(method.getName(), curScope.getId()));
		method.getType().accept(this); 
		
		// Start a new scope for this method
		SymbolTable newScope = new SymbolTable(method.getName(), ScopeKind.METHOD, curScope);
		method.setScope(newScope);
		curScope.addChild(method.getName(), newScope);
		curScope = newScope;
		
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

		whileStatement.setScope(curScope);
		
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);

		return null;
	}

	public Object visit(Break breakStatement) {
		breakStatement.setScope(curScope);
			
		return null;
	}

	public Object visit(Continue continueStatement) {
		continueStatement.setScope(curScope);

		return null;
	}

	public Object visit(StatementsBlock statementsBlock) {
		// Start a new scope for this method
		SymbolTable newScope = new SymbolTable("_block_" + statementsBlock.getLine() + "_" + 
				curScope.getId(), ScopeKind.BLOCK, curScope);
		statementsBlock.setScope(newScope);
		
		curScope.addChild("_block_" + statementsBlock.getLine() + "_" + curScope.getId(), newScope);
		curScope = newScope;
		
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);
		
		//Return to enclosing scope
		curScope = curScope.getParent();
		
		return null;
	}

	public Object visit(LocalVariable localVariable) {
		localVariable.setScope(curScope);
		
		if (curScope.getVariable(localVariable.getName()) != null)
			Utils.handleSemanticError(new SemanticError(localVariable.getLine(), 
					"Local variable '" + localVariable.getName() + "' defined twice"));

		SymbolClass class1 = curScope.insertVariable(localVariable.getName(), types.getType(localVariable.getType().getName(), localVariable.getType().getDimension()));
		
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
			location.getLocation().accept(this);
			
			location.setScope(temp);
		}
		else
		{
			location.setScope(curScope);
			
			SymbolClass class1 = temp.searchVariable(location.getName());
			if ((temp != null) && (class1 == null))
				Utils.handleSemanticError(new SemanticError(location.getLine(), 
						"variable '" + location.getName() + "' used without being declared."));
		}

		return null;
	}

	public Object visit(ArrayLocation location) {
		location.setScope(curScope);
		
		location.getArray().accept(this);
		location.getIndex().accept(this);

		return null;
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
			call.getLocation().accept(this);
			call.setScope(temp);
		}
		else
		{
			call.setScope(curScope);
			
			SymbolClass class1 = temp.searchMethod(call.getName());
			if ((temp != null) && (class1 == null))
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						"method '" + call.getName() + "' used without being declared."));
		}
		
		for (Expression argument : call.getArguments())
			argument.accept(this);

		return null;
	}

	public Object visit(This thisExpression) {
		thisExpression.setScope(curScope);
		
		SymbolTable scopeit = curScope;
		boolean err = true;
		
		while (scopeit != globScope)
		{
			if (scopeit.getKind() == SymbolTable.ScopeKind.METHOD)
			{
				MethodSigType mst = types.getMethodSig(scopeit.getId(), scopeit.getParent().getId());
				if (!mst.getStatic())
					err = false;
				break;
			}
			scopeit = scopeit.getParent();
		}
		if (err == true)
			Utils.handleSemanticError(new SemanticError(thisExpression.getLine(), 
				"'this' keyword used in static method"));

		return null;
	}

	public Object visit(NewClass newClass) {
		newClass.setScope(curScope);
				
		SymbolClass class1 = curScope.searchVariable("this");
		if (class1 == null)
			Utils.handleSemanticError(new SemanticError(newClass.getLine(), 
					"class not declared"));
		
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