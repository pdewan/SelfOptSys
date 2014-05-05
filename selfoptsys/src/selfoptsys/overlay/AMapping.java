package selfoptsys.overlay;

import java.io.*;

import selfoptsys.*;

public class AMapping 
	implements Serializable {

	private static final long serialVersionUID = 1619576303179178492L;
	private int m_userIndex;
	private Logger m_userLogger;
	
	public AMapping(
			int userIndex,
			Logger userLogger
			) {
		m_userIndex = userIndex;
		m_userLogger = userLogger;
	}
	
	public int getUserIndex() {
		return m_userIndex;
	}
	
	public Logger getUserLogger() {
		return m_userLogger;
	}
}
