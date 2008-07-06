package IC.Semantics;

import java.util.Iterator;
import java.util.List;

import IC.AST.ASTNode;

public class MethodSigType extends TypeClassMold {

	private String methodName;
	private List<TypeClass> params;
	private TypeClass returnVal;
	private boolean staticMethod;
	private ASTNode node; 

	/**
	 * @param methodName The name of the method
	 * @param staticMethod Method is static or not
	 * @param returnValueId Type of return of the function
	 * @param params List of the function formals
	 * @return <code> true </code> if type added, <code> SemanticException </code> if type already exist.
	 * 
	 * A constructor for a method signature 
	 */
	public MethodSigType(String methodName, boolean staticMethod, TypeClass returnVal, List<TypeClass> params, ASTNode node) 
	{
		super();
		this.methodName = methodName;
		this.staticMethod = staticMethod;
		this.params = params;
		this.returnVal  = returnVal;
		this.node = node;
	}

	public String getName()
	{
		return this.methodName;
	}
	
	public List<TypeClass> getParams()
	{
		return this.params;
	}

	public TypeClass getReturnVal()
	{
		return this.returnVal;
	}
	
	public boolean getStatic()
	{
		return this.staticMethod;		
	}
	
	public boolean isLegalMain()
	{
		if (this.methodName.equals("main") && (this.staticMethod) &&
			(this.returnVal.getDimension() == 0) && (this.returnVal.getId().equals("void")) &&
			(this.params.size() == 1) )
			{
				TypeClass formal1 = this.params.get(0);
				if ( (formal1.getId().equals("string")) &&
						(formal1.getDimension() == 1) )
					return true;
			}
		return false;
	}

	public String sigString()
	{
		String out = "{";
		for (TypeClass param1 : params)
		{
			out += param1.getIdAndDim() + ", ";
		}
		
		if (params.size() > 0) 
			out = out.substring(0, out.length() - 2);
		
		out += " -> " + returnVal.getIdAndDim() + "}";
		
		return out; 
	}
	
	public String toString(){
		String toReturn = this.getIdNum() + " :Method type: {"; 
		
		Iterator<TypeClass> iter = params.iterator();
		while (iter.hasNext())
		{
			TypeClass f = (TypeClass) iter.next();
			toReturn += f.getIdAndDim() + ", ";
			
		}
		if (params.size() > 0)
			toReturn = toReturn.substring(0, toReturn.length() - 2);
		
		toReturn = toReturn + " -> ";
		toReturn = toReturn + returnVal.getId();
		toReturn = toReturn + "}";
		return toReturn;
	}
	
	public boolean equals(MethodSigType other)
	{
		return this.sigString().equals(other.sigString());
	}

	public ASTNode getNode() {
		return node;
	}

}
