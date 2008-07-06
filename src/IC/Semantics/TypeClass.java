package IC.Semantics;

public class TypeClass extends TypeClassMold {

	private String id;
	private boolean hasParent;
	private TypeClass parent; //for primitive types - parent is this
	private int dimension;
	private boolean isPrimitive; 
	

	 public void setPrimitive()
	 {
		 isPrimitive = true;
	 }
	
	 /**
	  * 
	  * @param id Type name
	  * @param parent Parent Type name
	  * @param dimension Type dimension
	  * 
	  * New TypeClass type for classes with parent
	  */
	public TypeClass(String id, TypeClass parent, int dimension) {
		super();
		this.id = id;
		this.hasParent = true;
		this.parent = parent;
		this.dimension = dimension;		
		this.isPrimitive = false;		
	}
	
	/**
	 * 
	 * @param id
	 * @param defined
	 * @param dimension
	 * New TypeClass type for classes with NO parent
	 */
	public TypeClass(String id, int dimension, boolean isPrimitive) {
		super();
		this.id = id;
		this.hasParent = false;
		this.parent = null;
		this.dimension = dimension;		
		
		if (isPrimitive)
			this.isPrimitive = true;
		else
			this.isPrimitive = false;
	}
	
	public String toString(){
		String toReturn= "";
		
		//Dimension is 0
		if (this.dimension == 0)
		{
			//Primitive
			if (this.isPrimitive)
			{
				toReturn = this.getIdNum()+ ": Primitive type: " + this.id;
			}
			//Not primitive
			else
			{
				toReturn =  this.getIdNum()+": Class: " + this.id;
				//Has superclass
				if (this.hasParent)	
					toReturn = toReturn + ", Superclass ID: "+ this.parent.getIdNum(); 
			}
		}
		//Dimension is != 0
		else
		{
			toReturn = this.getIdNum()+ ": Array type: " + this.id;
			for (int i = 0; i < this.dimension; i++)
				toReturn = toReturn + "[]";

		}
		return toReturn;
	}
	
	public int getDimension(){
		return this.dimension;
	}
	
	public boolean subTypeOf(TypeClass other){
		
		if (this == other)
			return true;
		if  (this.id == "null" )
		{
			if (other.getId() == "string")
				return true;
			if (!other.isPrimitive)
				return true;
			else
				return false;
		}
		if (!hasParent)
		{
			return false;
		}
		
		return parent.subTypeOf(other);
	}
	
	public String getId(){
		return this.id;
	}
	
	public TypeClass getParent(){
		return this.parent;
	}
	
	public String getIdAndDim(){
		return this.id + Utils.getDimBrackets(this.getDimension());
	}
}
