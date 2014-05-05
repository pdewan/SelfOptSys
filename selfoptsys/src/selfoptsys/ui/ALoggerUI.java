package selfoptsys.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import selfoptsys.*;
import util.misc.Message.OutputLoggingLevel;

import bus.uigen.*;
import bus.uigen.introspect.AttributeNames;


public class ALoggerUI 
	implements LoggerUI {
	
	Vector<PropertyChangeListener> m_observers;

	LocalLogger m_logger;
	
	private int m_userIndex = -1;
	private int m_masterUserIndex = -1;
	private boolean m_isMaster = false;
	
	boolean m_joined = false;
	
//	private UserControlledLoggerUI m_userControlledLoggerUI;
//	private UserControlledMetaSchedulerUI m_userControlledSchedulerUI;
//	
	public ALoggerUI(
			LocalLogger logger
			) {
		ObjectEditor.setOutputLoggingLevel( OutputLoggingLevel.ERROR );

		m_observers = new Vector<PropertyChangeListener>();
		
		m_logger = logger;
		m_userIndex = m_logger.getUserIndex();
		m_joined = m_logger.getMasterUserIndex() != -1 ? true : false;
        ObjectEditor.setMethodAttribute(ALoggerUI.class, "setIsJoined", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(ALoggerUI.class, "getIsJoined", AttributeNames.VISIBLE, false);
	}
	
	public void addPropertyChangeListener(
			PropertyChangeListener l
			) {
		m_observers.add( l );
	}
	
	private void notifyAllObservers(
			PropertyChangeEvent e
			) {
		for ( int i = 0; i < m_observers.size(); i++ ) {
			m_observers.elementAt( i ).propertyChange( e );
		}
	}

	public boolean preSetMasterUserIndexInt() {
		return false;
	}
	public void setMasterUserIndex(
			int masterUserIndex
			) {
		int oldMasterUserIndex = m_masterUserIndex;
		m_masterUserIndex = masterUserIndex;
		setIsMaster( m_masterUserIndex == m_userIndex );
		notifyAllObservers(
				new PropertyChangeEvent(
						this,
						"MasterUserIndex",
						oldMasterUserIndex,
						m_masterUserIndex
						)
				);
	}
	public int getMasterUserIndex() {
		return m_masterUserIndex;
	}

	public int getUserIndex() {
		return m_userIndex;
	}

	public boolean preSetIsMasterBoolean() {
		return false;
	}
	public void setIsMaster(
			boolean isMaster
			) {
		boolean oldIsMaster = m_isMaster;
		m_isMaster = isMaster;
		notifyAllObservers(
				new PropertyChangeEvent(
						this,
						"IsMaster",
						oldIsMaster,
						m_isMaster
						)
				);
	}	
	public boolean getIsMaster() {
		return m_isMaster;
	}

	public boolean preJoinAsMaster() {
		return !getIsJoined();
	}
	public void joinAsMaster() {
		m_logger.joinAsMaster();
	}
	
	public boolean preJoinAsSlaveOfInt() {
		return !getIsJoined();
	}
	public void joinAsSlaveOf(
			int masterUserIndex
			) {
		m_logger.joinAsSlave(
				masterUserIndex
				);
	}
	
    public boolean preSetIsJoinedBoolean() {
    	return false;
    }
    public void setIsJoined(
    		boolean joined
    		) {
    	boolean oldJoined = m_joined;
    	m_joined = joined;
    	notifyAllObservers( 
    			new PropertyChangeEvent( 
    					this, 
    					"IsJoined", 
    					oldJoined, 
    					m_joined
    					)
    			);
    }
    public boolean getIsJoined() {
    	return m_joined;
    }

	public void quit() {
		m_logger.prepareToQuit();
		m_logger.quit();
	}
	
	public boolean preSetIsInputtingCommandsBoolean() {
		return getIsJoined();
	}
    public void setIsInputtingCommands(
    		boolean inputsCommands
    		) {
    	m_logger.setUserInputsCommands( inputsCommands );
    }
    public boolean getIsInputtingCommands() {
    	return m_logger.getUserInputsCommands();
    }
    
//    public void setUserControlledLoggerUI(
//            UserControlledLoggerUI userControlledLoggerUI
//            ) {
//        m_userControlledLoggerUI = userControlledLoggerUI;
//    }
//    public UserControlledLoggerUI getUserControlledLoggerUI() {
//        return m_userControlledLoggerUI;
//    }
//    
//    public void setUserControlledSchedulerUI(
//            UserControlledMetaSchedulerUI userControlledSchedulerUI
//            ) {
//        m_userControlledSchedulerUI = userControlledSchedulerUI;
//    }
//    public UserControlledMetaSchedulerUI getUserControlledSchedulerUI() {
//        return m_userControlledSchedulerUI;
//    }
    
}
