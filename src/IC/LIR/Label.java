package IC.LIR;
public class Label extends Instruction {
	
	private String name;
	
	/**
	 * Type's constructor
	 * @param op1P First Op 
	 * @param op2P Second Op
	 * @param type Instruction's Type
	 */
	public Label(Op op1P, Op op2P, LabelType type) {
		super(op1P, op2P, type);
	}
	
	public Label(String nameP) 
	{
		super(LabelType.Label);
		this.name = nameP;
	}
	
	public Label(String nameP, String comment) 
	{
		super(LabelType.Label);
		this.name = nameP;
		this.setOptComment(comment);
	}

	/**
	* @return The corresponding LIR code for the Instructions instance object
	*/
	public String toString()
	{
		String ret = "_" + getName() + ":";
		 
		if (this.getOptComment() != null)
		{
			ret+="   " + this.getOptComment().toString();
		}
		
		return ret;
	}
	
	/**
	 * @return The label's name
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	* Type's <code>enum</code> 
	*
	*/
	public enum LabelType implements InstructionType
	{
		Label
	}
}
