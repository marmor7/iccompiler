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
	}
	
	/**
	 * Gets methods
	 * @return
	 */
	public HashMap<String, Integer> getMethods()
	{
		return methodToOffset;
	}
	
	
	/**
	 * Gets fields
	 * @return
	 */
	public HashMap<String, Integer> getFields()
	{
		return fieldToOffset;
	}

	/**
	 * Sets the DT parent
	 * @param parent
	 */
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
	
	/**
	 * Adds field to DT
	 * @param f
	 * @return
	 */
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
	
	/**
	 * Adds method to DT
	 * @param m
	 * @return
	 */
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
	
	/**
	 * Gets method position by name
	 * @param name
	 * @return
	 */
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

	/**
	 * Gets field position by name
	 * @param name
	 * @return
	 */
	public int getFieldPos(String name){
		Iterator it = this.getFields().entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
			if (name.equals(getVarName(entry.getKey()))){
				return entry.getValue() + 1;
			}
		}
		return -1;
	}
	
	/**
	 * ToString()
	 */
	public String toString(){
		String ret = "_DV_" + className + ": [";
		
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
	
	/**
	 * Gets DT label name
	 * @return
	 */
	public String getName()
	{
		String ret = "_DV_" + className;
		return ret;
		
	}
	
	/**
	 * Gets the DV actual name
	 * @return
	 */
	public String getDvName()
	{
		return className;		
	}
	
	/**
	 * Gets the current instruction
	 * @return
	 */
	public StringInstruction getInstruction(){
		return new StringInstruction(this.toString());
	}
	
	/**
	 * Gets the class name
	 * @param key
	 * @return
	 */
	private Object getClassName(String key) {
		return key.substring(0, key.indexOf("$"));
	}
	
	/**
	 * Gets a var name by key
	 * @param key
	 * @return
	 */
	private Object getVarName(String key) {
		return key.substring(key.indexOf("$") + 1, key.length());
	}
}
