package selfoptsysapps.im;

import selfoptsys.*;

public class AnImLogger extends ALoggable {
    
    private AnImUI m_ui = null;
    
    public AnImLogger(
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
  		
        setOverrideConfigFileUsersWhoInputSettings( false );
        setUserInputsCommands( true );
        setRunningUIAsMaster( true );
        
    }
    
    public void setUI(
            AnImUI ui
            ) {
        m_ui = ui;
    }
    
    public synchronized void replayToView( Object message ) {
        m_ui.appendText( ( AnImMessage ) message );
        return;
    }

    public void outInsert( Object message ) {
        sendOutputMsg( message );
    }

    public void replayToModel( Object message ) {
        outInsert( message );
        return;
    }

    public void inInsert( Object message ) {
        sendInputMsg( message );
    }

    public void sessionStarted() {
        return;
    }
    
    public void quit() {
        super.quit();
    }
}
