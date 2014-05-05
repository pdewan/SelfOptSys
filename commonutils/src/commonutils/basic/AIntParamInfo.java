package commonutils.basic;
public class AIntParamInfo 
	extends ParamInfo {
	
	public AIntParamInfo( String name, int value )
	{
		super(name, value);
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public Object getValue()
	{
		return ((Integer) m_value).intValue();
	}
}
