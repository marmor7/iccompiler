package IC.LIR;

public class Jump {
	static private int JCounter = 0;
	static String getnextjumpcounter()
	{	
		JCounter++;
		return "_l" + JCounter;
	}
}
