package selfoptsysapps.demo;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import bus.uigen.introspect.AttributeNames;
import bus.uigen.ObjectEditor;
import java.rmi.registry.*;
import java.util.*;

import selfoptsys.*;
import selfoptsys.config.*;
import selfoptsys.ui.*;
import selfoptsysapps.checkers.Checkers;
import selfoptsysapps.checkers.CheckersLogger;
import commonutils.basic2.*;

public class ADemoStarter 
	implements DemoStarter {

    protected final String DEFAULT_SESSION_REGISTRY_HOST = "127.0.0.1";
    protected final int DEFAULT_SESSION_REGISTRY_PORT = 1100;
    protected final String DEFAULT_CONFIG_FILE = "..\\..\\sandlot\\modulogger\\config.txt";
	
	protected String m_sessionRegistryHost = DEFAULT_SESSION_REGISTRY_HOST;
	protected int m_sessionRegistryPort = DEFAULT_SESSION_REGISTRY_PORT;
	protected String m_configFile = DEFAULT_CONFIG_FILE;
	
	protected SessionRegistryUI m_sessionRegistryUI;
    
	protected boolean m_registryStarted = false;
	protected boolean m_perfDisplayManagerStarted = false;
    
	protected Map<Integer, CheckersLogger> m_checkersLoggers;
    
    protected ConfigParamProcessor m_mainCpp;
    
    public ADemoStarter() {
        m_mainCpp = AMainConfigParamProcessor.getInstance();
        
        ConfigUtils.setProcessorCoreAffinityMask();
        
    	m_checkersLoggers = new Hashtable<Integer, CheckersLogger>();
    	
    }
    
    public boolean preSetSessionRegistryHostString() {
    	return m_registryStarted == false;
    }
	public void setSessionRegistryHost(
			String sessionRegistryHost
			) {
		m_sessionRegistryHost = sessionRegistryHost;
	}
	public String getSessionRegistryHost() {
		return m_sessionRegistryHost;
	}
	
    public boolean preSetSessionRegistryPortInt() {
    	return m_registryStarted == false;
    }
	public void setSessionRegistryPort(
			int sessionRegistryPort
			) {
		m_sessionRegistryPort = sessionRegistryPort;
	}
	public int getSessionRegistryPort() {
		return m_sessionRegistryPort;
	}
	
    public boolean preSetConfigFileString() {
    	return m_registryStarted == false;
    }
	public void setConfigFile(
			String configFile
			) {
		m_configFile = configFile;
	}
	public String getConfigFile(){
		return m_configFile;
	}
	
    public boolean preStartSessionRegistry() {
    	return m_registryStarted == false;
    }
	public void startSessionRegistry() {
		
        m_sessionRegistryUI = new AGraphicalSessionRegistryUI(
        		m_sessionRegistryHost,
        		m_sessionRegistryPort
        		);

        m_registryStarted = true;
        
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "main", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "init", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "addMapping", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "removeMapping", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "addUser", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "removeUser", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "updateMappingsWithDelay", AttributeNames.VISIBLE, false);
        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "propertyChange", AttributeNames.VISIBLE, false);
        ObjectEditor.edit(m_sessionRegistryUI);
        
	}
	
	public boolean preStartPerfOptimizer() {
	    return m_registryStarted;
	}
	public void startPerfOptimizer() {
        try {
            Registry reg = LocateRegistry.getRegistry(
                    m_sessionRegistryHost,
                    m_sessionRegistryPort
                    );
            SessionRegistry sessionRegistry = (SessionRegistry) reg.lookup("SessionRegistry");
            sessionRegistry.startPerformanceOptimizationServer();
        }
        catch ( Exception e ) {
            System.err.println( "ERROR: ADemoStarter:: Could not start performance optimizer. " + e.getMessage() + "::::" + e.getStackTrace() );
        }
	}
	
//	public boolean preStartSingleUserPerformanceDisplay() {
//		return m_registryStarted == true;
//	}
//	public void startSingleUserPerformanceDisplay() {
//    	
//		m_singleUserPerformanceDisplay = new ASingleUserPerformanceDisplay(
//				m_sessionRegistryHost,
//				m_sessionRegistryPort
//				);
//
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "main", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "init", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "addMapping", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "removeMapping", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "addUser", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "removeUser", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "updateMappingsWithDelay", AttributeNames.VISIBLE, false);
////        ObjectEditor.setMethodAttribute(AGraphicalSessionRegistryUI.class, "propertyChange", AttributeNames.VISIBLE, false);
//        ObjectEditor.edit(m_singleUserPerformanceDisplay);		
//	}
	
    public boolean preStartCheckersLoggersIntIntBoolean() {
    	return m_registryStarted == true;
    }
	public void startCheckersLoggers(
			int startUserIndex,
			int endUserIndex,
			boolean usersInputCommands
			) {
		
		for ( int i = startUserIndex; i <= endUserIndex; i++ ) {

			if ( m_checkersLoggers.get( i ) != null ) {
				continue;
			}
			
			Frame f = new Frame("Checkers");
			
			Checkers checkers = new Checkers();
			f.setSize(320,400);
			f.add("Center",checkers);
		
			f.pack();
			f.setVisible(true);
			checkers.init();
	        
	        final CheckersLogger checkersLogger = new CheckersLogger( 
	        					i,
	                            m_sessionRegistryHost,
	                            m_sessionRegistryPort,
	                            usersInputCommands
	                            );
	        checkers.connectCheckersAndLogger( checkersLogger );

			f.addWindowListener( new WindowAdapter() {
				public void windowClosing( WindowEvent e ) {
					checkersLogger.quit();
				}
			});	
			
			m_checkersLoggers.put( 
					i, 
					checkersLogger 
					);
			
			checkersLogger.startLogger();
		}
		
	}
	
    public boolean preStartCheckersLoggersAsMastersIntIntBoolean() {
    	return m_registryStarted == true;
    }
	public void startCheckersLoggersAsMasters(
			int startUserIndex,
			int endUserIndex,
			boolean usersInputCommands
			) {
		
		startCheckersLoggers(
			startUserIndex,
			endUserIndex,
			usersInputCommands
			);
		
		for ( int i = startUserIndex; i <= endUserIndex; i++ ) {
			m_checkersLoggers.get( i ).getLogger().joinAsMaster();
		}
		
	}
	
    public boolean preStartCheckersLoggersAsSlavesIntIntIntBoolean() {
    	return m_registryStarted == true;
    }
	public void startCheckersLoggersAsSlaves(
			int startUserIndex,
			int endUserIndex,
			int masterUserIndex,
			boolean usersInputCommands
			) {
		
		startCheckersLoggers(
			startUserIndex,
			endUserIndex,
			usersInputCommands
			);
		
		for ( int i = startUserIndex; i <= endUserIndex; i++ ) {
			m_checkersLoggers.get( i ).getLogger().joinAsSlave( masterUserIndex );
		}
	}
	
	public void quit() {
		m_sessionRegistryUI.quit();
		
		for ( int i = 0; i < m_checkersLoggers.size(); i++ ) {
			m_checkersLoggers.get( i ).quit();
		}
	}
	
	public static void main( String[] args ) {
		
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
                
		DemoStarter demoStarter = new ADemoStarter();
		
        ObjectEditor.setMethodAttribute( ADemoStarter.class, "main", AttributeNames.VISIBLE, false );
        ObjectEditor.edit( demoStarter );
		
	}
	
//    public boolean preStartPerfDisplayManager() {
//    	return m_registryStarted == true && m_perfDisplayManagerStarted == false;
//    }
//	public void startPerfDisplayManager() {
//		
////		m_perfDisplayManagerStarted = true;
////		
////		PerfDisplayManager perfDisplayManager = new APerfDisplayManager(
////				m_sessionRegistryHost,
////				m_sessionRegistryPort
////				);
////		
////		ObjectEditor.setMethodAttribute(APerfDisplayManager.class, "receivePerfReport", AttributeNames.VISIBLE, false);
////		ObjectEditor.edit( perfDisplayManager );
//		
//	}
	
}
