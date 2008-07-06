package IC.Semantics;

import IC.AST.StaticCall;
import IC.Semantics.SymbolTable.ScopeKind;

public class Utils {
	
	protected static String getDimStr(int n){
		
		Integer a = new Integer(n);
		return a.toString();
	}
	
	/**
	 * Returns the key for our table hash map.
	 * @param id Type id name
	 * @param dim Type's dimension
	 * @return The key
	 */
	protected static String hashKey(String id, int dim)
	{
		return id + "$" + dim;
	}
	
	/**
	 * Returns the key for our table hash map for methods only
	 * 
	 * @param methodName The name of the method
	 * @param className The encapsulating class for the method
	 * @return The key
	 */
	protected static String hashKeyForMethods(String methodName,String className)
	{
		String key = "";
		
		//Concatenate class name and method name
		key = "class$" + className + "$method$" + methodName;
		return key;
	}
	
	/**
	 * Get's <code> n </code> times the string "[]".
	 * @param n The number of brackets to concatenate.
	 * @return <code> n </code> times [].
	 */
	protected static String getDimBrackets(int n){
		String output = "";
		for (int i = 0; i < n; i++){
			output += "[]";
		}
		return output;
	}
	
	/**
	 * Handles SemanticError exception when occours.
	 * 
	 * @param e The SemanticException object.
	 */
	protected static void handleSemanticError(SemanticError e)
	{
		System.out.println(e.toString());
		System.out.println("Program will now exit.");
		System.exit(1);
	}
	
	/**
	 * Gets the enclosing scope for the static call
	 * @param sc The static call object
	 * @return <code>SymbolTable<code> of the enclosing scope
	 */
	protected static SymbolTable GetEncloseingScopeForClass(StaticCall sc)
	{	
		 Utils.hashKeyForMethods(sc.getName(), sc.getClassName());
		
		SymbolTable st = sc.enclosingScope();
		boolean flag = true;
		
		while ((st.getChild(sc.getClassName()) == null) && (st.getKind() != ScopeKind.GLOBAL) && flag)
			{
			 st = st.getParent();
			 if (st.getKind() == ScopeKind.GLOBAL)
				 flag = !flag;
			}
		
		return st;
		
	}
}
