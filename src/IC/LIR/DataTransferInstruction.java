package IC.LIR;

public class DataTransferInstruction extends Instruction{
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public DataTransferInstruction(Op op1P, Op op2P, 
								   DataTransferInstructionType type)
	{
		super(op1P, op2P, type);
	}
	
	
	public DataTransferInstruction(Op op1P, Op op2P, Op op3P,  
			   DataTransferInstructionType type)
{
		super(op1P, op2P,op3P, type);
}

	/**
	 * Type's <code>enum</code> 
	 *
	 */
	public enum DataTransferInstructionType implements InstructionType
	{
		Move,
		MoveArray,
		MoveField,
		ArrayLength;
	}
}
