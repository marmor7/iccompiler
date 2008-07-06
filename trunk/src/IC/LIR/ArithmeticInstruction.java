package IC.LIR;

import IC.LIR.Op.OpType;


public class ArithmeticInstruction extends Instruction{
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public ArithmeticInstruction(Op op1P, Op op2P, 
								 ArithmeticInstructionType type) 
	{
		super(op1P, op2P, type);
	}
	
	public ArithmeticInstruction(Op op1P,
			 ArithmeticInstructionType type) 
{
		super(op1P,  type);
}

	/**
	 * Type's <code>enum</code> 
	 *
	 */
	public enum ArithmeticInstructionType implements InstructionType
	{
		Add,
		Sub,
		Mul,
		Div,
		Mod,
		Neg;
	}
	
	public String toString(){
		String str  = getInstructionType().toString() + " ";
		str += getOp1();
		if (getOp2() != null)
			str += "," + getOp2();
		
		if (getOptComment() != null)
			str += "\t\t" + getOptComment();
		return str;
	}
	
}
