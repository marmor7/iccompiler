package IC.LIR;

public class Utils {
	
	/**
	 * Mangeled for for hashing in the <code>objects</code> list
	 * @param variableName
	 * @param scopeName
	 * @param className
	 * @return
	 */
	public static String getObjectsMapName(String variableName, String scopeName, String className)
	{
		return variableName + "_" + scopeName + "_" + className; 
	}

}
