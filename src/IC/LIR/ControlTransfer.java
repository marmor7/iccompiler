package IC.LIR;

public class ControlTransfer extends Instruction{

	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public ControlTransfer(Op op1P, Op op2P, 
						   ControlTransferInstructionType type) // TBD - why is this two operators - i put the same one twice
	{
		super(op1P, op2P, type);
	}
	
	/**
	 * Constructor
	 * @param op1P
	 * @param type
	 */
	public ControlTransfer(Op op1P, 
			ControlTransferInstructionType type) // TBD - why is this two operators - i put the same one twice
	{
	super(op1P,  type);
	}
	
	
	/**
	 * Type's <code>enum</code> 
	 *
	 */
	public enum ControlTransferInstructionType implements InstructionType
	{
		Return,
		Jump,
		JumpTrue,
		JumpFalse,
		JumpG ,
		JumpGE,
		JumpL,
		JumpLE
	}
}
