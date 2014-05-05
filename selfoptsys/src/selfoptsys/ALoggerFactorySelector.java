package selfoptsys;

public class ALoggerFactorySelector {

	private static LoggerFactory m_factory = new ADefaultLoggerFactory();
	
	public static void setFactory(
			LoggerFactory factory
			) {
	    m_factory = factory;
	}
	
	public static LoggerFactory getFactory() {
		return m_factory;
	}
	
}
