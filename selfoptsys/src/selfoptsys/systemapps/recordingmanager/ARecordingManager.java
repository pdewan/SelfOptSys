package selfoptsys.systemapps.recordingmanager;

import java.io.*;

import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;

import commonutils.config.SchedulingPolicy;
import commonutils.basic2.*;

public class ARecordingManager 
	extends ALoggable
	implements RecordingManager {

	protected RecordingManagerUI m_rmUI;
	
	protected FileOutputStream m_out = null;
	protected ObjectOutputStream m_oos = null;
	protected int m_eventCounter = 0;
    
	protected ConfigParamProcessor m_mainCpp;
	
    public ARecordingManager(
    		int userIndex,
    		String registryHost,
    		int registryPort
    		) {
        super(
        	userIndex,
        	registryHost,
        	registryPort,
        	false
        	);
        setOverrideConfigFileUsersWhoInputSettings( true );
        setUserInputsCommands( false );
        setRunningUIAsMaster( true );
        
        m_mainCpp = AMainConfigParamProcessor.getInstance();
        
        startLogger();
        openReplayLogForWriting();
        
        m_myLogger.joinAsMaster();
    }
    
    public void setUI(
    		RecordingManagerUI rmUI
    		) {
    	m_rmUI = rmUI;
    }
    
	public void replayToModel(
			Object command
			) {
		recordMessage( command );
		outInsert( command );
	}
	
	public void replayToView(
			Object command
			) {	
		return;
	}
	
    public void outInsert(
    		Object command
    		) {
        sendOutputMsg( command );
    }
	
	public void sessionStarted() {
		return;
	}
	
    private void openReplayLogForWriting() {
        try {
        	String logFile = m_mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY ) + "//" + 
        		m_mainCpp.getStringParam( Parameters.LOG_FILE_OUT );
            m_out = new FileOutputStream(logFile);
            m_oos = new ObjectOutputStream(m_out);
        }
        catch ( Exception e ) {
            System.err.println( "Loggable::openReplayLogForWriting Error: " + e.getMessage() );
            e.printStackTrace();
        }
    }
    
    protected void recordMessage(
            Object command
            ) {
        try {
        	// Since the recording manager is an application, it receives the raw input and
        	// output commands. To allow later replay of the command, we must first wrap it 
        	// in a Message object.
        	Message m = new ACommandMessage(
        	        MessageType.INPUT,
        	        -1,
        	        -1,
        	        -1,
        	        -1,
        	        false,
        	        null,
        	        command,
        	        SchedulingPolicy.UNDEFINED,
        	        false,
        	        false
        	        );
            m_oos.writeObject( m );
            m_oos.flush();

            String newHistoryEntry = "Event " + m_eventCounter + " recorded";
            m_rmUI.updateString(newHistoryEntry);
            m_eventCounter++;
        } 
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
	
}
