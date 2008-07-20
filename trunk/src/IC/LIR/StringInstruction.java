package IC.LIR;

public class StringInstruction extends Instruction{
	
	private String line;
	
	/**
	* Class constructor
	* @param comm The string that represents the comment
	*/
	public StringInstruction(String line)
	{
		super(DispatchTableType.DispatchTable);
		this.line = line;
	}
	
	/**
	 * Constructor
	 */
	public StringInstruction()
	{
		super(DispatchTableType.EmptyLine);
		this.line = "";
	}
	
	/**
	 * @return The DV line and list
	 */
	public String getLine()
	{
		return this.line;
	}
	
	/**
	* Type's <code>enum</code> 
	*
	*/
	public enum DispatchTableType implements InstructionType
	{
		DispatchTable,
		EmptyLine
	}
	
	/**
	 * ToString()
	 */
	public String toString()
	{
		return this.line; 
	}
	
}
