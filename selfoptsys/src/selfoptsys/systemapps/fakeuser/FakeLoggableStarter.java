package selfoptsys.systemapps.fakeuser;

import java.util.concurrent.*;

import selfoptsys.config.Parameters;

import commonutils.misc.*;
import commonutils.basic2.*;

public class FakeLoggableStarter implements Runnable {

    int m_userIndex = -1;
    String m_registryHost = null;
    int m_registryPort = 0;
    BlockingQueue<Object> m_quitBB;
    
    public FakeLoggableStarter( 
    		int userIndex,
    		String registryHost,
    		int registryPort,
    		BlockingQueue<Object> quitBB
    		) {
        m_userIndex = userIndex;
        m_registryHost = registryHost;
        m_registryPort = registryPort;
        m_quitBB = quitBB;
    }
    
    public void run() {
        new FakeLoggableLogger( 
        		m_userIndex, 
        		m_registryHost, 
        		m_registryPort, 
        		m_quitBB
        		);
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
            	
    	String registryHost = mainCpp.getStringParam( Parameters.RMI_REGISTRY_HOST );
    	int registryPort = mainCpp.getIntParam( Parameters.RMI_REGISTRY_PORT );
    	
        int startUserIndex = mainCpp.getIntParam( Parameters.START_USER_INDEX );
        int endUserIndex = mainCpp.getIntParam( Parameters.END_USER_INDEX );

        int numLoggables = endUserIndex - startUserIndex + 1;
        BlockingQueue<Object> quitBB = new ArrayBlockingQueue<Object>(
        		numLoggables
        		);
        ( new AQuitThread(
        		quitBB,
        		"FakeLoggable quitting!",
        		numLoggables
        		) ).start();
        
        for ( int i = startUserIndex; i <= endUserIndex; i++ ) {
            System.err.println( "Starting fake loggable user: " + i );
            ( new FakeLoggableStarter( 
            		i, 
            		registryHost, 
            		registryPort,
            		quitBB
            		) ).run();
        }
        
    }
}
