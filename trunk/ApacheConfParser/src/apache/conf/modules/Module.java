package apache.conf.modules;

/**
 * <p>
 * Class used to model an Apache module. A module has a name and type.
 * </p>
 * <p>
 * Types are as follows:<br/>
 * Static - Modules that are compiled into Apache.<br/>
 * Shared - Modules that are loaded into Apache using the "LoadModule" Directive.<br/>
 * Available - Modules that are available to be loaded into Apache.<br/>
 * </p>
 *
 */
public class Module 
{
	private String name;
	private Type type;
	
	public enum Type {
		STATIC, SHARED, AVAILABLE
	}
	
	public Module(String name, Type type)
	{
		this.name=name;
		this.type=type;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Type getType()
	{
		return type;
	}
}
