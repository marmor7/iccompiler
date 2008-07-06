package IC.Parser;

import java.util.ArrayList;

/**
 * This class extends <code>java_cup.runtime.Symbol </code> and repesent a Token 
 * that is being created in Lexical analysis phase.
 *
 */
public class Token extends java_cup.runtime.Symbol {
	public int line;
	public int id;
	private static ArrayList<Integer> valueArrayList = new ArrayList<Integer>();
	private int valueflag = 0;
	
 	/**
 	 * This is a list which we use to know if the token we found has a value.
 	 */
	private static void setValueList() 
	{
	 valueArrayList.add(IC.Parser.sym.INTEGER); //integer
	 valueArrayList.add(IC.Parser.sym.ID); //id
	 valueArrayList.add(IC.Parser.sym.QUOTE); // quote
	 valueArrayList.add(IC.Parser.sym.CLASS_ID); //class id
	 
	}

	/**
	 * Constructor.
	 * @param id_param From <code> sym.java </code>
	 * @param value_param If has any
	 * @param line_param Line found
	 */
    public Token(int id_param, Object value_param, int line_param) {
        super(id_param, null); //first calls the base constructor
        this.id = id_param; //putting the data in our fields 
        this.line = line_param + 1;
        this.left = this.line;
        value = value_param; //we use the base class value variable
        if (valueflag==0) //only want to set up the valuearraylist once
        	{
        	setValueList();
        	valueflag = 1;
        	}
    }
    
    /**
     * Getter
     * @return <code> this.id; </code>
     */
    public int getId()
    {
    	return this.id;
    }
    
    /**
     * Getter
     * @return <code> this.value; </code>
     */
    public Object getValue()
    {
    	return value;
    }
    
    /**
     * Getter
     * @return <code> this.line; </code>
     */
    public int getLine()
    {
    	return this.line;
    }
    
    /**
     * Prints the token line number & ID or some text
     */
    public void printToken()
    {
    	 
    	if (valueArrayList.indexOf(this.id) != -1)
    		System.out.println(this.line + ": " + printTokenHelper(this.id) + " (" + this.value + ")");
    	else
    		System.out.println(this.line + ": " +  printTokenHelper(this.id));
    }
    
    /**
     * Implements a toString() function for this class.
     * @override toString()
     */
    public String toString(){
    	if (valueArrayList.indexOf(this.id) != -1)
    		return(printTokenHelper(this.id) + "(" + this.value + ")");
    	else
    		return(printTokenHelper(this.id));
    }
    
    /**
     * Gets a integer corresponding with an id , and returns the id's name.
     * @param tokenID - The Token's ID.
     * @return The corresponding string.
     */
    private String printTokenHelper(int tokenID )
    {
    		switch (tokenID) {
    		case IC.Parser.sym.EOF: 		return "EOF";
    		case IC.Parser.sym.LP: 		return "LP";
    		case IC.Parser.sym.RP: 		return "RP";
    		case IC.Parser.sym.LCBR: 		return "LCBR";
    		case IC.Parser.sym.RCBR: 		return "RCBR";
    		case IC.Parser.sym.COMMA: 	return "COMMA";
    		case IC.Parser.sym.LB: 		return "LB";
    		case IC.Parser.sym.RB: 		return "RB"; 
    		case IC.Parser.sym.GT: 		return "GT";
    		case IC.Parser.sym.LT: 		return "LT";
    		case IC.Parser.sym.LTE: 		return "LTE";
    		case IC.Parser.sym.GTE: 		return "GTE";
    		case IC.Parser.sym.FALSE: 	return "FALSE";
    		case IC.Parser.sym.TRUE: 		return "TRUE";
    		case IC.Parser.sym.INT: 		return "INT"; 
    		case IC.Parser.sym.INTEGER: 	return "INTEGER";
    		case IC.Parser.sym.MINUS: 	return "MINUS";
    		case IC.Parser.sym.MULTIPLY: 	return "MULTIPLY";
    		case IC.Parser.sym.PLUS: 		return "PLUS";
    		case IC.Parser.sym.DIVIDE: 	return "DIVIDE";
    		case IC.Parser.sym.CLASS: 	return "CLASS";
    		case IC.Parser.sym.CLASS_ID: 	return "CLASS_ID";
    		case IC.Parser.sym.ASSIGN: 	return "ASSIGN";
    		case IC.Parser.sym.BOOLEAN: 	return "BOOLEAN";
    		case IC.Parser.sym.BREAK: 	return "BREAK";
    		case IC.Parser.sym.CONTINUE: 	return "CONTINUE";
    		case IC.Parser.sym.DOT: 		return "DOT";
    		case IC.Parser.sym.EXTENDS: 	return "EXTENDS";
    		case IC.Parser.sym.IF: 		return "IF";
    		case IC.Parser.sym.ELSE: 		return "ELSE";
    		case IC.Parser.sym.ID: 		return "ID";
    		case IC.Parser.sym.EQUAL: 	return "EQUAL"; 
    		case IC.Parser.sym.NEQUAL:	return "NEQUAL";
    		case IC.Parser.sym.LOR: 		return "LOR";
    		case IC.Parser.sym.LAND: 		return "LAND";
    		case IC.Parser.sym.LNEG: 		return "LNEG";
    		case IC.Parser.sym.WHILE: 	return "WHILE";
    		case IC.Parser.sym.LENGTH: 	return "LENGTH";
    		case IC.Parser.sym.NEW: 		return "NEW";
    		case IC.Parser.sym.MOD: 		return "MOD";
    		case IC.Parser.sym.NULL: 		return "NULL";
    		case IC.Parser.sym.RETURN: 	return "RETURN";
    		case IC.Parser.sym.SEMI: 		return "SEMI";
    		case IC.Parser.sym.STATIC: 	return "STATIC";
    		case IC.Parser.sym.STRING: 	return "STRING";
    		case IC.Parser.sym.QUOTE: 	return "QUOTE";
    		case IC.Parser.sym.THIS: 		return "THIS";
    		case IC.Parser.sym.VOID: 		return "VOID";
    		default: return "ERROR! Unreported number.";
  		}
    } 
}
