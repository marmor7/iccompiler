package IC.LIR;


public class Jump {
	static private int JCounter = 0;
	
	/**
	 * Gets the next jump counter value
	 * @return
	 */
	static String getNextJumpCounter()
	{	
		JCounter++;
		return "_l" + JCounter;
	}
}
