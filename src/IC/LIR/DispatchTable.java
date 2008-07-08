package IC.LIR;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import IC.AST.*;

public class DispatchTable {
	
	private String                   className;
	private HashMap<String, Integer> methodToOffset;
	private int                      methodCounter;
	private HashMap<String, Integer>  fieldToOffset;
	private int                      fieldCounter;
	
	public DispatchTable(String className)
	{
		this.className = className;
		methodToOffset = new HashMap<String, Integer>();
		fieldToOffset  = new HashMap<String, Integer>();
		methodCounter  = 0;
		fieldCounter   = 0;
	}
	
	public HashMap<String, Integer> getMethods()
	{
		return methodToOffset;
	}
	
	public HashMap<String, Integer> getFields()
	{
		return fieldToOffset;
	}

	public void setParent(DispatchTable parent)
	{
		//Verify this is the first insertion
		assert(fieldCounter == 0);
		assert(methodCounter == 0);
		
		//Add parent's fields and methods
		int    pos;
		Iterator it = parent.getMethods().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			pos = entry.getValue();
			methodToOffset.put(entry.getKey(), pos);			
		}
		it = parent.getFields().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			pos = entry.getValue();
			fieldToOffset.put(entry.getKey(), pos);
		}
	}
	
	public int addField(Field f){
		fieldToOffset.put(f.getName(), fieldCounter);
		fieldCounter++;
		return fieldCounter - 1; //returns the counter before the increment
	}
	
	public int addMethod(Method m){
		methodToOffset.put(m.getName(), methodCounter);
		methodCounter++;
		return methodCounter - 1; //returns the counter before the increment
	}
	
	public int getMethodPos(String name){
		return methodToOffset.get(name);
	}

	public int getFieldPos(String name){
		return fieldToOffset.get(name);
	}
	
	public String toString(){
		String ret = "_DV_" + className + ": [";
		
		/*
		for (int i = 0; i < fieldCounter; i++){
			Iterator<String> it = fieldToOffset.keySet().iterator();
			while (it.hasNext()){
				String field = it.next();
				if (fieldToOffset.get(field) == i){
					ret += i + ": " + field + ",";
					break;
				}
			}
		}
		*/
		
		for (int i = 0; i < methodCounter; i++){
			Iterator<String> it = methodToOffset.keySet().iterator();
			while (it.hasNext()){
				String method = it.next();
				if (methodToOffset.get(method) == i){
					ret += "_" + className + "_" + method + ",";
					break;
				}
			}
		}
		if ((methodCounter > 0))
			ret = ret.substring(0, ret.length() - 1);
		
		ret += "]";
		return ret;
	}
	
	public String getName()
	{
		String ret = "_DV_" + className;
		return ret;
		
	}
	
	
	public StringInstruction getInstruction(){
		return new StringInstruction(this.toString());
	}
}
