package selfoptsysapps.im;

import java.awt.event.*;
import javax.swing.*;

import selfoptsys.config.*;
import commonutils.basic2.*;

public class AnIMStarter implements Runnable {

    private int m_userIndex;
    private String m_registryHost = null;
    private int m_registryPort = 0;
    
    private boolean m_printCosts = false;
    private String m_printCostsFile = "";
    
    private boolean m_waitForUserToReleaseTasks = false;
    private boolean m_waitForUserToScheduleTasks = false;
    
    public AnIMStarter( 
    		int userIndex,
    		String registryHost,
    		int registryPort,
    		boolean printCosts,
    		String costsOutputFile,
    		boolean waitForUserToReleaseTasks,
    		boolean waitForUserToScheduleTasks
    		) {
        m_userIndex = userIndex;
        m_registryHost = registryHost;
        m_registryPort = registryPort;
        m_printCosts = printCosts;
        m_printCostsFile = costsOutputFile;
        m_waitForUserToReleaseTasks = waitForUserToReleaseTasks;
        m_waitForUserToScheduleTasks = waitForUserToScheduleTasks;
    }
    
    public void run() {
    	
        final AnImLogger logger = new AnImLogger( 
        		m_userIndex,
        		m_registryHost,
        		m_registryPort
        		);
        
        try {
            UIManager.setLookAndFeel("");
        } catch (Exception e) { }
        
        AnImUI app = new AnImUI( m_userIndex );
        
        app.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logger.quit();
            }
        });
        
        app.setProgram( logger );
        logger.setUI( app );
        
        logger.setPrintCosts( m_printCosts );
        logger.setCostsOutputFile( m_printCostsFile );

        logger.setWaitForUserToReleaseTasks( m_waitForUserToReleaseTasks );
        logger.setWaitForUserToScheduleTasks( m_waitForUserToScheduleTasks );

        logger.startLogger();
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
    	
    	int userIndex = mainCpp.getIntParam( Parameters.USER_INDEX );
    	String registryHost = mainCpp.getStringParam( Parameters.RMI_REGISTRY_HOST );
    	int registryPort = mainCpp.getIntParam( Parameters.RMI_REGISTRY_PORT );
    	
        String outputDirectory = mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY );
    	boolean printCosts = mainCpp.getBooleanParam( Parameters.PRINT_COSTS );
        String printCostsFile = mainCpp.getStringParam( Parameters.COSTS_OUTPUT_FILE );
        
        boolean waitForUserToReleaseTasks = mainCpp.getBooleanParam( Parameters.WAIT_FOR_USER_TO_RELEASE_TASKS );
        boolean waitForUserToScheduleTasks = mainCpp.getBooleanParam( Parameters.WAIT_FOR_USER_TO_SCHEDULE_TASKS );

        ( new AnIMStarter( 
        		userIndex,
        		registryHost,
        		registryPort,
        		printCosts,
        		outputDirectory + "\\" + printCostsFile,
        		waitForUserToReleaseTasks,
        		waitForUserToScheduleTasks ) ).run();
    }
}
