package IC.Semantics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import IC.AST.ASTNode;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.Program;

public class TypeTable {
	
	private static HashMap<String, TypeClassMold> types = new HashMap<String, TypeClassMold>();
	private static final boolean NO_PRIMITIVE = false;
	private static final boolean PRIMITIVE = true;
	private static final int NO_DIMENSION = 0;
	private static ASTNode root;
	private static String prog;
	
	/**
	 * Constructor
	 * Initializes table with primitive types
	 */
	public TypeTable(ASTNode r, String progParam){
		TypeClass tc = null;
		setPrimitivesToTypeTable(tc);
		root = r;
		prog = progParam;
	}

	private void setPrimitivesToTypeTable(TypeClass tc) {
		//int
		tc = new TypeClass("int", NO_DIMENSION, PRIMITIVE);
		types.put(Utils.hashKey(tc.getId(), NO_DIMENSION), tc);
		
		//string
		tc = new TypeClass("string", NO_DIMENSION, PRIMITIVE);
		types.put(Utils.hashKey(tc.getId(), NO_DIMENSION), tc);
		
		//boolean
		tc = new TypeClass("boolean", NO_DIMENSION, PRIMITIVE);
		types.put(Utils.hashKey(tc.getId(), NO_DIMENSION), tc);
		
		//null
		tc = new TypeClass("null", NO_DIMENSION, PRIMITIVE);
		types.put(Utils.hashKey(tc.getId(), NO_DIMENSION), tc);
		
		//void
		tc = new TypeClass("void", NO_DIMENSION, PRIMITIVE);
		types.put(Utils.hashKey(tc.getId(), NO_DIMENSION), tc);
	}
	
	/**
	 * This function add class type to TypeTable with NO superclass.
	 * 
	 * @param id Type's name
	 * @param dim Type's dimension
	 * @return <code> true </code> if type added, <code> SemanticExeption </code> if type already exist.
	 * 
	 * @throws SemanticError 
	 */
	public boolean addType(String id, int dim, int line) throws SemanticError{
		String key = Utils.hashKey(id, dim);
		TypeClass entry = (TypeClass)types.get(key);
		
		//Not defined
		if (entry == null)
		{
			entry = new TypeClass(id, dim, NO_PRIMITIVE);
			types.put(key, entry);
			
			return true; //Type added 
		}
		//Already defined
		else 
		{
			throw new SemanticError(line, "Type redefinition for " + entry.getId());
		}
	}
	
	/**
	 * This function add class type to TypeTable with superclass. 
	 * 
	 * @param id Type name
	 * @param parent Superclass name
	 * @return <code> true </code> if type added, <code> SemanticException </code> if type already exist.
	 * 
	 * @throws SemanticError 
	 */
	public boolean addType(String id, String parent, int line) throws SemanticError{
		String childKey = Utils.hashKey(id, NO_DIMENSION);
		String parentKey = Utils.hashKey(parent, NO_DIMENSION);
		
		TypeClass entry = (TypeClass)types.get(childKey);
		TypeClass parentType =(TypeClass) types.get(parentKey);
		
		//Not defined
		if (entry == null){
			//Has a super class
			if (parentType != null){
				entry = new TypeClass(id, parentType, NO_DIMENSION);
				types.put(childKey, entry);
			}
			//No super class defined
			else 
			{
				//Class cannot extends itself
				if(parent.equals(id))
				{
					throw new SemanticError(line, "Class cannot extends itself: " + id+".");
				}
				else
				{
					throw new SemanticError(line, "No super class definition exist for " + id+".");
				}
				
			}
		}
		//Type already exist
		else
		{
			throw new SemanticError(line, "Type redefinition for " + id);
		}
		
		return true; //Type added
	}
	
	/**
	 * This function add method type to TypeTable.
	 *
	 * @param methodName The name of the method
	 * @param staticMethod Is method static
	 * @param returnValueId Type of return of the function
	 * @param dimOfRetVal Dimension of the return value
	 * @param params List of the function formals
	 * @return <code> true </code> if type added, <code> SemanticException </code> if type already exist.
	 * 
	 */
	public boolean addType(String methodName, boolean staticMethod, TypeClass returnValue, int dimOfRetVal, 
						   List<TypeClass> params, ICClass methodClass, int line, ASTNode node){
		
		//Check for no func redef in the same class (no method overloading in IC)
		String key = Utils.hashKeyForMethods(methodName, methodClass.getName()); 
		TypeClassMold entry = (TypeClassMold)types.get(key);
		
		//Method sig already exists
		if (entry != null)
		{
			Utils.handleSemanticError(new SemanticError(line, "Method definition already exists for " + methodName));

		}
		//Type not exists, OK to add it to TypeTable if overloading is OK
		else
		{
			try
			{
				checkForLegalOverloading(methodName, methodClass, staticMethod, returnValue, dimOfRetVal,
										 params, line);
			}
			catch(SemanticError e)
			{
				Utils.handleSemanticError(e);
			}
			//Add method signature to TypeTable
			entry = new MethodSigType(methodName, staticMethod, returnValue, params, node);
			types.put(key, entry);
		}
		
		return true;
	}

	public TypeClass getType(String id, int dim)
	{
		return (TypeClass) types.get(Utils.hashKey(id, dim));
	}
	
	public MethodSigType getMethodSig(String methodName, String methodClass)
	{
		MethodSigType mst = (MethodSigType) types.get(Utils.hashKeyForMethods(methodName, methodClass));
		if (mst == null)
		{
			String name = Utils.hashKey(methodClass, 0);
			TypeClass cl = (TypeClass) types.get(name);
			TypeClass parent = cl.getParent();
			if (parent != null)
			{
				return getMethodSig(methodName, parent.getId());
			}
			return null;
		}
		else
			return mst;
	}
	
	public Collection<TypeClassMold> getValues()
	{
		return types.values();
	}
	
	/**
	 * Get the static object Program root which is the Program root's node
	 * 
	 * @return Program root
	 */
	public Program getProgramRoot()
	{
		return (Program) root;
	}
	
	/**
	 * Get the ICClass object by its name
	 * 
	 * @param className The class name
	 * @return The ICClass object if the class was found
	 */
	public ICClass getClass(String className)
	{
		//The object to return
		ICClass match = null;
		
		Program program = getProgramRoot(); 
		for (ICClass icClass : program.getClasses())
		{
			//If the class found
			if (icClass.getName().equals(className))
			{
				match = icClass;
				break;
			}
		}
		
		return match;	
	}
	 
	/**
	 * Checks whether the method is overriding another method, and if so is the overriding is semanticly legal.
	 * 
	 * @param methodName The name of the method
	 * @param methodClass The ICClass object of the encapsulating class.
	 * @param staticMethod Is the method declared as static
	 * @param returnValue What is the method return type's value.
	 * @param dimOfRetVal The dimension of the method return value.
	 * @param params List of all method formal parameters.
	 * @return <code> true </code> iff the method override (if so) is sematicly legal, else throw 
	 * 		   <code> SemanticException </code>.
	 * @throws SemanticError With corresponding message
	 */
	private boolean checkForLegalOverloading(String methodName, ICClass methodClass, boolean staticMethod,
			TypeClass returnValue, int dimOfRetVal, List<TypeClass> params, int line) 
					throws SemanticError 
	{
		ICClass parentClass;
		
		//Class has no super class, thus no overriding.
		if (methodClass.hasSuperClass() != true)
		{
			return true;
		}
		//A least one superclass exist, check the entire hierarchy.
		else
		{			
			//while we can go up in class hierarchy, check super classes. 
			do
			{
				parentClass = getClass(methodClass.getSuperClassName());
				assert(parentClass != null); //If assert fails than bug.				
				
				//Get the list of methods in the class
				Iterator<Method> iter = parentClass.getMethods().iterator();
				while (iter.hasNext())
				{
					Method m = iter.next();
					//Check only if current method has the same name as the one we're checking
					if (m.getName().equals(methodName))
					{
						MethodSigType sig = this.getMethodSig(m.getName(),parentClass.getName());
						if (sig == null)
							Utils.handleSemanticError(new SemanticError(m.getLine(), 
									"Method " + m.getName() + " not found in scope"));
						//Check method's return value
						if (!compareMethodsTypesPlusDims(returnValue, sig.getReturnVal() ))
						{
							throw new SemanticError(line, "Method " + methodName + 
									" and superclass return type don't agree.");
						}
						//Check methods formal types
						if (!compareMethodsParams(params,  sig.getParams() ))
						{
							throw new SemanticError(line, "Method " + methodName + 
									" and superclass parameters don't agree.");
						}
						//Check methods static state
						if(!compareMethodsStaticState(staticMethod, m.getStaticState()))
						{
							throw new SemanticError(line, "Method " + methodName + 
									" and superclass static state don't agree.");
						}
					}
				}
				methodClass = parentClass;			
			} while (parentClass.hasSuperClass());
		}
		
		return false;
	}
	
	/**
	 * Checks if 2 method's static state is identical.
	 * 
	 * @param first First method's static state .
	 * @param second Second method's static state.
	 * @return <code> true </code> iff the two booleans are true.
	 */
	private boolean compareMethodsStaticState(boolean first, boolean second) 
	{
		return (first == second);	
	}

	/**
	 * Check if two method's parameters types and order are equal
	 * 
	 * @param fParams First parameters set
	 * @param sParams Second parameters set
	 * 
	 * @return <code> true </code> iff the two set's types are identical.
	 */
	private boolean compareMethodsParams(List<TypeClass> fParams, List<TypeClass> sParams)
	{
		Iterator<TypeClass> fIter = fParams.iterator();
		Iterator<TypeClass> sIter = sParams.iterator();
		do
		{
			//Both has items, compare them
			if(fIter.hasNext() == sIter.hasNext())
			{
				//If still has items in the list, compare them
				if (fIter.hasNext())
				{
					boolean b = compareMethodsTypesPlusDims( fIter.next(), sIter.next());
					
					//Types or dimensions are not the same
					if (!b)
					{
						return false;
					}
				}					
			}
			//One set has more items than the other one
			else
			{ 
				return false;
			}
		} while (fIter.hasNext() || sIter.hasNext());
		
		return true;
	}
	
	/**
	 * Check if the two types are equal
	 * 
	 * @param tFirst The first type
	 * @param dimfirst The second type
	 * @return <code> true </code> iff the two types & their dimensions are equal.
	 */
	private boolean compareMethodsTypesPlusDims(TypeClass tFirst,TypeClass tSecond) {
		int dimfirst = tFirst.getDimension();
		int dimSecond = tSecond.getDimension();
 		
		//Dimension of types must be equal
		if (dimfirst != dimSecond) 
		{
			return false;
		}
		
		//Type's names must be equal
		if (!((tFirst.getId().equals(tSecond.getId()))))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * @override
	 * The <code>toString()</code> implementaion of TypeTable.
	 */
	public String toString()
	{
		String output = "";
			
		//Caption:
		output = "TypeTable: " + prog + "\n"; //file name here!
		for (TypeClassMold t : types.values())
		{
			output = output + t.toString() + "\n";
		}
		
		return output;
	}
}
