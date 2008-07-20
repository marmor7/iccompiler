package IC.LIR;

import java.util.ArrayList;

public class StringLiteral {

	 static ArrayList<String> alliterals = new ArrayList<String>();
	
	 /**
	  * Adds a string
	  * @param toAdd
	  */
	 static void addtoliterals(String toAdd)
	{
		 alliterals.add(toAdd);
	}
	 
	 /**
	  * Is the string in the list
	  * @param toCheck
	  * @return
	  */
	 static boolean isIn(String toCheck)
	 {
		 return (alliterals.indexOf(toCheck) != -1);
	 }
	 
	 /**
	  * Gets the index of the literal
	  * @param toCheck
	  * @return
	  */
	 static String getIndexOfLit(String toCheck)
	 {
		 return "str"+alliterals.indexOf(toCheck);
	 }
	 
	 /**
	  * Gets a string literal by its index
	  * @param index
	  * @return
	  */
	 static String getStringByIndex(int index)
	 {
		 return alliterals.get(index);
	 }
	
	 /**
	  * PrettyPrinter for string literals
	  * @return
	  */
	 static public String prettyPrint()
	{
		String toRet = "";
		for (int i = 0 ; i < alliterals.size();i++)
			toRet =toRet + "str"+i+": "+alliterals.get(i)+"\n";
		//Remove last \n:
		if (toRet.length() > 0)
			toRet = toRet.substring(0, toRet.length() - 1);	
		return toRet;
	}
}
