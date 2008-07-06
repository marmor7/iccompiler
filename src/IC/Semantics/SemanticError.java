package IC.Semantics;

public class SemanticError extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int line;
	private String message;
	
	/**
	 * 
	 * @param line The line which the error has occurred.
	 * @param msg The error that occurred.
	 */
	public SemanticError(int line, String msg){
		this.line = line;
		this.message = msg;		
	}
	
	/**
	 * Error with no line
	 * @overload
	 * @param msg
	 */
	public SemanticError(String msg){
		this.line = -1;
		this.message = msg;		
	}
	
	/**
	 * @override
	 * This function returns the message that this exception object contains.
	 */
	public String toString(){
		
		if (this.line > -1)
		{
			return ("Semantic error found on line: " + this.line + 
					", " +  this.message);			
		}
		
		else
		{
			return ("Semantic error found: " + this.message);
		}	
	}
}

