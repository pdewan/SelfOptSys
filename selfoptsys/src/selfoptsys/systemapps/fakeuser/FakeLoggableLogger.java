package selfoptsys.systemapps.fakeuser;

import java.util.concurrent.*;

import selfoptsys.*;


public class FakeLoggableLogger extends ALoggable {
    
    BlockingQueue<Object> m_quitBB;
    
    public FakeLoggableLogger(
            int userIndex,
            String registryHost,
            int registryPort,
            BlockingQueue<Object> quitBB
            ) {
        super( userIndex, registryHost, registryPort, true );
        
        setOverrideConfigFileUsersWhoInputSettings( true );
        setUserInputsCommands( false );
        setRunningUIAsMaster( true );
        
        m_quitBB = quitBB;
        startLogger();
    }
    
    public synchronized void replayToView( Object command ) {
        return;
    }

    public void outInsert( Object command ) {
        sendOutputMsg(
                command,
                false,
                false
                );
    }

    public void replayToModel( Object command ) {
        outInsert( command );
        return;
    }

    public void inInsert( Object command ) {
        sendInputMsg( 
                command,
                false,
                false
                );
    }

    public void sessionStarted() {
        return;
    }
    
    public void quit() {
    	try {
    		m_quitBB.put( new Integer( 1 ) );
    	}
    	catch ( Exception e ) {
    		e.printStackTrace();
    	}
    }
}
