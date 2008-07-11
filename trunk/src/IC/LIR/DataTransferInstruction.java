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
	
	public String toString()
    {
            String ret = "";
            DataTransferInstructionType dtit = (DataTransferInstructionType)this.getInstructionType();

            if (dtit.equals(DataTransferInstructionType.MoveField))
            {
                    if(this.getOp3() != null)
                    {
                            ret     = "MoveField " +
                                      this.getOp1().toString() +
                                      "," +
                                      this.getOp2().toString() +
                                      "." +
                                      this.getOp3().toString();
                    }
                    else
                    {
                            ret     = "MoveField " +
                              this.getOp1().toString() +
                              "," +
                              this.getOp2().toString();
                    }
            }
            else if (dtit.equals(DataTransferInstructionType.Move))
            {
                    ret = "Move " +
                          this.getOp1() + ", " +
                          this.getOp2();
            }
            else if (dtit.equals(DataTransferInstructionType.ArrayLength))
            {
            	   ret = "ArrayLength " +
                   this.getOp1() + ", " +
                   this.getOp2();
            }
            else if (dtit.equals(DataTransferInstructionType.MoveArray))
            {
            	   ret = "MoveArray " +
                   this.getOp1() + ", " +
                   this.getOp2();
            }

            
            if (this.getOptComment() != null)
            {
                    ret+= " " + this.getOptComment();
            }

            return ret;

    }
}
