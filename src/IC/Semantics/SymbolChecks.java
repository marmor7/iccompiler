package IC.Semantics;

import java.util.Iterator;

import IC.Semantics.SymbolClass.SymbolKind;

public class SymbolChecks {
	
	private static String mainFound;
	
	public static void globalRedefineCheck(SymbolTable root) throws SemanticError
	{
		mainFound = "";
		
		redefineCheck(root);
		
		//Check that a single main appears in the program
		if (mainFound.length() == 0)
			throw new SemanticError("program has no main method");
	}

	private static void redefineCheck(SymbolTable table) throws SemanticError
	{
		//Check for main
		SymbolClass main = table.getMethod("main");
		if (main != null)
		{
			if (mainFound.length() != 0)
			{
				throw new SemanticError("multiple main methods found, in " + 
						mainFound + " and " + table.getId());
			}
			else //Check main's signature and change mainFound
			{ 
				if ( (main.getKind() == SymbolKind.STATIC_METHOD) &&
						( ((MethodSigType)main.getType()).isLegalMain()) )
				{
					//Check parameter name
					SymbolTable symtab = table.getChild("main");
					if ((symtab != null) && (symtab.getParameter("args") != null))
						mainFound = table.getId();
					else 
						throw new SemanticError("main method found in " + 
								table.getId() + " has a bad signature");
				}
				else
					throw new SemanticError("main method found in " + 
							table.getId() + " has a bad signature");
			}
		}
		
		//Recursively check children tables
		Iterator<String> it = table.getChildrenIterator();
		while (it.hasNext())
		{
			redefineCheck(table.getChild(it.next()));
		}
	}
	
}
