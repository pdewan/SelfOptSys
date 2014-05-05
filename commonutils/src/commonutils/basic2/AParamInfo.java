package commonutils.basic2;

public class AParamInfo<T> {
    
	protected String m_name;
	protected T m_value;
	protected boolean m_specifiedOnCommandLine;
	
	public AParamInfo(
			String name,
			T value
			) {
	    this(
	           name,
	           value,
	           false
	           );
	}
	
	public AParamInfo(
	        String name,
	        T value,
	        boolean specifiedOnCommandLine
	        ) {
        m_name = name;
        m_value = value;
	    m_specifiedOnCommandLine = specifiedOnCommandLine;
	}
	
	public String getName() {
	    return m_name;
	}
	
	public T getValue() {
	    return m_value;
	}
	public void setValue(
	        T value
	        ) {
	    m_value = value;
	}
	
	public boolean getWasSpecifiedOnCmdLine() {
	    return m_specifiedOnCommandLine;
	}
}
