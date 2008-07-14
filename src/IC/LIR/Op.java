package IC.LIR;

public class Op {
	
	private String opName;
	private OpType opType;
	
	/**
	 * Type's constructor
	 * @param name The Op's name
	 * @param type The Op's type
	 */
	public Op(String name, OpType type)
	{
		this.opName = name;
		this.opType = type;
	}
	
	/**
	 * Op's type
	 */
	public enum OpType
	{
		Label,
		FuncHeader,
		Immediate,
		Memory,
		Reg,
		ThisType,
		Param, //Param is to mark that op will be returned by his function.
		Disabled,
		Var,  //var is strliteral or variable
		DV, //Dispatch table vector
		Array, //Name is Reg[Reg]
		Field, //Name Reg.number
		Other; //TBD: should this be?
	}
	
	/**
	 * 
	 * @return The Op's name
	 */
	public String getName()
	{
		return this.opName;
	}
	
	/**
	 * 
	 * @return The Op's type
	 */
	public OpType getOpType()
	{
		return this.opType;
	}
	
	public String toString(){
		return getName();
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

}
