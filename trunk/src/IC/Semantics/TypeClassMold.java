package IC.Semantics;

public class TypeClassMold {

	private static int counter =0;
	private int idNum ;
	
	public TypeClassMold(){	
		counter++;
		idNum = counter;
	};
	
	public void undoCounter()
	{
		counter--;
	}
	
	public int getIdNum()
	{
		return idNum;
	}
}
