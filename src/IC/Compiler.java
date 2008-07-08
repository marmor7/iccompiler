package IC; 

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.LIR.Instruction;
import IC.LIR.LirVisitor;
import IC.LibraryParser.LibraryParser;
import IC.Parser.Lexer;
import IC.Parser.LexicalError;
import IC.Parser.Parser;
import IC.Parser.SyntaxError;
import IC.Semantics.SemanticError;
import IC.Semantics.SymbolChecks;
import IC.Semantics.SymbolTable;
import IC.Semantics.SymbolVisitor;
import IC.Semantics.TypeCheckingVisitor;
import IC.Semantics.TypeTable;
import IC.Semantics.TypeVisitor;

public class Compiler
{	
	/**
	 * This function invokes INPUT class to handle user input and dealing 
	 * files as demanded for each PA.
	 * 
	 * @param args What was passed as an arguments from user.
	 * 
	 */
    public static void main(String[] args)
    {    	
    	Input input = new Input(args);
    	
    	//DEBUG:
    	input.printInput();
    	
    	//User entered an IC file path
    	if (input.ICFileSpecified){
    		ICClass libroot = null;
    		if (input.libraryFileSpecified) 
            {
            	libroot = handleLibFile(input.libPath);
            }
    		handleICFile(input.fileName, input.printAST, input.dumpSymTable, input.printLir, input.optLir, libroot);    		
    	}
    	//Bad input format
    	else
    	{
    		System.out.println("No IC file specified.\n" +
    						   "Usage: IC.Compiler <file.ic> [ -L<library-file> ] [ -print-ast ] " +
    						   "[ -dump-symtab] [ -print-lir] [ -opt-lir]");
    		System.exit(1);
    	}
    }
    
    /**
     * This function is in charge of lexical analysis and syntactic parsing of the IC file.
     * 
     * @param fileName The IC file to be parsed.
     * @param printAST Boolean flag, indicating whether to invoke prettyPrint. 
     */
	private static void handleICFile(String fileName, boolean printAST, boolean dumpSymTable, boolean printLir, boolean optLir, ICClass libroot) {
		try 
    	{
            FileReader txtFile = new FileReader(fileName);
            Lexer scanner = new Lexer(txtFile);
            Parser parser = new Parser(scanner);
            Program root = (Program) parser.parse().value;
            
            if (libroot != null){
            	// Add the lib root as a class in root:
            	if (!libroot.getName().equals("Library")){
            		throw new SemanticError("Library file has wrong class name");
            	}
            	
            	List<ICClass> classList = root.getClasses();
            	
            	classList.add(0, libroot);
            }
            
            System.out.println("Parsed " + fileName + " successfully!");
            System.out.println();
        	String croppedFileName = fileName.substring(fileName.lastIndexOf(File.separatorChar)+1);
            //Type building visitor
            TypeVisitor tvisitor = new TypeVisitor(root, croppedFileName);
			TypeTable typeTable = (TypeTable) tvisitor.visit(root);
            // Symbol table building visitor
            SymbolVisitor svisitor = new SymbolVisitor(typeTable, croppedFileName);
            SymbolTable symbolTable = (SymbolTable) svisitor.visit(root);
            
            // Type checking
            TypeCheckingVisitor tcVisitor = new TypeCheckingVisitor(typeTable);
            tcVisitor.visit(root);            
            //Symbol checking
            SymbolChecks.globalRedefineCheck(symbolTable);
            if (printAST){
            	PrettyPrinter print = new PrettyPrinter(fileName);
                System.out.println(print.visit(root));
                System.out.println();
            }
            
            if (dumpSymTable)
            {
            	System.out.println(symbolTable);
            	System.out.println(typeTable);
            }
            
            System.out.println("SEMANTIC CHECKS DONE"); //TMP
            
            //LIR Translation
            if (libroot != null){
            	// remove the Library from the classes list
            	List<ICClass> classList = root.getClasses();
            	
            	classList.remove(0);
            }
            
            LirVisitor lir = new LirVisitor(croppedFileName,typeTable);
            System.out.println("Lir constructor");
            ArrayList<Instruction> lirList = (ArrayList<Instruction>) lir.visit(root);
            
            System.out.println("LIR TRANSLATION DONE"); //TMP
            
            if (printLir)
            {
            	Iterator<Instruction> it = lirList.iterator();
            	while (it.hasNext())
            		System.out.println(it.next());
            }
            
            
    	}
    	
    	catch (LexicalError e)
    	{
    		Utils.handleLexicalException(e); 
    	} 
    	catch (SyntaxError e) 
       	{
    		Utils.handleSyntaxError(e);
       	}
    	catch (SemanticError e)
    	{
    		Utils.handleSemanticError(e);
    	}
        catch (Exception e)
	    {	
	        Utils.handleGenerlException(e);
	    }
	}
	
	/**
	 * This function is in charge of lexical analysis and syntactic parsing of the Library.cup file.
	 *  
	 * @param libPath The Library.cup file to be parsed (full path inc. file name).
	 * 
	 */
	private static ICClass handleLibFile(String libPath) {
    	ICClass libroot = null;
		try 
		{
			FileReader libicFile = new FileReader(libPath);
		    Lexer scanner = new Lexer(libicFile);
		    LibraryParser parser = new LibraryParser(scanner);
		    libroot = (ICClass) parser.parse().value;
		}
		catch (LexicalError e)
    	{
    		Utils.handleLexicalException(e); 
    	} 
    	catch (SyntaxError e) 
       	{
    		Utils.handleSyntaxError(e);
       	}
    	catch (SemanticError e)
    	{
    		Utils.handleSemanticError(e);
    	}      
		catch (Exception e)
		{
		    Utils.handleGenerlException(e);
		}
		return libroot;
	}

}
