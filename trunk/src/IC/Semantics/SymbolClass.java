package IC.Semantics;

public class SymbolClass {

	private String id;
	private SymbolKind kind;
	private TypeClassMold type;
	
	/**
	 * Constructor
	 * @param id The SymClass id
	 * @param kind The symbol's enum kind
	 * @param type The class type
	 */
	public SymbolClass(String id, SymbolKind kind, TypeClassMold type) {
		super();
		this.id = id;
		this.kind = kind;
		this.type = type;
	}
	
	public String getId(){
		return this.id;
	}
	
	public SymbolKind getKind(){
		return this.kind;
	}
	
	public TypeClassMold getType(){
		return this.type;
	}
	
	/**
	 * The <code>toString()</code> implementation
	 */
	public String toString(){
		String mytype = "";
		switch (kind) {
		case CLASS :  
			mytype = "Class: " + id; 
		break;
		case VIRTUAL_METHOD :
			mytype = "Virtual Method: ";
			mytype += id + " " + ((MethodSigType)type).sigString();
		break;
		case STATIC_METHOD : 
			mytype = "Static method: ";
			mytype += id + " " + ((MethodSigType)type).sigString();
		break;
		case VAR :    
			mytype = "Local variable: ";
			mytype += ((TypeClass)type).getIdAndDim() + " " + id; 
		break;
		case FIELD :  
			mytype = "Field: ";
			mytype += ((TypeClass)type).getIdAndDim() + " " + id; 
		break;
		case PARAMETER :    
			mytype = "Parameter: ";
			mytype += ((TypeClass)type).getIdAndDim() + " " + id; 
		break;
		case LOCAL_VAR :    
			mytype = "Local variable: ";
			mytype += ((TypeClass)type).getIdAndDim() + " " + id; 
		break;
		default :
			Utils.handleSemanticError(new SemanticError("unknown kind"));
		}
		
		return mytype;
	}
	
	/**
	 * 
	 * SymbolKind enum
	 *
	 */
	public enum SymbolKind {
		CLASS,
		VIRTUAL_METHOD,
		STATIC_METHOD,
		VAR,
		FIELD,
		PARAMETER,
		LOCAL_VAR,
		OTHER;
	}
}
