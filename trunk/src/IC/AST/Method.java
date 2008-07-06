package IC.AST;

import java.util.List;

/**
 * Abstract base class for method AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Method extends ASTNode {

	protected Type type;

	protected String name;

	protected List<Formal> formals;

	protected List<Statement> statements;
	
	private Boolean staticState;
	

	/**
	 * Constructs a new method node. Used by subclasses.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 * @param statements
	 *            List of method's statements.
	 */
	protected Method(Type type, String name, List<Formal> formals,
			List<Statement> statements) {
		super(type.getLine());
		this.type = type;
		this.name = name;
		this.formals = formals;
		this.statements = statements;
		this.staticState = false;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public List<Formal> getFormals() {
		return formals;
	}

	public List<Statement> getStatements() {
		return statements;
	}

	/**
	 * Get the static state of the method.
	 * 
	 * @return <code> true </code> iff the method is declared static or is a library method.
	 */
	public boolean getStaticState() {
		return staticState;
	}

	/**
	 * Set the method static state;
	 * 
	 * @param staticState <code>Boolean</code> value
	 */
	public void setStaticState(boolean staticState) {
		this.staticState = staticState;
	}
	
	
}