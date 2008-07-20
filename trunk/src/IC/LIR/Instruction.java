package IC.LIR;

import IC.LIR.Op.OpType;

public abstract class Instruction {
	
	private Op op1;
	private Op op2;
	private Op op3;
	private InstructionType instructionType;
	private int opsCount = 0;
	private Comment optComment = null;
	
	/**
	 * Constructor. 
	 * Base class for all instructions
	 * @param op1P
	 * @param op2P
	 * @param op3P
	 * @param iType
	 */
	public Instruction(Op op1P, Op op2P,Op op3P ,InstructionType iType)
	{
		this.op1 = op1P;
		this.op2 = op2P;
		this.op3 = op3P;
		this.instructionType = iType;
		this.opsCount =3;
	}
	
	
	/**
	 * Binary op constructor
	 * @param op1P First op
	 * @param op2P Second op
	 * @param iType Instruction's type
	 */
	public Instruction(Op op1P, Op op2P, InstructionType iType)
	{
		this.op1 = op1P;
		this.op2 = op2P;
		this.op3 = null;
		this.instructionType = iType;
		this.opsCount = 2;
	}
	
	/**
	 * Gets the comment for current line (instruction)
	 * @return
	 */
	public Comment getOptComment() {
		return optComment;
	}

	/**
	 * Sets a comment for the instruction
	 * @param optComment
	 * @return
	 */
	public Instruction setOptComment(String optComment) {
		this.optComment = new Comment(optComment);
		return this;
	}

	/**
	 * Unary constructor 
	 * @param op1P First op
	 * @param iType Instruction's type
	 * @param comm Comment
	 */
	public Instruction(Op op1P, InstructionType iType)
	{
		this.op1 = op1P;
		this.op2 = null;
		this.instructionType = iType;
		this.opsCount = 1;
		this.op3 = null;
	}
	
	/**
	 * Non Op's constructor
	 * @param type The instruction's type
	 * @param comm Comment
	 */
	public Instruction(InstructionType type)
	{
		this.op1 = null;
		this.op2 = null;
		this.op3 = null;
		this.opsCount = 0;
	}
	
	/**
	 * All subclasses must implement me!
	 * @return The corresponding LIR code for the Instructions instance object
	 */
	public String toString(){
		String str  = getInstructionType().toString() + " ";
		if ((op1 != null)&&(op1.getOpType() != OpType.Disabled))
			str += op1;
		if ((op2 != null)&&(op2.getOpType() != OpType.Disabled))
			str += "," + op2;
		if ((op3 != null)&&(op3.getOpType() != OpType.Disabled))
			str += "," + op3;
		if (optComment != null)
			str += "\t\t" + optComment;
		return str;
	}
	
	/**
	 * Empty interface for derived classes
	 *
	 */
	public interface InstructionType{};
	
	/**
	 * @return The first op
	 */
	public Op getOp1()
	{
		return this.op1;
	}
	
	/**
	 * @return The second op
	 */
	public Op getOp2()
	{
		return this.op2;
	}
	
	/**
	 * @return The third op
	 */
	public Op getOp3()
	{
		return this.op3;
	}
	
	/**
	 * @return The instance instruction type
	 */
	public InstructionType getInstructionType()
	{
		return this.instructionType;				
	}
	
	/**
	 * @return The number of <code>Op</code> delivered by constructor.
	 */
	public int getOpCount()
	{
		return this.opsCount;
	}
	
}
