package IC.LIR;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import IC.AST.*;

public class DispatchTable {
	
	private String                   className;
	private String                   parentName = "";
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
		parentName.getClass(); //Remove compiler warning
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
		
		parentName = parent.getDvName();
		
		//Add parent's fields and methods
		int    pos;
		Iterator it = parent.getMethods().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			pos = entry.getValue();
			methodToOffset.put(entry.getKey(), pos);
			methodCounter++;
		}
		it = parent.getFields().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			pos = entry.getValue();
			fieldToOffset.put(entry.getKey(), pos);
			fieldCounter++;
		}
	}
	
	public int addField(Field f){
		
		Iterator it = this.getFields().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			if (f.getName().equals(getVarName(entry.getKey()))){
				this.getFields().put(className + "$" + getVarName(entry.getKey()) ,entry.getValue());
				this.getFields().remove(entry.getKey());
				return fieldCounter;
			}
		}
		
		fieldToOffset.put(className + "$" + f.getName(), fieldCounter);
		fieldCounter++;
		return fieldCounter - 1; //returns the counter before the increment
	}

	public int addMethod(Method m){
		Iterator it = this.getMethods().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			if (m.getName().equals(getVarName(entry.getKey()))){
				this.getMethods().put(className + "$" + getVarName(entry.getKey()) ,entry.getValue());
				this.getMethods().remove(entry.getKey());
				return methodCounter;
			}
		}
		
		methodToOffset.put(className + "$" + m.getName(), methodCounter);
		methodCounter++;
		return methodCounter - 1; //returns the counter before the increment
	}
	
	public int getMethodPos(String name){
		Iterator it = this.getMethods().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			if (name.equals(getVarName(entry.getKey()))){
				return entry.getValue();
			}
		}
		return -1;
	}

	public int getFieldPos(String name){
		Iterator it = this.getFields().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			if (name.equals(getVarName(entry.getKey()))){
				return entry.getValue();
			}
		}
		return -1;
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
					ret += "_" + method.replace('$', '_') + ",";
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
	
	public String getDvName()
	{
		return className;		
	}
	
	
	public StringInstruction getInstruction(){
		return new StringInstruction(this.toString());
	}
	
	private Object getClassName(String key) {
		return key.substring(0, key.indexOf("$"));
	}
	
	private Object getVarName(String key) {
		return key.substring(key.indexOf("$") + 1, key.length());
	}
}
