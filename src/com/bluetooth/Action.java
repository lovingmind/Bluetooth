
/**
 * @author balakrishnan
 *
 */
package com.bluetooth;
public class Action
{
	private String action = "";
	private String descripiton = "";
	private String className = "";

	public Action(String name, String descripiton, String className)
	{
		this.action = name;
		this.descripiton = descripiton;
		this.className = className;
	}

	public String getAction()
	{
		return action;
	}

	public String getDescripiton()
	{
		return descripiton;
	}

	public String getClassName()
	{
		return className;
	}
}
