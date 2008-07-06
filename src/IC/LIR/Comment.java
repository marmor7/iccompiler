package IC.LIR;

public class Comment extends Instruction{
	
	private String comment;
	
	/**
	* Class constructor
	* @param comm The string that represents the comment
	*/
	public Comment(String comm)
	{
		super(CommentType.Comment);
		this.comment = comm;
	}
	
	/**
	 * @return The comment
	 */
	public String getComment()
	{
		return this.comment;
	}
	
	/**
	* Type's <code>enum</code> 
	*
	*/
	public enum CommentType implements InstructionType
	{
		Comment
	}
	
	public String toString()
	{
		return "# " + this.comment; 
	}
	
}
