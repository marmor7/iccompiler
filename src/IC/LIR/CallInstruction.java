package IC.LIR;

public class CallInstruction extends Instruction{
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public CallInstruction(Op op1P, Op op2P, 
			CallInstructionType type)
	{
		super(op1P, op2P, type);
	}
	
	
	public CallInstruction(Op op1P, Op op2P, Op op3P,  
			CallInstructionType type)
{
		super(op1P, op2P,op3P, type);
}

	/**
	 * Type's <code>enum</code> 
	 *
	 */
	public enum CallInstructionType implements InstructionType
	{
		StaticCall
	}
}
