package IC.LIR;


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
		Inc,
		Dec,
		Neg;
	}
}
