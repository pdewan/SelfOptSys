package commonutils.basic;

public abstract class ParamInfo
{
	protected String m_name;
	protected Object m_value;
	
	public ParamInfo( String name, Object value ) {
		m_name = name;
		m_value = value;
	}
	
	public abstract String getName();
	
	public abstract Object getValue();
}
