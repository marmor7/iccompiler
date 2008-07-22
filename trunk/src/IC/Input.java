package IC;

/**
 * This class is designed to handle the input correctly and make debugging 
 * on input easier.
 *
 */
public class Input {
	
	private static final int MAX_NUM_OF_ARGS = 6;

	private String[] input;	
	
	public String fileName = "";
	public String libPath = "";
	
	public boolean libraryFileSpecified;
	public boolean ICFileSpecified;
	public boolean printAST;
	public boolean dumpSymTable;
	public boolean printLir;
	public boolean optLir;
	
	/**
	 * 
	 * @param args What was passed in command line.
	 * Invokes handleInput, which contains all the handling logic.
	 */
	public Input(String[] args)
	{
		this.input = args;
		handleInput(this.input);
	}
	
	/**
	 * 
	 * @param input What was passed in command line.
	 * Handels the input.
	 */
    private void handleInput(String[] input){
    	String arg;
    	
    	//General parameters initialization.
    	this.libraryFileSpecified = false;
    	this.ICFileSpecified = false;
    	this.printAST = false;
    	this.dumpSymTable = false;
    	this.printLir = false;
    	this.optLir = false;
    	
    	if (input.length ==  0)
    	{
    		wrongInputExit("No IC file specified.\n" +
    				       "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    				       "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    	}
    	else if(input.length > MAX_NUM_OF_ARGS){
    		wrongInputExit("Too many arguments: "+ input.length + ".\n" +
    				       "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    				       "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    	}
    	//Number of arguments in command line is OK
    	else
    	{
    		for (int i = 0; i < input.length; i++)
        	{
    			arg = input[i];
    			if (arg.startsWith("-L"))
    			{
    				//Already parsed a libfile.
    				if (libraryFileSpecified == true)
    				{
    					wrongInputExit("Library file path alreay specified.\n" + 
    								   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    								   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    				}
    				libPath = arg.substring(2);
    				libraryFileSpecified = true;
    			}
    			else if (arg.equalsIgnoreCase("-print-ast")) {
    				//Flag duplication
    				if (printAST == true){
    					wrongInputExit("-print-ast flag alreay specified.\n" + 
    								   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    								   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    				}
    				printAST = true;    				    				
    			}
    			else if (arg.equalsIgnoreCase("-dump-symtab")){
    				//Flag duplication
    				if (dumpSymTable == true){
    					wrongInputExit("-dump-symtab flag already specified.\n" + 
    					 			   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    					 			   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    				}
    				dumpSymTable = true;
    			}
    			else if (arg.equalsIgnoreCase("-print-lir")){
    				//Flag duplication
    				if (printLir == true){
    					wrongInputExit("-print-lir flag already specified.\n" + 
    					 			   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    					 			   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    				}
    				printLir = true;
    				
    			}
    			else if (arg.equalsIgnoreCase("-opt-lir")){
    				//Flag duplication
    				if (optLir == true){
    					wrongInputExit("-opt-lir flag already specified.\n" + 
    					 			   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    					 			   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    				}
    				optLir = true;
    				
    			}
    			else {
    				//More then one file path or another free argument
    				if (ICFileSpecified == true){
    					wrongInputExit("Wrong argument or file name.\n" +
    					  			   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    					  			   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    				}
    				fileName = arg;
    				ICFileSpecified = true;
    			}    				  			
        	}    		
    	}
   }
    /** 
     * @param exitMessage The error message recieved from <code>inputHandle</code>.
     * This function prints what's wrong with input and exit.
     */
    private void wrongInputExit(String exitMessage){
    	System.out.println(exitMessage);
    	System.out.println("Program will now exit.");
    	
    	System.exit(1);
    }
    
    /**
     * For easy class debugging. 
     *
     */
    public void printInput(){
    	System.out.println("File name: " +				     fileName);
    	System.out.println("libic.sig: " + 				     libPath);
    	System.out.println("IC file specified flag: " +  	 ICFileSpecified);
    	System.out.println("Library file specified flag: " + libraryFileSpecified);
    	System.out.println("-print-ast flag: " + 		     printAST);
    	System.out.println("-dump-symtab flag: " + 	         dumpSymTable);
    	System.out.println("-print-lir flag: " + 			 printLir);
    	System.out.println("-opt-lir flag: " + 				 optLir);
    	System.out.println("");
    }
}
