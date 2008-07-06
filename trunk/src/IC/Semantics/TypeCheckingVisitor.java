package IC.Semantics;

import java.util.Iterator;

import IC.AST.*;
import IC.Semantics.SymbolClass.SymbolKind;
import IC.Semantics.SymbolTable.ScopeKind;

/**
 * Type building visitor
 */
public class TypeCheckingVisitor implements Visitor {
	
	private TypeTable tTable;
	private TypeClass retType = null;
	private int       staticMode;
	
	public TypeCheckingVisitor(TypeTable filledTypeTable){
		tTable = filledTypeTable;
		staticMode = 0;
	}
	
	public Object visit(Program program) {
		
		//Visit all program classes
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		
		return null;
	}

	public Object visit(ICClass icClass) {
			
		for (Field field : icClass.getFields())
			 field.accept(this);
		
		for (Method method : icClass.getMethods())
			method.accept(this);
		
		return null;
	}

	public Object visit(PrimitiveType type) {
		
		TypeClass t = (TypeClass) tTable.getType(type.getName(), type.getDimension());
		//Handle the case when type does exist in type table. 
		if(t == null)
		{
			Utils.handleSemanticError(new SemanticError(type.getLine(), type.getName() + 
									  " with dimension " + type.getDimension() + " is undefined."));
		}
		//If type != null, return it.
		return t;
	}

	public Object visit(UserType type) {
		TypeClass t = (TypeClass) tTable.getType(type.getName(), type.getDimension());
		//Handle the case when type does exist in type table. 
		if(t == null)
		{
			Utils.handleSemanticError(new SemanticError(type.getLine(), type.getName() + 
									  " with dimension " + type.getDimension() + " is undefined."));
		}
		//If type != null, return it.
		return t;
	}

	public Object visit(Field field) {
		return field.getType().accept(this);
	}

	public Object visit(LibraryMethod method) {
		
		SymbolClass mthd = method.enclosingScope().searchMethod(method.getName());
		if (mthd == null) 
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					"Method " + method.getName() + " not found in scope"));
		
		MethodSigType mType = (MethodSigType)mthd.getType();
		Iterator<TypeClass> it = mType.getParams().iterator();
		TypeClass cur = null;
		
		for (Formal formal : method.getFormals())
		{
			if (it.hasNext())
				cur = it.next();
			else
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						method.getName() + " formals do not match signature"));
			
			if (formal.accept(this) != cur)
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						method.getName() + " formals do not match signature"));
		}
		if (it.hasNext())
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					method.getName() + " formals do not match signature"));
		
		method.getType().accept(this);
		
		return mType.getReturnVal();
	}

	public Object visit(Formal formal) {
		return formal.getType().accept(this);
	}

	public Object visit(VirtualMethod method) {
		
		SymbolClass mthd = method.enclosingScope().searchMethod(method.getName());
		if (mthd == null) 
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					"Method " + method.getName() + " not found in scope"));
		
		MethodSigType mType = (MethodSigType)mthd.getType();
		Iterator<TypeClass> it = mType.getParams().iterator();
		TypeClass cur = null;
		
		//Check formals types
		for (Formal formal : method.getFormals())
		{
			if (it.hasNext())
				cur = it.next();
			else
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						method.getName() + " formals do not match signature"));
			
			if (formal.accept(this) != cur)
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						method.getName() + " formals do not match signature"));
		}
		if (it.hasNext())
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					method.getName() + " formals do not match signature"));
		
		//Go over statements in the method
		TypeClass old = retType;
		retType = mType.getReturnVal();
		for (Statement statement : method.getStatements())
			statement.accept(this);
		retType = old;
		
		method.getType().accept(this);
		
		return mType.getReturnVal();
	}

	public Object visit(StaticMethod method) {
		
		staticMode++;
		SymbolClass mthd = method.enclosingScope().searchMethod(method.getName());
		if (mthd == null) 
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					"Method " + method.getName() + " not found in scope"));
		
		MethodSigType mType = (MethodSigType)mthd.getType();
		Iterator<TypeClass> it = mType.getParams().iterator();
		TypeClass cur = null;
		
		//Check formals types
		for (Formal formal : method.getFormals())
		{
			if (it.hasNext())
				cur = it.next();
			else
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						method.getName() + " formals do not match signature"));
			
			if (formal.accept(this) != cur)
				Utils.handleSemanticError(new SemanticError(method.getLine(), 
						method.getName() + " formals do not match signature"));
		}
		if (it.hasNext())
			Utils.handleSemanticError(new SemanticError(method.getLine(), 
					method.getName() + " formals do not match signature"));
		
		//Go over statements in the method
		TypeClass old = retType;
		retType = mType.getReturnVal();
		for (Statement statement : method.getStatements())
			statement.accept(this);
		retType = old;
		
		method.getType().accept(this);
		
		staticMode--;
		return mType.getReturnVal();
	}

	public Object visit(Assignment assignment) {
		
		TypeClass loc = (TypeClass) assignment.getVariable().accept(this);
		TypeClass exp = (TypeClass) assignment.getAssignment().accept(this);
		
		if (!exp.subTypeOf(loc))
				Utils.handleSemanticError(new SemanticError(assignment.getLine(), "assignment types do not agree"));

		// Assignment does not return a value -> return null
		return null;
	}

	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);
		
		return null;
	}

	public Object visit(Return returnStatement) {
		
		if (!returnStatement.hasValue())
		{
			if (retType.getId() != "void")
				Utils.handleSemanticError(new SemanticError(returnStatement.getLine(), 
						"return type should be non-void"));
		}
		else 
		{
			TypeClass t = (TypeClass)returnStatement.getValue().accept(this);
			if (!t.subTypeOf(retType))
				Utils.handleSemanticError(new SemanticError(returnStatement.getLine(), 
						"wrong return type"));
		}
		return null;
	}

	public Object visit(If ifStatement) {
		
		TypeClass temp = (TypeClass) ifStatement.getCondition().accept(this);
		
		//Get the original primitive boolean type
		TypeClass booleanType = getPrimitiveTypeClass("boolean", 0);
	
		if (temp != booleanType)
		{
			Utils.handleSemanticError(new SemanticError(ifStatement.getLine(), "if statement's case must be of type boolean"));
		}

		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);

		return null;
	}

	public Object visit(While whileStatement) {

		TypeClass temp = (TypeClass) whileStatement.getCondition().accept(this);
		
		//Get the original primitive boolean type
		TypeClass booleanType = getPrimitiveTypeClass("boolean", 0);
		
		
		if (temp != booleanType)
		{
			Utils.handleSemanticError(new SemanticError(whileStatement.getLine(), 
				"while condition must be of type boolean"));
		}
		
		whileStatement.getOperation().accept(this);

		return null;
	}

	public Object visit(Break breakStatement) {
		
		return null;
	}

	public Object visit(Continue continueStatement) {
		
		return null;
	}

	public Object visit(StatementsBlock statementsBlock) {

		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);

		return null;
	}

	public Object visit(LocalVariable localVariable) {
		
		TypeClass init = null;
		if (localVariable.hasInitValue()) {
			init = (TypeClass) localVariable.getInitValue().accept(this);
		}
		
		TypeClass t = (TypeClass) localVariable.getType().accept(this);
		
		if ((init != null) && (!init.subTypeOf(t)))
				Utils.handleSemanticError(new SemanticError(localVariable.getLine(), 
						"Wrong type in assignment"));

		return t;
	}

	public Object visit(VariableLocation location) {
		
		SymbolClass s = null;
		
		if (location.isExternal()) {
			location.getLocation().accept(this);
		}
		
		if (staticMode > 0)
		{
			SymbolTable sym = location.enclosingScope();
			
			while ((s == null) && (sym != null))
			{
				if ((!location.isExternal()) && (sym.getKind() == ScopeKind.CLASS))
					break;
				s = sym.getVariable(location.getName());
				sym = sym.getParent();
			}
		}
		else
			s = location.enclosingScope().searchVariable(location.getName());
		
		if (s == null)
			Utils.handleSemanticError(new SemanticError(location.getLine(), 
				"variable " + location.getName() + " wasn't declared"));
		TypeClass t = (TypeClass) s.getType();
		
		return t;
	}

	public Object visit(ArrayLocation location) {

		TypeClass t = (TypeClass) location.getArray().accept(this);
		
		if (t.getDimension() <= 0)
			Utils.handleSemanticError(new SemanticError(location.getLine(), 
					"variable is not an array"));
			
		TypeClass i = (TypeClass) location.getIndex().accept(this);
		
		TypeClass intType = getPrimitiveTypeClass("int", 0);
		
		if (i != intType)
			{
				Utils.handleSemanticError(new SemanticError(location.getLine(), "index must be of type int"));
			}
		
		t = tTable.getType(t.getId(), t.getDimension() - 1);
		return t;
	}

	public Object visit(StaticCall call) {

		 MethodSigType t = tTable.getMethodSig(call.getName(), call.getClassName());
		 if (t == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getClassName() + " wasn't declared or " + call.getName() + " wasn't declared" ));
		if (t.getStatic() == false)
			Utils.handleSemanticError(new SemanticError(call.getLine(), 
					call.getName() + " Internal error - method was recognized as static"));
		
		Iterator<TypeClass> it = t.getParams().iterator();
		TypeClass cur = null;
		
		for (Expression argument : call.getArguments())
		{
			if (it.hasNext())
				cur = it.next();
			else
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " arguments do not match signature, too many args"));
			
			TypeClass argType = (TypeClass) argument.accept(this);
			
			if (!argType.subTypeOf(cur))
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " arguments do not match signature, got " + argType));
		}
		if (it.hasNext())
			Utils.handleSemanticError(new SemanticError(call.getLine(), 
					call.getName() + " arguments do not match signature, too few args"));
		
		return t.getReturnVal();
	}

	public Object visit(VirtualCall call) {
		
		TypeClass tc;
		SymbolClass sym;
		MethodSigType t;
		
		if (call.isExternal())
		{
			tc = (TypeClass)call.getLocation().accept(this);
			if (tc == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " location isn't defined"));
			t = tTable.getMethodSig(call.getName(), tc.getId());
			if (t == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " location isn't defined"));
		}
		else
		{
			sym = call.enclosingScope().searchMethod(call.getName());
			if (sym == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " wasn't declared in scope"));
			
			//Check that we're not in static mode
			if ((sym.getKind() != SymbolKind.STATIC_METHOD) && (staticMode > 0))
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						"call to virtual function " + call.getName() + " is not allowed in a static method"));
			
			t = (MethodSigType)sym.getType();
			if (t == null)
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " wasn't declared in scope"));
		}
		Iterator<TypeClass> it = t.getParams().iterator();
		TypeClass cur = null;
		
		for (Expression argument : call.getArguments())
		{
			if (it.hasNext())
				cur = it.next();
			else
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " arguments do not match signature, too many args"));
			
			TypeClass argType = (TypeClass) argument.accept(this);
			
			if (!argType.subTypeOf(cur))
				Utils.handleSemanticError(new SemanticError(call.getLine(), 
						call.getName() + " arguments do not match signature, got " + argType));
		}
		if (it.hasNext())
			Utils.handleSemanticError(new SemanticError(call.getLine(), 
					call.getName() + " arguments do not match signature, too few args"));
		
		return t.getReturnVal();
	}

	public Object visit(This thisExpression) {
		
		SymbolTable sym = thisExpression.enclosingScope();
		
		while ((sym != null) && (sym.getKind() != ScopeKind.CLASS))
			sym = sym.getParent();
		if (sym == null)
			Utils.handleSemanticError(new SemanticError(thisExpression.getLine(), 
					"illegal 'this'"));
		
		return tTable.getType(sym.getId(), 0);
	}

	public Object visit(NewClass newClass) {
		
		TypeClass t = tTable.getType(newClass.getName(), 0);
		if (t == null)
			Utils.handleSemanticError(new SemanticError(newClass.getLine(), 
					"class " + newClass.getName() + " not found in scope"));
		
		return t;
	}

	public Object visit(NewArray newArray) {
		
		newArray.getType().accept(this);
		newArray.getSize().accept(this);
		
		TypeClass t = tTable.getType(newArray.getType().getName(), newArray.getType().getDimension()+1);
		if (t == null)
		{
			Utils.handleSemanticError(new SemanticError(newArray.getLine(), 
									  newArray.toString() + " is not defined."));
		}

		return t;
	}

	public Object visit(Length length) {

		TypeClass ar = (TypeClass) length.getArray().accept(this);
		
		if (ar.getDimension() == 0)
			Utils.handleSemanticError(new SemanticError(length.getLine(), 
					"length on non-arrays is not allowed"));

		return tTable.getType("int", 0);
	}

	public Object visit(MathBinaryOp binaryOp) {

		TypeClass tLeft  = (TypeClass) binaryOp.getFirstOperand().accept(this);
		TypeClass tRight = (TypeClass) binaryOp.getSecondOperand().accept(this);
		
		
		TypeClass intType = getPrimitiveTypeClass("int", 0);
		TypeClass strType = getPrimitiveTypeClass("string", 0);
		
		switch (binaryOp.getOperator()){
		case PLUS :
			if ( (tLeft == strType) && (tRight == strType) )
				return strType;
			//Case fall through!
		case MINUS : case MULTIPLY : case DIVIDE : case MOD : 
			if ( (tLeft == intType) && (tRight == intType) )
				return intType;
		}
		
			Utils.handleSemanticError(new SemanticError(binaryOp.getLine(), "operator '" + 
					binaryOp.getOperator().getOperatorString() + "' is not defined for provided types"));

		return null;
	}

	public Object visit(LogicalBinaryOp binaryOp) {

		TypeClass tLeft  = (TypeClass) binaryOp.getFirstOperand().accept(this);
		TypeClass tRight = (TypeClass) binaryOp.getSecondOperand().accept(this);
		TypeClass intType = getPrimitiveTypeClass("int", 0);
		TypeClass boolType = getPrimitiveTypeClass("boolean", 0);
			
		switch (binaryOp.getOperator()){
		case GT : case GTE : case LT : case LTE : 
			if ( (tLeft == intType) && (tRight == intType) )
				return boolType;
			break;
		case EQUAL : case NEQUAL :
			if (tLeft.subTypeOf(tRight) || tRight.subTypeOf(tLeft)) 
				return boolType;
			break;
		case LAND : case LOR : 
			if ( (tLeft == boolType) && (tRight == boolType) )
				return boolType;
			break;
		}
		
			Utils.handleSemanticError(new SemanticError(binaryOp.getLine(), "operator '" + 
					binaryOp.getOperator().getOperatorString() + "' is not defined for provided types"));

		return null;
	}

	public Object visit(MathUnaryOp unaryOp) {

		TypeClass opr = (TypeClass) unaryOp.getOperand().accept(this);
		TypeClass intType = getPrimitiveTypeClass("int", 0);
		
		switch (unaryOp.getOperator())
		{
		case UMINUS :
			if (opr == intType)
				return intType;
			break;
		default :
				Utils.handleSemanticError(new SemanticError(unaryOp.getLine(), "Error in parsing, near operator: " + 
					unaryOp.getOperator().getOperatorString()));
		}

		Utils.handleSemanticError(new SemanticError(unaryOp.getLine(), "operator '" + 
					unaryOp.getOperator().getOperatorString() + "' is not defined for provided type"));
			
		return null;
	}

	public Object visit(LogicalUnaryOp unaryOp) {

		TypeClass opr = (TypeClass) unaryOp.getOperand().accept(this);
		TypeClass boolType =getPrimitiveTypeClass("boolean", 0);
		
		switch (unaryOp.getOperator())
		{
		case LNEG :
			if (opr == boolType)
				return boolType;
			break;
		default :
				Utils.handleSemanticError(new SemanticError(unaryOp.getLine(), "Error in parsing, near operator: " + 
					unaryOp.getOperator().getOperatorString()));
		}
		
		Utils.handleSemanticError(new SemanticError(unaryOp.getLine(), "operator '" + 
					unaryOp.getOperator().getOperatorString() + "' is not defined for provided type"));

		return null;
	}

	public Object visit(Literal literal) {

		switch (literal.getType()){
		case FALSE : case TRUE :
			return getPrimitiveTypeClass("boolean", 0);
		case INTEGER : 
			return getPrimitiveTypeClass("int", 0);
		case NULL :
			return getPrimitiveTypeClass("null", 0);
		case STRING :
			return getPrimitiveTypeClass("string", 0);
		}
		return null;
	}

	public Object visit(ExpressionBlock expressionBlock) {

		return expressionBlock.getExpression().accept(this);
	}
	
	/**
	 * Type function gets the TypeClass of a primitive type
	 * and throws an error in case of a Bug - no type exists.
	 *
	 * @param name The name of the primitive type
	 * @param dim The dim of the primitive type
	 * @return <code> TypeClass </code> primitive type, else exception is thrown and the program exits.
	 * 
	 */
	private TypeClass getPrimitiveTypeClass(String name, int dim)
	{
		TypeClass t= tTable.getType(name, dim);
		if(t == null)
		{
			Utils.handleSemanticError(new SemanticError("BUG: No " + name + " type found!"));
		}
		return t;
	}
}