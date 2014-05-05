package selfoptsys.ui;

import java.beans.PropertyChangeListener;

public interface SessionRegistryUI 
	extends PropertyChangeListener {
    
    void addMapping( 
    		UIMapping uiMapping 
    		);

    void removeMapping( 
    		UIMapping uiMapping 
    		);
    
    void addUser(
    		int userIndex
    		);
    
    void removeUser(
    		int userIndex
    		);
    
    void useCentralizedArchitecture(
    		int masterUserIndex
    		);

    void useReplicatedArchitecture();
    
    void quit();
    
}
