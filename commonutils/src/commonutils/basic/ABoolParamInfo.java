package commonutils.basic;

public class ABoolParamInfo 
	extends ParamInfo {
	
	public ABoolParamInfo( String name, String value )
	{
		super(name, value);
	}
	
	public ABoolParamInfo( String name, boolean value )
	{
		this( name, String.valueOf( value ) );
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public Object getValue()
	{
		boolean boolValue = false;
		try {
			boolValue = Integer.parseInt( (String) m_value ) == 1 ? true : false;
		}
		catch ( Exception e ) {
			boolValue = ( (String) m_value ).toLowerCase().equals( "true" ) ? true : false;
		}
		

		return boolValue;
	}
}