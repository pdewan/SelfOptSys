package commonutils.basic2;

public class AParamSpec
	implements ParamSpec {

	private String m_name;
	private ConfigParamType m_type;
	private int m_numValues;
	private Object m_defaultValue;
	
	public AParamSpec(
			String name,
			ConfigParamType type,
			int numValues,
			Object defaultValue
			) {
		m_name = name;
		m_type = type;
		m_numValues = numValues;
		m_defaultValue = defaultValue;
	}
	
	public ConfigParamType getConfigParamType() {
		return m_type;
	}

	public Object getDefaultValue() {
		return m_defaultValue;
	}

	public String getName() {
		return m_name;
	}

	public int getNumValues() {
		return m_numValues;
	}

}
