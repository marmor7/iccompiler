package IC.LIR;

public class Register {
	static private int RegCounter = 0;
	
	/**
	 * Gets the next free reg
	 * @return
	 */
	static String getFreeReg()
	{	
		RegCounter++;
		return "R" + RegCounter;
	}
	
	/**
	 * Reset registers count
	 */
	static void reset()
	{
		RegCounter = 0;
	}
}
