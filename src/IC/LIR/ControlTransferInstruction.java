package IC.LIR;

public class ControlTransferInstruction extends Instruction{

	
	public ControlTransferInstruction(Op op1P, 
			InstructionType type) 
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
	
	public String toString(){
		String str  = getInstructionType().toString() + " ";
			str += getOp1();
			
			if (getOptComment() != null)
				str += "\t\t" + getOptComment();
			return str;
	}
}
