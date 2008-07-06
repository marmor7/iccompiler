package IC.LIR;

import java.util.ArrayList;

public class StringLiteral {

	 static ArrayList<String> alliterals = new ArrayList<String>();
	
	 static void addtoliterals(String toAdd)
	{
		 alliterals.add(toAdd);
	}
	 
	 static boolean isIn(String toCheck)
	 {
		 return (alliterals.indexOf(toCheck) != -1);
	 }
	 
	 static String getindexoflit(String toCheck)
	 {
		 return "str"+alliterals.indexOf(toCheck);
	 }
	 static String getstringbyindex(int index)
	 {
		 return alliterals.get(index);
	 }
	
	 static public String prettyPrint()
	{
		String toRet = "";
		for (int i = 0 ; i < alliterals.size();i++)
			toRet =toRet + "str"+i+": "+alliterals.get(i)+"\n";
		//Remove last \n:
		toRet = toRet.substring(0, toRet.length() - 1);
		return toRet;
	}
}
