package IC;

import IC.Parser.LexicalError;
import IC.Parser.SyntaxError;
import IC.Semantics.SemanticError;

public class Utils {
	
	/**
	 * Handles the lexical error exception
	 * @param e The exception object
	 */
	protected static void handleLexicalException(LexicalError e) {
		switch (e.type){
			case LexicalError.ILLEGAL_CHAR:
			case LexicalError.LEXER_MESSAGE:
			case LexicalError.UNFINISHED_COMMENT:
			case LexicalError.UNFINISHED_STRING:
			case LexicalError.ILLEGAL_QUOTE:
			case LexicalError.ILLEGAL_ID:
			case LexicalError.SEQUENTIAL_ZEROS:
			case LexicalError.LARGE_NUMBER:
			case LexicalError.GENERAL_ERROR:
				System.out.println("Lexical error was detected. Lexer message:");
				System.out.println(e.toString());
				System.out.println("Program will now exit.");
				System.exit(1);
				break;
			
			default:
				System.out.println("BUG: Unhandled error");
				System.out.println("Program will now exit.");
				System.exit(1);
		}
	}
	
	/**
	 * Handles syntax error exception
	 * @param e The exception object
	 */
	protected static void handleSyntaxError(SyntaxError e) {
		System.out.println("Syntax error was detected in IC file. Parser message:");
		System.out.println(e.toString());
		System.out.println("Program will now exit.");
		System.exit(1);
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
	 * Handles general exceptions
	 * @param e The exception object
	 */
	protected static void handleGenerlException(Exception e) {
		System.out.println("Catching general exception.");
		throw new RuntimeException("IO Error: " + e.toString());
	}
}
