package IC.LIR;

public class LogicalInstruction extends Instruction {
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public LogicalInstruction(Op op1P, Op op2P, 
							  LogicalInstructionType type) 
	{
		super(op1P, op2P, type);
	}
	
	/**
	* Type's <code>enum</code> 
	*
	*/
	public enum LogicalInstructionType implements InstructionType
	{
		Not,
		And,
		Or,
		Xor,
		Compare,
		ArrayLength
	}
}
