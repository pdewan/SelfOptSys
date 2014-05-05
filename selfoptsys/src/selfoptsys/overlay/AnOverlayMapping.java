package selfoptsys.overlay;

import java.io.Serializable;

import selfoptsys.*;

public class AnOverlayMapping 
	implements Serializable {

	private static final long serialVersionUID = -4189579042089706690L;
	private int m_rootUserIndex;
	private int m_userIndex;
	private Logger m_userLogger;
	
	public AnOverlayMapping(
			int rootUserIndex,
			int userIndex
			) {
		m_rootUserIndex = rootUserIndex;
		m_userIndex = userIndex;
	}
	
	public int getRootUserIndex() {
		return m_rootUserIndex;
	}
	
	public int getUserIndex() {
		return m_userIndex;
	}
	
	public Logger getUserLogger() {
		return m_userLogger;
	}
	public void setUserLogger(
			Logger userLogger
			) {
		m_userLogger = userLogger;
	}
}
