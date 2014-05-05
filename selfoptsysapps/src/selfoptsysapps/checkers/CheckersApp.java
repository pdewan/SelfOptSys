package selfoptsysapps.checkers;

import java.awt.*;
import java.awt.event.*;
import selfoptsys.config.*;

import commonutils.basic2.*;


public class CheckersApp {

    public static void main(String[] args){
        
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
    	
    	int userIndex = mainCpp.getIntParam( Parameters.USER_INDEX );
        
		Frame f = new Frame("Checkers");
		
		Checkers checkers = new Checkers();
		f.setSize(320,400);
		f.add("Center",checkers);
	
		f.pack();
		f.setVisible(true);
		checkers.init();
        
        final CheckersLogger logger = new CheckersLogger( 
        					userIndex,
                            registryHost,
                            registryPort
                            );
        checkers.connectCheckersAndLogger( logger );
        
        String outputDirectory = mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY );
    	boolean printCosts = mainCpp.getBooleanParam( Parameters.PRINT_COSTS );
        logger.setPrintCosts( printCosts );
        String printCostsFile = mainCpp.getStringParam( Parameters.COSTS_OUTPUT_FILE );
        logger.setCostsOutputFile( outputDirectory + "\\" + printCostsFile );
        
        boolean waitForUserToReleaseTasks = mainCpp.getBooleanParam( Parameters.WAIT_FOR_USER_TO_RELEASE_TASKS );
        logger.setWaitForUserToReleaseTasks( waitForUserToReleaseTasks );
        boolean waitForUserToScheduleTasks = mainCpp.getBooleanParam( Parameters.WAIT_FOR_USER_TO_SCHEDULE_TASKS );
        logger.setWaitForUserToScheduleTasks( waitForUserToScheduleTasks );

		f.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				logger.quit();
			}
		});
		
		logger.startLogger();
    }
}
