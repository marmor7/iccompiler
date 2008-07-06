package IC.Parser;


public class SyntaxError extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	int line;
	Token token;

	/**
	 * @param Token tok - The Token on which a SyntaxError had occoured.
	 * SyntaxError class constructor.
	 */
	public SyntaxError(Token tok) {
		this.line = tok.getLine();
		this.token = tok;
	}
	
	
	/**
	 * @override
	 * This function returns the message that this exception object contains.
	 */
	public String toString(){
		
		return ("Syntax error found on line: " + this.line + 
				", near token " +  this.token.toString());
		
	}

}


