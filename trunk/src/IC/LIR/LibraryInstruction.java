package IC.LIR;

public class LibraryInstruction extends Instruction {
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public LibraryInstruction(Op op1P, Op op2P, 
			LibraryInstructionType type) 
	{
		super(op1P, op2P, type);
	}
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param type Instruction's Type
	 */
	public LibraryInstruction(Op op1P, 
			LibraryInstructionType type) 
	{
		super(op1P, type);
	}
	
	/**
	* Type's <code>enum</code> 
	*
	*/
	public enum LibraryInstructionType implements InstructionType
	{
		AllocateArray("AllocateArray"),
		Exit("exit(0)");
		
		private String print;

		private LibraryInstructionType(String print) {
			this.print = print;
		}
		
		public String toString(){
			return print;
		}
	}
	
	public String toString(){
		String output = "Library __" + getInstructionType().toString();
		if (getOp1() != null)
			output += ", " + getOp1();
		if (getOp2() != null)
			output += ", " + getOp2();
		if (getOp3() != null)
			output += ", " + getOp3();
		return output;

	}
}
