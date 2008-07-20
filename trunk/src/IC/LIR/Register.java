package IC.LIR;

public class Register {
	static private int RegCounter = 0;
	static String getFreeReg()
	{	
		RegCounter++;
		return "R" + RegCounter;
	}
	
	static void reset()
	{
		RegCounter = 0;
	}
}
