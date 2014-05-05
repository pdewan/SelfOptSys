package commonutils.basic;

public class AStringParamInfo 
	extends ParamInfo {

	public AStringParamInfo( String name, String value )
	{
		super(name, value);
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public Object getValue()
	{
		return (String) m_value;
	}
}
