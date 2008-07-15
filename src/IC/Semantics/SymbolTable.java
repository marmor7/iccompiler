package IC.Semantics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import IC.Semantics.SymbolClass.SymbolKind;

public class SymbolTable {

	/** Method Map id->entry **/

	private static int counter = 0; 
	
	private Map<String,SymbolClass>  classEntries;
	private Map<String,SymbolClass>  methodEntries;
	private Map<String,SymbolClass>  varEntries;
	private Map<String,SymbolClass>  fieldEntries;
	private String                   id;
	private int                   	numid;
	private ScopeKind                kind;
	private SymbolTable              parent;
	private Map<String, SymbolTable> children;

	/**
	 * Constructor	
	 * @param id The SymbolTable name 
	 * @param kind The SkopeKind Enum
	 * @param parent The SymbolTable parent if has any
	 */
	public SymbolTable(String id, ScopeKind kind, SymbolTable parent){
		this.id = id;
		this.parent = parent;
		this.kind = kind;
		if (kind == ScopeKind.GLOBAL)
			classEntries = new HashMap<String, SymbolClass>();
		methodEntries = new HashMap<String, SymbolClass>();
		varEntries = new HashMap<String, SymbolClass>();
		fieldEntries = new HashMap<String, SymbolClass>();
		children = new HashMap<String, SymbolTable>();
		numid = counter++;
	}
	
	public SymbolClass getClass(String id){
		if (kind != ScopeKind.GLOBAL)
			Utils.handleSemanticError(new SemanticError("Scope table " + this.id + 
					" does not contain class " + id + ". Not a program scope."));

		return classEntries.get(id);
	}

	public SymbolClass insertClass(String id, TypeClassMold t) {
		if (kind != ScopeKind.GLOBAL)
			Utils.handleSemanticError(new SemanticError( "Class declaration in non global scope: " +id));
			
		return classEntries.put(id, new SymbolClass(id, SymbolKind.CLASS, t));		
	}
	
	public SymbolClass getMethod(String id) {
		return methodEntries.get(id);
	}

	public SymbolClass insertMethod(String id, TypeClassMold t) {
		
		if (t==null)
			Utils.handleSemanticError(new SemanticError("Error trying to insert a null method"));

		return methodEntries.put(id, new SymbolClass(id, 
				(((MethodSigType)t).getStatic()? SymbolKind.STATIC_METHOD : SymbolKind.VIRTUAL_METHOD), t));
	}
	
	public SymbolClass getVariable(String id) {
		return varEntries.get(id);
	}

	public SymbolClass insertVariable(String id, TypeClassMold t) {
		return varEntries.put(id, new SymbolClass(id, SymbolKind.VAR, t));
	}
	
	public SymbolClass searchVariable(String id) {
		SymbolClass s = varEntries.get(id);
		
		if ((s == null) && (this.getParent() != null))
			s = this.getParent().searchVariable(id);
		
		return s;
	}
	
	public SymbolTable searchVariableReturnScope(String id) {
		SymbolClass s = varEntries.get(id);
		SymbolTable s2 = this;
		if ((s == null) && (this.getParent() != null))
			 s2 =	this.getParent().searchVariableReturnScope(id);
		
		return s2;
	}
	
	public SymbolClass searchMethod(String id) {
		SymbolClass s = methodEntries.get(id);
		
		if ((s == null) && (this.getParent() != null))
			s = this.getParent().searchMethod(id);
		
		return s;
	}
	
	public SymbolClass getField(String id) {
		return varEntries.get(id);
	}
	
	public SymbolClass searchField(String id) {
		SymbolClass s = varEntries.get(id);
		if ((s == null) && (this.getParent() != null))
			s = this.getParent().searchField(id);
		return s; 
	}
	
	public SymbolClass insertParameter(String id, TypeClassMold t) {
		return varEntries.put(id, new SymbolClass(id, SymbolKind.PARAMETER, t));
	}
	
	public SymbolClass getParameter(String id) {
		return varEntries.get(id);
	}

	public SymbolClass insertField(String id, TypeClassMold t) {
		return varEntries.put(id, new SymbolClass(id, SymbolKind.FIELD, t));
	}
	
	public String getId(){
		if (kind != ScopeKind.BLOCK)
			return this.id;
		else
			return "statement block in " + parent.getId();
	}
	
	public ScopeKind getKind(){
		return this.kind;
	}
	
	public String getLocation(){
		if (kind != ScopeKind.BLOCK)
			return this.id;
		else
			return "in " + parent.getLocation();
	}
	
	public SymbolTable getParent(){
		return this.parent;
	}
	
	public SymbolTable addChild(String id, SymbolTable child){
		return children.put(id, child);
	}
	
	public SymbolTable getChild(String id){
		return children.get(id);
	}
	
	public Iterator<String> getClassIterator()
	{
		return this.classEntries.keySet().iterator();
	}

	public Iterator<String> getFieldIterator()
	{
		return this.fieldEntries.keySet().iterator();
	}
	
	public Iterator<String> getMethodIterator()
	{
		return this.methodEntries.keySet().iterator();
	}
	
	public Iterator<String> getVarIterator()
	{
		return this.varEntries.keySet().iterator();
	}
	
	public Iterator<String> getChildrenIterator()
	{
		return this.children.keySet().iterator();
	}
	
	/**
	 * The <code>toString()</code> implementation
	 */
	public String toString(){		
		String output = "";
		
		switch (kind) {
		case GLOBAL : 
			output = "Global Symbol Table: " + id + "\n";
			for (SymbolClass a : classEntries.values())
				output += "\t" + a + "\n";
			break;
		case CLASS  : 
			output = "Class Symbol Table: " + id + "\n";
			for (SymbolClass a : fieldEntries.values()) {
				output += "\t" + a + "\n";
			}
			for (SymbolClass a : methodEntries.values()) {
				output += "\t" + a + "\n";
			}
			break;
		case METHOD : 
			output = "Method Symbol Table: " + id + "\n";

			for (SymbolClass a : varEntries.values()) {
				if (a.getKind() == SymbolKind.PARAMETER) output += "\t" + a + "\n";
			}
			for (SymbolClass a : varEntries.values()) {
				if (a.getKind() == SymbolKind.VAR) output += "\t" + a + "\n";
			}
			break;
		case BLOCK  : 
			output = "Statement Block Symbol Table ( located " + getLocation() + " )\n";
			break;
		default     : 
				Utils.handleSemanticError(new SemanticError("unknown scope"));

		}

		if (!children.isEmpty())
		{
			output += "Children tables: ";
			for (SymbolTable a : children.values()) {
				output += a.getId() + ", ";
			}
			if (children.size() > 0)
				output = output.substring(0, output.length() - 2);
			output += "\n\n";
			for (SymbolTable a : children.values()) {
				output += a;
			}
		}
		else
			output += "\n";
		
		return output;
	}
	
	public enum ScopeKind {
		GLOBAL,
		CLASS,
		METHOD,
		BLOCK;
	}

	public int getNumid(String var) {
		return searchVariableReturnScope(var).getNumid();
		
	}

	private int getNumid() {
		return numid;
	}
}


