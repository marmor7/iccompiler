package IC.LIR;

public class LibraryInstruction extends Instruction {
	private int size = 0;
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public LibraryInstruction(Op op1P, Op op2P) 
	{
		super(op1P, op2P, LibraryInstructionType.Dummy);
	}
	
	/**
	* Type's <code>enum</code> 
	*
	*/
	public enum LibraryInstructionType implements InstructionType
	{
		Dummy;
	}
	
	public int getSize()
	{
		return this.size;
	}

	 public String toString(){
	     String ret = "Library " + getOp1().getName()+"," + getOp2().getName();
	
	     //Good for all types:
	     if (this.getOptComment() != null)
	     {
	             ret+= " " + this.getOptComment();
	         }
	         return ret;
	
	}
}
