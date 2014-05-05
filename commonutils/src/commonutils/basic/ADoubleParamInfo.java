package commonutils.basic;
public class ADoubleParamInfo 
	extends ParamInfo {
	
	public ADoubleParamInfo( String name, double value )
	{
		super(name, value);
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public Object getValue()
	{
		return ((Double) m_value).doubleValue();
	}
}
