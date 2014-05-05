package selfoptsys.overlay;

import java.util.*;

import selfoptsys.*;

public class AnOverlayManagerUserInfo {

	private int m_userIndex;
	private Logger m_userLogger;
	private int m_masterUserIndex;
	private Logger m_masterUserLogger;
	private boolean m_userInputsCommands;
	private boolean m_slaveOfUserInputsCommands;
	
	private Vector<AMapping> m_inputSourceMappings;
	private Vector<AMapping> m_inputDestMappings;
	private Vector<AMapping> m_outputSourceMappings;
	private Vector<AMapping> m_outputDestMappings;

	public AnOverlayManagerUserInfo(
			int userIndex,
			Logger userLogger,
			int masterUserIndex,
			Logger masterUserLogger
			) {
		m_userIndex = userIndex;
		m_userLogger = userLogger;
		m_masterUserIndex = masterUserIndex;
		m_masterUserLogger = masterUserLogger;

		m_inputSourceMappings = new Vector<AMapping>();
		m_inputDestMappings = new Vector<AMapping>();
		m_outputSourceMappings = new Vector<AMapping>();
		m_outputDestMappings = new Vector<AMapping>();
	}
	
	public void addInputSourceMapping(
			AMapping mapping
			) {
		m_inputSourceMappings.add( mapping );
	}
	public void addInputDestMapping(
			AMapping mapping
			) {
		m_inputDestMappings.add( mapping );
	}
	public void addOutputSourceMapping(
			AMapping mapping
			) {
		m_outputSourceMappings.add( mapping );
	}
	public void addOutputDestMapping(
			AMapping mapping
			) {
		m_outputDestMappings.add( mapping );
	}

	public int getUserIndex() {
		return m_userIndex;
	}
	public Logger getUserLogger() {
		return m_userLogger;
	}
	public int getMasterUserIndex() {
		return m_masterUserIndex;
	}
	public Logger getMasterUserLogger() {
		return m_masterUserLogger;
	}
	
	public Vector<AMapping> getInputSourceMappings() {
		return m_inputSourceMappings;
	}
	public Vector<AMapping> getInputDestMappings() {
		return m_inputDestMappings;
	}
	public Vector<AMapping> getOutputSourceMappings() {
		return m_outputSourceMappings;
	}
	public Vector<AMapping> getOutputDestMappings() {
		return m_outputDestMappings;
	}
	
	public void setMasterUserLogger(
			Logger masterUserLogger
			) {
		m_masterUserLogger = masterUserLogger;
	}
	
	public void removeAllMappingsForUser(
			int userIndex
			) {
		for ( int i = m_inputDestMappings.size() - 1; i >= 0; i-- ) {
			if ( m_inputDestMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_inputDestMappings.removeElementAt( i );
			}
		}

		for ( int i = m_inputSourceMappings.size() - 1; i >= 0; i-- ) {
			if ( m_inputSourceMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_inputSourceMappings.removeElementAt( i );
			}
		}

		for ( int i = m_outputDestMappings.size() - 1; i >= 0; i-- ) {
			if ( m_outputDestMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_outputDestMappings.removeElementAt( i );
			}
		}

		for ( int i = m_outputSourceMappings.size() - 1; i >= 0; i-- ) {
			if ( m_outputSourceMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_outputSourceMappings.removeElementAt( i );
			}
		}

	}
	
	public void removeInputDestMapping(
			int userIndex
			) {
		
		for ( int i = 0; i < m_inputDestMappings.size(); i++ ) {
			if ( m_inputDestMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_inputDestMappings.removeElementAt( i );
				break;
			}
		}
		
	}
	
	public void removeInputSourceMapping(
			int userIndex
			) {
		
		for ( int i = 0; i < m_inputSourceMappings.size(); i++ ) {
			if ( m_inputSourceMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_inputSourceMappings.removeElementAt( i );
				break;
			}
		}
		
	}
	
	public void removeOutputDestMapping(
			int userIndex
			) {
		
		for ( int i = 0; i < m_outputDestMappings.size(); i++ ) {
			if ( m_outputDestMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_outputDestMappings.removeElementAt( i );
				break;
			}
		}
		
	}
	
	public void remoteOutputSourceMapping(
			int userIndex
			) {
		
		for ( int i = 0; i < m_outputSourceMappings.size(); i++ ) {
			if ( m_outputSourceMappings.elementAt( i ).getUserIndex() == userIndex ) {
				m_outputSourceMappings.removeElementAt( i );
				break;
			}
		}
		
	}
	
	public void removeAllInputDestMappings() {
		m_inputDestMappings.clear();
	}
	
	public void remoteAllInputSourceMappings() {
		m_inputSourceMappings.clear();
	}
	
	public void removeAllOutputDestMappings() {
		m_outputDestMappings.clear();
	}
	
	public void remoteAllOutputSourceMappings() {
		m_outputSourceMappings.clear();
	}
	
	public void setUserInputsCommands(
			boolean userInputsCommands
			) {
		m_userInputsCommands = userInputsCommands;
	}
	public boolean getUserInputsCommands() {
		return m_userInputsCommands;
	}
	
	public void setSlaveOfUserInputsCommands(
			boolean slaveOfUserInputsCommands
			) {
		m_slaveOfUserInputsCommands = slaveOfUserInputsCommands;
	}
	public boolean getSlaveOfUserInputsCommands() {
		return m_slaveOfUserInputsCommands;
	}
}
