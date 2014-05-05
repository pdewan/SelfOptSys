package selfoptsys.ui;

import java.beans.PropertyChangeEvent;
import java.util.*;

import selfoptsys.*;
import selfoptsys.config.*;

import commonutils.basic2.*;

public abstract class ASessionRegistryUI 
	implements SessionRegistryUI {

    LocalSessionRegistry m_sessionRegistry;
    
    String m_registryHost = null;
    int m_registryPort = 0;
    
    Vector<UIMapping> m_uiMappings;
    
	boolean m_ignoreNextPropertyChangeListenerEvent = false;
	
    public ASessionRegistryUI(
    		String registryHost,
    		int registryPort
    		) {
    	m_registryHost = registryHost;
    	m_registryPort = registryPort;

    	m_sessionRegistry = new ASessionRegistry(
        		m_registryHost,
        		m_registryPort
        		);
        m_sessionRegistry.startRegistry();
        m_uiMappings = new Vector<UIMapping>();
        
        OperationMode operationMode = OperationMode.valueOf( 
                AMainConfigParamProcessor.getInstance().getStringParam( Parameters.OPERATION_MODE ) );
        if ( operationMode == OperationMode.RECORD ) {
        	m_sessionRegistry.startRecordingManager();
        }
    }
    
    
    public void addMapping( UIMapping uiMapping ) {
    	m_uiMappings.addElement( uiMapping );
    	
    	uiMapping.addPropertyChangeListener( this );
    }
    
    public void removeMapping( UIMapping uiMapping ) {
    	m_uiMappings.remove( uiMapping );
    }
    
	public void propertyChange(
			PropertyChangeEvent evt
			) {
		if ( evt.getSource() instanceof UIMapping ) {
			UIMapping uiMapping = (UIMapping) evt.getSource();
			
			m_sessionRegistry.uiUpdateDelayBetweenUsers(
					uiMapping.getSourceUserIndex(),
					uiMapping.getDestUserIndex(),
					uiMapping.getDelay()
					);
		}
	}


}
