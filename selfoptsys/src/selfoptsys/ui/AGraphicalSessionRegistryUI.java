package selfoptsys.ui;

import java.util.*;

import util.misc.Message.OutputLoggingLevel;
import util.models.AListenableVector;
import util.models.ChangeableVector;

import bus.uigen.*;
import bus.uigen.introspect.AttributeNames;

import commonutils.ui.shapes.*;
import commonutils.basic2.*;

import selfoptsys.config.*;

public class AGraphicalSessionRegistryUI
	extends ASessionRegistryUI
	implements GraphicalSessionRegistryUI {
	
	Map<Integer, AppComponent> m_userInterfaces;
	Map<Integer, AppComponent> m_programComponents;
	
	AListenableVector<GraphicalUIMapping> m_graphicalUIMappings;
	AListenableVector<GraphicalUIMapping> m_graphicalUIMappingsToShow;
	
	private boolean m_showTextualInfo = true;
	private boolean m_showAllCommunicationOverlays = true;
	private int m_showOverlayForRootUserIndex = -1;
	
	private boolean m_showGraphicalMappings = true;
	
    public AGraphicalSessionRegistryUI(
    		String registryHost,
    		int registryPort
    		) {
    	super( registryHost, 
    			registryPort 
    			);
        m_sessionRegistry.setSessionRegistryUI( this );
        
        m_userInterfaces = new Hashtable<Integer, AppComponent>();
        m_programComponents = new Hashtable<Integer, AppComponent>();
        m_graphicalUIMappings = new AListenableVector<GraphicalUIMapping>();
        m_graphicalUIMappingsToShow = new AListenableVector<GraphicalUIMapping>();
        
        ConfigParamProcessor mainCpp = AMainConfigParamProcessor.getInstance();
        m_showGraphicalMappings = mainCpp.getBooleanParam( Parameters.SHOW_GRAPHICAL_MAPPINGS );
    }
    
    public void addMapping( 
    		UIMapping uiMapping
    		) {
    	
    	GraphicalUIMapping mapping = new AGraphicalUIMapping(
    			uiMapping.getSourceUserIndex(),
    			uiMapping.getDestUserIndex(),
    			uiMapping.getOverlaysInWhichIAmIn().elementAt( 0 ),
    			uiMapping.getMappedCommand(),
    			uiMapping.getMappedDirection(),
    			uiMapping.getDelay()
    			);

    	GraphicalUIMapping existingMapping = findMapping( mapping );
    	if ( existingMapping != null ) {
    		if ( existingMapping.getOverlaysInWhichIAmIn().contains( 
    				uiMapping.getOverlaysInWhichIAmIn().elementAt( 0 ) ) == false ) {
        		existingMapping.addOverlayInWhichIAmIn( 
        				uiMapping.getOverlaysInWhichIAmIn().elementAt( 0 ) 
        				);
    		}
    		return;
    	}
    	
    	mapping.setShowTextualInfoInOE( m_showTextualInfo );

    	AppComponent sourceAppComponent = null;
    	AppComponent destAppComponent = null;

    	MappedDirection direction = uiMapping.getMappedDirection();
    	if ( direction == MappedDirection.UI_TO_LOCAL_PC || 
    			direction == MappedDirection.UI_TO_REMOTE_PC ) {
    		sourceAppComponent = m_userInterfaces.get( uiMapping.getSourceUserIndex() );
    		destAppComponent = m_programComponents.get( uiMapping.getDestUserIndex() );
    	}
    	else if ( direction == MappedDirection.PC_TO_LOCAL_UI || 
    			direction == MappedDirection.PC_TO_REMOTE_UI ) {
    		sourceAppComponent = m_programComponents.get( uiMapping.getSourceUserIndex() );
    		destAppComponent = m_userInterfaces.get( uiMapping.getDestUserIndex() );    		
    	}
    	else if ( direction == MappedDirection.PC_TO_REMOTE_PC ) {
    		sourceAppComponent = m_programComponents.get( uiMapping.getSourceUserIndex() );
    		destAppComponent = m_programComponents.get( uiMapping.getDestUserIndex() );    		
    	}
    	else if ( direction == MappedDirection.UI_TO_REMOTE_UI ) {
    		sourceAppComponent = m_userInterfaces.get( uiMapping.getSourceUserIndex() );
    		destAppComponent = m_userInterfaces.get( uiMapping.getDestUserIndex() );    		
    	}
    	
    	mapping.setSourceAppComponent( sourceAppComponent );
    	mapping.setDestAppComponent( destAppComponent );
    	
    	m_graphicalUIMappings.addElement( mapping );
    	
    	if ( m_showGraphicalMappings ) {
	    	if ( m_showAllCommunicationOverlays || 
	    			uiMapping.getOverlaysInWhichIAmIn().elementAt( 0 ) == -1 ||
	    			uiMapping.getOverlaysInWhichIAmIn().contains( getShowOverlayForRootUserIndex() ) ) {
	    		m_graphicalUIMappingsToShow.add( mapping );
	    	}
    	}
    	super.addMapping( mapping );
    	
    }
    
    public void removeMapping( 
    		UIMapping uiMapping
    		) {
    	
    	GraphicalUIMapping mapping = new AGraphicalUIMapping(
    			uiMapping.getSourceUserIndex(),
    			uiMapping.getDestUserIndex(),
    			uiMapping.getOverlaysInWhichIAmIn().elementAt( 0 ),
    			uiMapping.getMappedCommand(),
    			uiMapping.getMappedDirection(),
    			uiMapping.getDelay()
    			);
    	
    	GraphicalUIMapping existingMapping = findMapping( mapping );
    	if ( existingMapping == null ) {
    		return;
    	}
    	
    	existingMapping.removeOverlayInWhichIAmIn( 
    			uiMapping.getOverlaysInWhichIAmIn().elementAt( 0 ) 
    			);
    	if ( existingMapping.getCount() > 0 ) {
    		return;
    	}
    	
    	existingMapping.setDestAppComponent( null );
    	existingMapping.setSourceAppComponent( null );
    	m_graphicalUIMappings.removeElement( existingMapping );
    	
    	if ( m_showGraphicalMappings ) {
	    	if ( getShowAllCommunicationOverlays() || 
	    			uiMapping.getOverlaysInWhichIAmIn().contains( getShowOverlayForRootUserIndex() ) ) {
	    		m_graphicalUIMappingsToShow.removeElement( existingMapping );
	    	}
    	}
    	
    	super.removeMapping( mapping );
    	
    }

	public ChangeableVector<GraphicalUIMapping> getUIMappings() {
		return m_graphicalUIMappingsToShow;
	}
	
	public void addUser(
			int userIndex
			) {
		
		int offsetX = userIndex * AppComponent.TOTAL_WIDTH +
			( userIndex + 1 ) * HORZ_DIST_BETWEEN_COMPONENTS;
		
		AppComponent userInterfaceComponent = new AnAppComponent(
				AppComponentType.USER_INTERFACE,
				"UI " + userIndex,
				offsetX,
				TOP_MARGIN
				);
		m_userInterfaces.put( userIndex, userInterfaceComponent );
		
		AppComponent programComponentComponent = new AnAppComponent(
				AppComponentType.PROGRAM_COMPONENT,
				"PC " + userIndex,
				offsetX,
				TOP_MARGIN + AppComponent.TOTAL_HEIGHT + VERT_DIST_BETWEEN_COMPONENTS
				);
		m_programComponents.put( userIndex, programComponentComponent );
	}
	
	public void removeUser(
			int userIndex
			) {
		Oval userInterface = m_userInterfaces.get( userIndex ).getOval();
		Oval programComponent = m_programComponents.get( userIndex ).getOval();
		
		m_userInterfaces.remove( userInterface );
		m_programComponents.remove( programComponent );
		
		for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
			if ( m_graphicalUIMappings.elementAt( i ).getSourceAppComponent() != null 
					&& m_graphicalUIMappings.elementAt( i ).getSourceAppComponent().equals( userInterface ) ) {
				m_graphicalUIMappings.elementAt( i ).setSourceAppComponent( null );
			}
			
			if ( m_graphicalUIMappings.elementAt( i ).getDestAppComponent() != null 
					&& m_graphicalUIMappings.elementAt( i ).getDestAppComponent().equals( programComponent ) ) {
				m_graphicalUIMappings.elementAt( i ).setDestAppComponent( null );
			}
		}
	}
    
	public void useCentralizedArchitecture(
    		int masterUserIndex
    		) {
		m_sessionRegistry.setupCentralizedArchitecture(
				masterUserIndex
				);
	}

	public void useReplicatedArchitecture() {
		m_sessionRegistry.setupReplicatedArchitecture();
	}

	public void quit() {
		m_sessionRegistry.quit();
		System.exit( 0 );
	}
	
	private GraphicalUIMapping findMapping(
			GraphicalUIMapping mapping
			) {
		
		for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
			if ( m_graphicalUIMappings.elementAt( i ).equals( mapping ) ) {
				return m_graphicalUIMappings.elementAt( i );
			}
		}
		
		return null;
	}
	
    
    public void setShowTextualInfo(
    		boolean showTextualInfo
    		) {
    	m_showTextualInfo = showTextualInfo;
		for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
			m_graphicalUIMappings.elementAt( i ).setShowTextualInfoInOE( m_showTextualInfo );
		}
    }
    
    public boolean getShowTextualInfo() {
    	return m_showTextualInfo;
    }
    
	public void setShowAllCommunicationOverlays(
			boolean showAllCommunicationOverlays
			) {
		m_showAllCommunicationOverlays = showAllCommunicationOverlays;
		
		if ( m_showGraphicalMappings ) {
			m_graphicalUIMappingsToShow.clear();
			for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
				if ( m_graphicalUIMappings.elementAt( i ).getOverlaysInWhichIAmIn().elementAt( 0 ) == -1 ) {
					m_graphicalUIMappingsToShow.add( m_graphicalUIMappings.elementAt( i ) );
				}
			}
			
			if ( m_showAllCommunicationOverlays ) {
				for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
					if ( m_graphicalUIMappings.elementAt( i ).getOverlaysInWhichIAmIn().elementAt( 0 ) == -1 ) {
						continue;
					}
					m_graphicalUIMappingsToShow.add( m_graphicalUIMappings.elementAt( i ) );
				}
				
				setShowOverlayForRootUserIndex( getShowOverlayForRootUserIndex() );
			}
		}
	}
	public boolean getShowAllCommunicationOverlays() {
		return m_showAllCommunicationOverlays;
	}
	
//	public boolean preSetShowOverlayForRootUserIndexInt() {
//		return !getShowAllCommunicationOverlays();
//	}
	public void setShowOverlayForRootUserIndex(
			int userIndex
			) {
		m_showOverlayForRootUserIndex = userIndex;
		if ( m_showAllCommunicationOverlays ) {
			return;
		}

		if ( m_showGraphicalMappings ) {
			m_graphicalUIMappingsToShow.clear();
			for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
				if ( m_graphicalUIMappings.elementAt( i ).getOverlaysInWhichIAmIn().elementAt( 0 ) == -1 ) {
					m_graphicalUIMappingsToShow.add( m_graphicalUIMappings.elementAt( i ) );
				}
			}
			
			for ( int i = 0; i < m_graphicalUIMappings.size(); i++ ) {
				Vector<Integer> overlaysInWhichMappingIsIn = m_graphicalUIMappings.elementAt( i ).getOverlaysInWhichIAmIn();
				if ( overlaysInWhichMappingIsIn.contains( userIndex ) ) {
					m_graphicalUIMappingsToShow.add( m_graphicalUIMappings.elementAt( i ) );
				}
			}
		}
	
	}
	public int getShowOverlayForRootUserIndex() {
		return m_showOverlayForRootUserIndex;
	}
	
	
	public static void main( String[] args ) {

		ObjectEditor.setOutputLoggingLevel( OutputLoggingLevel.ERROR );

        ConfigParamProcessor mainCpp = AMainConfigParamProcessor.getInstance(
                args,
                true
                );
        String configFilePath = mainCpp.getStringParam( Parameters.CUSTOM_CONFIGURATION_FILE );
        AMainConfigParamProcessor.overrideValuesByThoseSpecifiedInSource(
        		mainCpp,
        		ASettingsFileConfigParamProcessor.getInstance(
                        configFilePath,
                        true
                        )
        		);
    	
    	String registryHost = mainCpp.getStringParam( Parameters.RMI_REGISTRY_HOST );
    	int registryPort = mainCpp.getIntParam( Parameters.RMI_REGISTRY_PORT );
    	
        OperationMode operationMode = OperationMode.valueOf( mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
        if ( operationMode == OperationMode.REPLAY ) {
            System.err.println( "STATUS: Using SessionRegistryUI in REPLAY mode is not recommended." );
        }
        
        SessionRegistryUI sessionRegistryUI;
        
        sessionRegistryUI = new AGraphicalSessionRegistryUI(
        		registryHost,
        		registryPort
        		);
        
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "main", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "init", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "addMapping", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "removeMapping", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "addUser", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "removeUser", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "propertyChange", AttributeNames.VISIBLE, false);
        ObjectEditor.edit( sessionRegistryUI );
    }
	
	public static GraphicalSessionRegistryUI startGraphicalSessionRegistryUI(
			String sessionRegistryHost,
			int sessionRegistryPort
			) {
		
        GraphicalSessionRegistryUI graphicalSessionRegistryUI = new AGraphicalSessionRegistryUI(
        		sessionRegistryHost,
        		sessionRegistryPort
        		);

        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "main", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "init", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "addMapping", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "removeMapping", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "addUser", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "removeUser", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "updateMappingsWithDelay", AttributeNames.VISIBLE, false );
        ObjectEditor.setMethodAttribute( AGraphicalSessionRegistryUI.class, "propertyChange", AttributeNames.VISIBLE, false );
        ObjectEditor.edit( graphicalSessionRegistryUI );
		
        return graphicalSessionRegistryUI;
        
	}

}
