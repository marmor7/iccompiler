package IC.LIR;

import IC.LIR.Op.OpType;

public class ControlTransferInstruction extends Instruction{

	
	public ControlTransferInstruction(Op op1P, 
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
	
	public String toString(){
		String str  = getInstructionType().toString() + " ";
			str += getOp1();
			
			if (getOptComment() != null)
				str += "\t\t" + getOptComment();
			return str;
	}
}
